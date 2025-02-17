/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.sat.MiniSat;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.objects.queues.CircularQueue;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.chocosolver.sat.MiniSat.C_Undef;

/**
 * This engine is priority-driven constraint-oriented seven queues engine.
 * <br/>On a call to {@code onVariableUpdate}, it stores the event generated and schedules the propagator in
 * one of the 7 queues wrt to its priority for future revision.
 * <p>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/07/12
 */
public class PropagationEngine {

    @SuppressWarnings("WeakerAccess")
    public static boolean CHECK_SCOPE = false;

    /**
     * The model declaring this engine
     */
    final Model model;
    /**
     * The SAT solver, required for LCG mode
     */
    final MiniSat sat;
    /**
     * The array of propagators to execute
     */
    final List<Propagator<?>> propagators;
    /**
     * To deal with propagators added dynamically
     */
    private final DynPropagators dynPropagators;
    /**
     * The main structure of this engine: seven circular queues,
     * each of them is dedicated to store propagator to execute wrt their priority.
     */
    private final CircularQueue<Propagator<?>>[] pro_queue;

    private final CircularQueue<Variable> var_queue;

    private final CircularQueue<Propagator<?>> awake_queue;
    /**
     * The last propagator executed
     */
    protected Propagator<?> lastProp;
    /**
     * The last scheduled variable
     */
    protected Variable lastVar;
    /**
     * One bit per queue: true if the queue is not empty.
     */
    private int notEmpty;
    /**
     * PropagatorEventType's mask for delayed propagation
     */
    private int delayedPropagationType;
    /**
     * Set to <tt>true</tt> once {@link #initialize()} has been called.
     */
    private boolean init;
    /**
     * When set to '0b00', this works as a constraint-oriented propagation engine;
     * when set to '0b01', this works as a hybridization between variable and constraint oriented
     * propagation engine.
     * when set to '0b10', this works as a variable-oriented propagation engine.
     */
    private byte hybrid;
    /**
     * For dynamic addition, avoid creating a new lambda at each call
     */
    @SuppressWarnings("Convert2Diamond")
    private final Consumer<Propagator<?>> consumer = new Consumer<Propagator<?>>() {
        @Override
        public void accept(Propagator propagator) {
            awake_queue.addLast(propagator);
        }
    };

    /**
     * A propagation insight to collect information about the propagation
     */
    private PropagationInsight insight = PropagationInsight.VOID;

    /**
     * A seven-queue propagation engine.
     * Each of the seven queues deals with on priority.
     * When a propagator needs to be executed, it is scheduled in the queue corresponding to its priority.
     * The lowest priority queue is emptied before one element of the second-lowest queue is popped, etc.
     *
     * @param model the declaring model
     * @param sat   the SAT solver, required for LCG mode
     */
    public PropagationEngine(Model model, MiniSat sat) {
        this.model = model;
        int nbQueues = model.getSettings().getMaxPropagatorPriority() + 1;
        //noinspection unchecked
        this.pro_queue = new CircularQueue[nbQueues];
        for (int i = 0; i < nbQueues; i++) {
            pro_queue[i] = new CircularQueue<>(16);
        }
        this.var_queue = new CircularQueue<>(16);
        this.awake_queue = new CircularQueue<>(16);
        this.dynPropagators = new DynPropagators();
        this.propagators = new ArrayList<>();
        //0b00: cstr-ori
        //0b10: var-ori
        this.hybrid = model.getSettings().enableHybridizationOfPropagationEngine();
        this.sat = sat;
    }

    /**
     * A seven-queue propagation engine.
     * Each of the seven queues deals with on priority.
     * When a propagator needs to be executed, it is scheduled in the queue corresponding to its priority.
     * The lowest priority queue is emptied before one element of the second-lowest queue is popped, etc.
     *
     * @param model the declaring model
     */
    public PropagationEngine(Model model) {
        this(model, null);
    }

    /**
     * Build up internal structure, if not yet done, in order to allow propagation.
     * If new constraints are added after having initializing the engine, dynamic addition is used.
     * A call to clear erase the internal structure, and allow new initialisation.
     *
     * @throws SolverException if a constraint is declared more than once in this propagation engine
     */
    public void initialize() throws SolverException {
        if (!init) {
            notEmpty = 0;
            init = true;
            while (!var_queue.isEmpty()) {
                var_queue.pollFirst().clearEvents();
            }
            Constraint[] constraints = model.getCstrs();
            for (Constraint constraint : constraints) {
                Propagator<?>[] cprops = constraint.getPropagators();
                Collections.addAll(propagators, cprops);
            }
            if (model.getSettings().sortPropagatorActivationWRTPriority()) {
                propagators.sort(
                        (p1, p2) -> {
                            int p = p1.getPriority().getValue() - p2.getPriority().getValue();
                            if (p == 0) {
                                return p1.getNbVars() - p2.getNbVars();
                            } else {
                                return p;
                            }
                        });
            }
            for (int i = 0; i < propagators.size(); i++) {
                Propagator<?> propagator = getPropagator(i);
                awake_queue.addLast(propagator);
            }
        }
    }

    private Propagator<?> getPropagator(int i) {
        Propagator<?> propagator = propagators.get(i);
        if (propagator.getPriority().getValue() >= pro_queue.length) {
            throw new SolverException(
                    propagator+
                    "\nThis propagator declares a priority (" +
                    propagator.getPriority() + ") whose value (" + propagator.getPriority().getValue() +
                    ") is greater than the maximum allowed priority (" +
                    model.getSettings().getMaxPropagatorPriority() +
                    ").\n" +
                    "Either increase the maximum allowed priority (`Model model = new Model(Settings.init().setMaxPropagatorPriority(" +
                    (propagator.getPriority().getValue() + 1) +
                    "));`)  " +
                    "or decrease the propagator priority.");
        }
        propagator.setPosition(i);
        return propagator;
    }

    /**
     * Is the engine initialized?
     * Important for dynamic addition of constraints
     *
     * @return true if the engine has been initialized
     */
    public boolean isInitialized() {
        return init;
    }

    /**
     * Launch the propagation, ie, active propagators if necessary, then reach a fix point
     *
     * @throws ContradictionException if a contradiction occurs
     */
    public void propagate() throws ContradictionException {
        propagateSat();
        insight.clear();
        activatePropagators();
        do {
            manageModifications();
            for (int i = nextNotEmpty(); i > -1; i = nextNotEmpty()) {
                assert !pro_queue[i].isEmpty() : "try to pop a propagator from an empty queue";
                if (model.getSolver().isTimeLimitMet()) {
                    return;
                }
                lastProp = pro_queue[i].pollFirst();
                insight.cardinality(lastProp);
                if (pro_queue[i].isEmpty()) {
                    notEmpty &= ~(1 << i);
                }
                // revision of the variable
                lastProp.unschedule();
                delayedPropagationType = 0;
                try {
                    propagateEvents();
                    propagateSat();
                    insight.update(lastProp, lastVar, false);
                } catch (ContradictionException cex) {
                    insight.update(lastProp, lastVar, true);
                    throw cex;
                }
                if (hybrid < 0b01) {
                    manageModifications();
                }
            }
        } while (!var_queue.isEmpty());
    }

    private void propagateSat() throws ContradictionException {
        if (sat != null) {
            model.getSolver().getMeasures().incPropagationCount();
            sat.propagate();
            if (sat.confl != C_Undef) {
                model.getSolver().throwsException(Cause.Sat, null, null); //todo better !
            }
        }
    }

    protected void propagateEvents() throws ContradictionException {
        if (lastProp.reactToFineEvent()) {
            model.getSolver().getMeasures().incPropagationCount();
            lastProp.doFinePropagation();
            // now we can check whether a delayed propagation has been scheduled
            if (delayedPropagationType > 0) {
                lastProp.propagate(delayedPropagationType);
            }
        } else if (lastProp.isActive()) { // need to be checked due to views
            model.getSolver().getMeasures().incPropagationCount();
            lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
        }
    }

    /**
     * Checks if some propagators were added or have to be propagated on backtrack
     *
     * @throws ContradictionException if a propagation fails
     */
    private void activatePropagators() throws ContradictionException {
        int cw = model.getEnvironment().getWorldIndex(); // get current index
        dynPropagators.descending(cw, consumer);
        while (!awake_queue.isEmpty()) {
            execute(awake_queue.pollFirst());
        }
    }

    /**
     * Execute 'coarse' propagation on a newly added propagator
     * or one that should be propagated on backtrack
     *
     * @param propagator a propagator to propagate
     * @throws ContradictionException if propagation fails
     */
    public void execute(Propagator<?> propagator) throws ContradictionException {
        if (propagator.isStateLess()) {
            propagator.setActive();
        }
        if (propagator.isActive()) {
            model.getSolver().getMeasures().incPropagationCount();
            propagator.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            model.getSolver().getMeasures().incPropagationCount();
            propagateSat();
            while (!var_queue.isEmpty()) {
                var_queue.pollFirst().schedulePropagators(this);
            }
        }
    }

    private void manageModifications() {
        if (!var_queue.isEmpty()) {
            do {
                var_queue.pollFirst().schedulePropagators(this);
            } while (hybrid < 2 && !var_queue.isEmpty());
        }
    }

    private int nextNotEmpty() {
        if (notEmpty == 0) return -1;
        return Integer.numberOfTrailingZeros(notEmpty);
    }

    /**
     * Flush <code>this</code>, i.e. remove every pending events
     */
    public void flush() {
        if (lastProp != null) {
            lastProp.doFlush();
        }
        while (!var_queue.isEmpty()) {
            var_queue.pollLast().clearEvents();
        }
        for (int i = nextNotEmpty(); i > -1; i = nextNotEmpty()) {
            while (!pro_queue[i].isEmpty()) {
                // revision of the variable
                pro_queue[i].pollLast().doFlush();
            }
            notEmpty = notEmpty & ~(1 << i);
        }
        lastProp = null;
        lastVar = null;
    }

    /**
     * Take into account the modification of a variable
     *
     * @param variable modified variable
     * @param type     type of modification event
     * @param cause    origin of the modification
     */
    public void onVariableUpdate(Variable variable, IEventType type, ICause cause) {
        if (CHECK_SCOPE && Propagator.class.isAssignableFrom(cause.getClass())) {
            // make sure the variable appears in prop scope
            Propagator<?> p = (Propagator<?>) cause;
            boolean found = false;
            for (int i = 0; i < p.getNbVars() && !found; i++) {
                found = (p.getVar(i) == variable);
            }
            assert found : variable + " not in scope of " + cause;
        }
        insight.modifiy(variable);
        if (!variable.isScheduled()) {
            var_queue.addLast(variable);
            variable.schedule();
        }
        variable.storeEvents(type.getMask(), cause);
    }

    public void schedule(Propagator<?> prop, int pindice, int mask) {
        prop.doScheduleEvent(pindice, mask);
        notEmpty |= (1 << prop.doSchedule(pro_queue));
    }

    /**
     * Execute a delayed propagator
     *
     * @param propagator propagator to execute
     * @param type       type of event to execute
     */
    public void delayedPropagation(Propagator<?> propagator, PropagatorEventType type) {
        assert propagator == lastProp;
        assert delayedPropagationType == 0 || delayedPropagationType == type.getMask();
        delayedPropagationType = type.getMask();
    }

    /**
     * @return the propagator Event Type's mask for delayed propagation
     */
    int getDelayedPropagation() {
        return delayedPropagationType;
    }

    /**
     * Action to do when a propagator is executed
     *
     * @param propagator propagator to execute
     */
    public void onPropagatorExecution(Propagator<?> propagator) {
        deactivatePropagator(propagator);
    }

    /**
     * Set the propagator as inactivated within the propagation engine
     *
     * @param propagator propagator to deactivate
     */
    public void deactivatePropagator(Propagator<?> propagator) {
        if (propagator.reactToFineEvent()) {
            propagator.doFlush();
        }
    }

    public void setInsight(PropagationInsight insight) {
        this.insight = insight;
    }

    public void setHybrid(byte hybrid) {
        this.hybrid = hybrid;
    }

    /**
     * Reset the propagation engine.
     */
    public void reset() {
        flush();
        clear();
    }

    /**
     * Clear internal structures
     */
    public void clear() {
        dynPropagators.clear();
        awake_queue.clear();
        propagators.clear();
        notEmpty = 0;
        init = false;
        lastProp = null;
        lastVar = null;
    }

    public void ignoreModifications() {
        while (!var_queue.isEmpty()) {
            var_queue.pollFirst().clearEvents();
        }
    }

    /**
     * Add a constraint to the propagation engine
     *
     * @param permanent does the constraint is permanently added
     * @param ps        propagators to add
     *                  * @throws SolverException if a constraint is declared more than once in this propagation engine
     */
    public void dynamicAddition(boolean permanent, Propagator<?>... ps) throws SolverException {
        int nbp = ps.length;
        for (int i = 0; i < nbp; i++) {
            if (permanent) {
                ps[i].setPosition(propagators.size());
                propagators.add(ps[i]);
                dynPropagators.add(ps[i]);
            }
        }
    }


    /**
     * Update the scope of variable of a propagator (addition or deletion are allowed -- p.vars are scanned)
     *
     * @param p a propagator
     */
    public void updateInvolvedVariables(Propagator<?> p) {
        propagateOnBacktrack(p); // TODO: when p is not permanent AND a new var is added ... well, one looks for trouble!
    }

    /**
     * Update the scope of variable of a propagator (addition or deletion are allowed -- p.vars are scanned)
     *
     * @param propagator a propagator
     */
    public void propagateOnBacktrack(Propagator<?> propagator) {
        int idx = propagator.getPosition();
        assert propagators.get(idx) == propagator : "Try to remove the wrong propagator";
        shift(idx);
        propagators.set(propagators.size() - 1, propagator);
        propagator.setPosition(propagators.size() - 1);
        dynPropagators.addOrUpdate(propagator);
    }

    /**
     * Delete the list of propagators in input from the engine
     *
     * @param ps a list of propagators
     */
    public void dynamicDeletion(Propagator<?>... ps) {
        for (Propagator<?> toDelete : ps) {
            if (lastProp == toDelete) {
                lastProp = null;
            }
            if (toDelete.getPosition() > -1) {
                dynPropagators.remove(toDelete);
                remove(toDelete);
            }
        }
    }

    private void remove(Propagator<?> propagator) {
        int idx = propagator.getPosition();
        if (idx > -1) {
            assert propagators.get(idx) == propagator : "Try to remove the wrong propagator";
            // todo: improve
            shift(idx);
            propagator.setPosition(-1);
            propagators.remove(propagators.size() - 1);
        }
    }

    private void shift(int from) {
        for (int i = from; i < propagators.size() - 1; i++) {
            propagators.set(i, propagators.get(i + 1));
            propagators.get(i).setPosition(i);
        }
    }

    private static class DynPropagators {

        private Propagator<?>[] elements;
        private int[] keys;
        private int size;

        DynPropagators() {
            elements = new Propagator[16];
            keys = new int[16];
            size = 0;
        }

        public void clear() {
            size = 0;
        }

        public void add(Propagator<?> e) {
            ensureCapacity();
            elements[size] = e;
            keys[size++] = Integer.MAX_VALUE;
        }

        private void ensureCapacity() {
            if (size >= elements.length - 1) {
                int nsize = ArrayUtils.newBoundedSize(elements.length, 8);
                elements = Arrays.copyOf(elements, nsize);
                keys = Arrays.copyOf(keys, nsize);
            }
        }

        void addOrUpdate(Propagator<?> e) {
            remove(e);
            add(e);
        }

        public void remove(Propagator<?> e) {
            int p = indexOf(e);
            if (p > -1) {
                removeAt(p);
            }
        }

        private void removeAt(int p) {
            if (p < size - 1) {
                System.arraycopy(elements, p + 1, elements, p, size - p);
                System.arraycopy(keys, p + 1, keys, p, size - p);
            }
            elements[--size] = null;
            keys[size] = 0;
        }

        private int indexOf(Propagator<?> e) {
            for (int i = 0; i < size; i++) {
                if (e.equals(elements[i])) {
                    return i;
                }
            }
            return -1;
        }

        void descending(int w, Consumer<Propagator<?>> cons) {
            int i = size - 1;
            while (i >= 0 && keys[i] >= w) {
                cons.accept(elements[i]);
                keys[i] = w;
                i--;
            }
        }
    }
}
