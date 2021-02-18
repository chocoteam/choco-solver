/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.propagation;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.EvtScheduler;
import org.chocosolver.util.objects.queues.CircularQueue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

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
     * Mask to deal with emptiness (see {@link #notEmpty})
     */
    private static final int WORD_MASK = 0xffffffff;
    /**
     * The model declaring this engine
     */
    private final Model model;
    /**
     * The array of propagators to execute
     */
    private final List<Propagator> propagators;
    /**
     * To deal with propagators added dynamically
     */
    private final DynPropagators dynPropagators;
    /**
     * The main structure of this engine: seven circular queues,
     * each of them is dedicated to store propagator to execute wrt their priority.
     */
    private final CircularQueue<Propagator>[] pro_queue;

    private final CircularQueue<Variable> var_queue;

    private final CircularQueue<Propagator> awake_queue;
    /**
     * The last propagator executed
     */
    private Propagator lastProp;
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
     * when set to '0b01', this workds as an hybridization between variable and constraint oriented
     * propagation engine.
     * when set to '0b10', this workds as a variable- oriented propagation engine.
     */
    private final byte hybrid;
    /**
     * For dynamyc addition, avoid creating a new lambda at each call
     */
    private final Consumer<Propagator> consumer = new Consumer<Propagator>() {
        @Override
        public void accept(Propagator propagator) {
            awake_queue.addLast(propagator);
        }
    };

    /**
     * A seven-queue propagation engine.
     * Each of the seven queues deals with on priority.
     * When a propagator needs to be executed, it is scheduled in the queue corresponding to its priority.
     * The lowest priority queue is emptied before one element of the second lowest queue is popped, etc.
     *
     * @param model the declaring model
     */
    public PropagationEngine(Model model) {
        this.model = model;
        //noinspection unchecked
        this.pro_queue = new CircularQueue[8];
        for (int i = 0; i < 8; i++) {
            pro_queue[i] = new CircularQueue<>(16);
        }
        this.var_queue = new CircularQueue<>(16);
        this.awake_queue = new CircularQueue<>(16);
        this.dynPropagators = new DynPropagators();
        this.propagators = new ArrayList<>();
        this.hybrid = model.getSettings().enableHybridizationOfPropagationEngine();
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
            Constraint[] constraints = model.getCstrs();
            for (int c = 0; c < constraints.length; c++) {
                Propagator[] cprops = constraints[c].getPropagators();
                Collections.addAll(propagators, cprops);
            }
            if (model.getSettings().sortPropagatorActivationWRTPriority()) {
                propagators.sort(
                        (p1, p2) -> {
                            int p = p1.getPriority().priority - p2.getPriority().priority;
                            if (p == 0) {
                                return p1.getNbVars() - p2.getNbVars();
                            } else return p;
                        });
            }
            for (int i = 0; i < propagators.size(); i++) {
                propagators.get(i).setPosition(i);
                awake_queue.addLast(propagators.get(i));
            }
        }
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
     * Launch the proapagation, ie, active propagators if necessary, then reach a fix point
     *
     * @throws ContradictionException if a contradiction occurs
     */
    @SuppressWarnings({"NullableProblems"})
    public void propagate() throws ContradictionException {
        activatePropagators();
        do {
            manageModifications();
            for (int i = nextNotEmpty(); i > -1; i = nextNotEmpty()) {
                assert !pro_queue[i].isEmpty() : "try to pop a propagator from an empty queue";
                lastProp = pro_queue[i].pollFirst();
                if (pro_queue[i].isEmpty()) {
                    notEmpty &= ~(1 << i);
                }
                // revision of the variable
                lastProp.unschedule();
                delayedPropagationType = 0;
                if (lastProp.reactToFineEvent()) {
                    lastProp.doFinePropagation();
                    // now we can check whether a delayed propagation has been scheduled
                    if (delayedPropagationType > 0) {
                        lastProp.propagate(delayedPropagationType);
                    }
                } else if (lastProp.isActive()) { // need to be checked due to views
                    lastProp.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                }
                if (hybrid < 0b01) {
                    manageModifications();
                }
            }
        } while (!var_queue.isEmpty());
    }

    /**
     * Checks if some propagators were added or have to be propagated on backtrack
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
    public void execute(Propagator propagator) throws ContradictionException {
        if (propagator.isStateLess()) {
            propagator.setActive();
        }
        if (propagator.isActive()) {
            propagator.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            while (!var_queue.isEmpty()) {
                schedule(var_queue.pollFirst());
            }
        }
    }

    private void manageModifications() {
        if (!var_queue.isEmpty()) {
            do {
                schedule(var_queue.pollFirst());
            } while (hybrid < 2 && !var_queue.isEmpty());
        }
    }

    private int nextNotEmpty() {
        if (notEmpty == 0) return -1;
        return Integer.numberOfTrailingZeros(notEmpty);
    }

    /**
     * Flush <code>this</code>, ie. remove every pending events
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
            Propagator p = (Propagator) cause;
            boolean found = false;
            for (int i = 0; i < p.getNbVars() && !found; i++) {
                found = (p.getVar(i) == variable);
            }
            assert found : variable + " not in scope of " + cause;
        }
        var_queue.addLast(variable);
        variable.storeEvents(type.getMask(), cause);
    }

    private void schedule(Variable variable) {
        int mask = variable.getMask();
        if (mask > 0) {
            ICause cause = variable.getCause();
            Propagator[] vpropagators = variable.getPropagators();
            int[] vindices = variable.getPIndices();
            Propagator prop;
            EvtScheduler si = variable.getEvtScheduler();
            //noinspection unchecked
            si.init(mask);
            while (si.hasNext()) {
                int p = variable.getDindex(si.next());
                int t = variable.getDindex(si.next());
                for (; p < t; p++) {
                    prop = vpropagators[p];
                    if (prop.isActive() && cause != prop) {
                        schedule(prop, vindices[p], mask);
                    }
                }
            }
        }
        variable.clearEvents();
    }

    public void schedule(Propagator prop, int pindice, int mask) {
        prop.doScheduleEvent(pindice, mask);
        notEmpty |= (1 << prop.doSchedule(pro_queue));
    }

    /**
     * Exeucte a delayed propagator
     *
     * @param propagator propagator to execute
     * @param type       type of event to execute
     */
    public void delayedPropagation(Propagator propagator, PropagatorEventType type) {
        assert propagator == lastProp;
        assert delayedPropagationType == 0 || delayedPropagationType == type.getMask();
        delayedPropagationType = type.getMask();
    }

    /**
     * Action to do when a propagator is executed
     *
     * @param propagator propagator to execute
     */
    public void onPropagatorExecution(Propagator propagator) {
        desactivatePropagator(propagator);
    }

    /**
     * Set the propagator as inactivated within the propagation engine
     *
     * @param propagator propagator to desactivate
     */
    public void desactivatePropagator(Propagator propagator) {
        if (propagator.reactToFineEvent()) {
            propagator.doFlush();
        }
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
    public void dynamicAddition(boolean permanent, Propagator... ps) throws SolverException {
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
    public void updateInvolvedVariables(Propagator p) {
        propagateOnBacktrack(p); // TODO: when p is not permanent AND a new var is added ... well, one looks for trouble!
    }

    /**
     * Update the scope of variable of a propagator (addition or deletion are allowed -- p.vars are scanned)
     *
     * @param propagator a propagator
     */
    public void propagateOnBacktrack(Propagator propagator) {
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
    public void dynamicDeletion(Propagator... ps) {
        for (Propagator toDelete : ps) {
            if (lastProp == toDelete) {
                lastProp = null;
            }
            if (toDelete.getPosition() > -1) {
                dynPropagators.remove(toDelete);
                remove(toDelete);
            }
        }
    }

    private void remove(Propagator propagator) {
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

    private class DynPropagators {

        private Propagator[] elements;
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

        public void add(Propagator e) {
            ensureCapacity();
            elements[size] = e;
            keys[size++] = Integer.MAX_VALUE;
        }

        private void ensureCapacity() {
            if (size >= elements.length - 1) {
                Propagator[] tmp = elements;
                elements = new Propagator[elements.length * 3 / 2];
                System.arraycopy(tmp, 0, elements, 0, size);
                int[] itmp = keys;
                keys = new int[elements.length];
                System.arraycopy(itmp, 0, keys, 0, size);
            }
        }

        void addOrUpdate(Propagator e) {
            remove(e);
            add(e);
        }

        public void remove(Propagator e) {
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

        private int indexOf(Propagator e) {
            for (int i = 0; i < size; i++) {
                if (e.equals(elements[i])) {
                    return i;
                }
            }
            return -1;
        }

        void descending(int w, Consumer<Propagator> cons) {
            int i = size - 1;
            while (i >= 0 && keys[i] >= w) {
                cons.accept(elements[i]);
                keys[i] = w;
                i--;
            }
        }
    }
}
