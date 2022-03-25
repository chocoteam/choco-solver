/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationEngine;
import org.chocosolver.solver.variables.*;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.iterators.EvtScheduler;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Class used to factorise code The subclass must implement Variable interface <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 30 june 2011
 */
public abstract class AbstractVariable implements Variable {

    /**
     * Message associated with last value removals exception.
     */
    static final String MSG_REMOVE = "remove last value";

    /**
     * Message associated with domain wipe out exception.
     */
    protected static final String MSG_EMPTY = "empty domain";

    /**
     * Message associated with double instantiation exception.
     */
    protected static final String MSG_INST = "the variable is already instantiated to another value";

    /**
     * Default exception message.
     */
    static final String MSG_UNKNOWN = "unknown value";

    /**
     * Message associated with wrong upper bound exception.
     */
    static final String MSG_UPP = "the new upper bound is lesser than the current lower bound";

    /**
     * Message associated with wrong lower bound exception.
     */
    static final String MSG_LOW = "the new lower bound is greater than the current upper bound";

    /**
     * Message associated with wrong bounds exception.
     */
    static final String MSG_BOUND = "new bounds are incorrect";

    /**
     * Unique ID of this variable.
     */
    private final int ID;

    /**
     * Reference to the model containing this variable (unique).
     */
    protected final Model model;

    /**
     * Name of the variable.
     */
    protected final String name;

    /**
     * List of propagators, by event type
     */
    private final BipartiteList[] propagators;
    /**
     * Nb dependencies
     */
    private int nbPropagators;

    /**
     * List of views based on this variable.
     */
    private IView<?>[] views;

    /**
     * Indices of this variable in subscribed views
     */
    private int[] idxInViews;

    /**
     * Index of the last not null view in <code>views</code>.
     */
    private int vIdx;

    /**
     * List of monitors observing this variable.
     */
    @SuppressWarnings("rawtypes")
    protected IVariableMonitor[] monitors;

    /**
     * Index of the last not null monitor in <code>monitors</code>.
     */
    protected int mIdx;

    /**
     * The event scheduler of this variable, for efficient scheduling purpose. It stores propagators
     * wrt the propagation conditions.
     */
    private final EvtScheduler<?> scheduler;

    /**
     * World index of last instantiation event.
     */
    private int instWI;

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // FOR PROPAGATION PURPOSE
    ////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * possibly aggregated event's mask
     */
    private int mask;
    /**
     * possibly aggregated event's cause
     */
    private ICause cause;

    /**
     * True if related propagators are scheduled for propagation
     */
    public boolean scheduled;

    //////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create the shared data of any type of variable.
     *
     * @param name  name of the variable
     * @param model model which declares this variable
     */
    protected AbstractVariable(String name, Model model) {
        this.name = name;
        this.model = model;
        this.views = new IView[2];
        this.idxInViews = new int[2];
        this.monitors = new IVariableMonitor[2];
        this.scheduler = createScheduler();
        int dsize = this.scheduler.select(0);
        this.propagators = new BipartiteList[dsize + 1];
        for (int i = 0; i < dsize + 1; i++) {
            this.propagators[i] = new BipartiteList(model.getEnvironment());
        }
        this.nbPropagators = 0;
        this.ID = this.model.nextId();
        this.model.associates(this);
        this.instWI = 0; // set to 0 to capture constant automatically
        this.scheduled = false;
    }

    protected abstract EvtScheduler<?> createScheduler();

    @Override
    public boolean isScheduled() {
        return scheduled;
    }

    @Override
    public void schedule() {
        this.scheduled = true;
    }

    @Override
    public void schedulePropagators(PropagationEngine engine) {
        if (mask > 0) {
            EvtScheduler<?> si = this.getEvtScheduler();
            si.init(mask);
            while (si.hasNext()) {
                int i = si.next();
                int j = si.next();
                for (; i < j; i++) {
                    propagators[i].schedule(cause, engine, mask);
                }
            }
        }
        this.clearEvents();
    }

    @Override
    public void unschedule() {
        this.scheduled = false;
    }

    @Override
    public final int getId() {
        return ID;
    }

    @Override
    public final void link(Propagator<?> propagator, int idxInProp) {
        int i = scheduler.select(propagator.getPropagationConditions(idxInProp));
        nbPropagators++;
        propagator.setVIndices(idxInProp, propagators[i].add(propagator, idxInProp));
    }

    @Override
    public final void unlink(Propagator<?> propagator, int idxInProp) {
        int i = scheduler.select(propagator.getPropagationConditions(idxInProp));
        nbPropagators--;
        propagators[i].remove(propagator, idxInProp, this);
    }

    @Override
    public void swapOnPassivate(Propagator<?> propagator, int idxInProp) {
        int i = scheduler.select(propagator.getPropagationConditions(idxInProp));
        propagators[i].swap(propagator, idxInProp, this);
    }

    @Override
    @Deprecated
    public int swapOnActivate(Propagator<?> propagator, int idxInProp) {
        throw new UnsupportedOperationException("Cannot swap on activation");
    }

    @Override
    public final Propagator<?>[] getPropagators() {
        throw new UnsupportedOperationException("The method is deprecated");
    }

    @Override
    public final Propagator<?> getPropagator(int idx) {
        throw new UnsupportedOperationException("The method is deprecated");
    }

    @Override
    public Stream<Propagator<?>> streamPropagators() {
        Spliterator<Propagator<?>> it = new Spliterator<Propagator<?>>() {

            int c = 0;
            int i = propagators[c].first;

            @Override
            public boolean tryAdvance(Consumer<? super Propagator<?>> action) {
                do {
                    if (i < propagators[c].last) {
                        action.accept(propagators[c].propagators[i++]);
                        return true;
                    } else {
                        c++;
                        if (c < propagators.length) {
                            i = propagators[c].first;
                        } else {
                            return false;
                        }
                    }
                } while (true);
            }

            @Override
            public Spliterator<Propagator<?>> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                return nbPropagators;
            }

            @Override
            public int characteristics() {
                return Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.NONNULL | Spliterator.CONCURRENT;
            }

        };
        return StreamSupport.stream(it, false);
    }

    @Override
    public final int getNbProps() {
        return nbPropagators;
    }

    @Override
    public final int[] getPIndices() {
        throw new UnsupportedOperationException("The method is deprecated");
    }

    @Override
    public final void setPIndice(int pos, int val) {
        //pindices[pos] = val;
        throw new UnsupportedOperationException("setPIndice to be implemented");
    }

    @Override
    @Deprecated
    public final int getDindex(int i) {
        throw new UnsupportedOperationException("The method is deprecated");
    }

    @Override
    public final int getIndexInPropagator(int pidx) {
        //return pindices[pidx];
        throw new UnsupportedOperationException("setPIndice to be implemented");
    }

    @Override
    public int instantiationWorldIndex() {
        return this.isInstantiated() ? this.instWI : Integer.MAX_VALUE;
    }

    @Override
    public void recordWorldIndex() {
        this.instWI = model.getEnvironment().getWorldIndex();
    }

    @Override
    public final String getName() {
        return this.name;
    }

    ////////////////////////////////////////////////////////////////
    ///// 	methodes 		de 	  l'interface 	  Variable	   /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void notifyPropagators(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        if (this.isInstantiated()) {
            recordWorldIndex();
        }
        model.getSolver().getEngine().onVariableUpdate(this, event, cause);
        notifyMonitors(event);
        notifyViews(event, cause);
    }

    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            //noinspection unchecked
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void notifyViews(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        if (cause == Cause.Null) {
            for (int i = vIdx - 1; i >= 0; i--) {
                views[i].notify(event, idxInViews[i]);
            }
        } else {
            for (int i = vIdx - 1; i >= 0; i--) {
                if (views[i] != cause) { // reference is enough
                    views[i].notify(event, idxInViews[i]);
                }
            }
        }
    }

    @Override
    public void addMonitor(IVariableMonitor<?> monitor) {
        // 1. check the non redundancy of a monitor if expected.
        if (model.getSettings().checkDeclaredMonitors()) {
            for (int i = 0; i < mIdx; i++) {
                if (monitors[i] == monitor) return;
            }
        }
        // 2. then add the monitor
        if (mIdx == monitors.length) {
            IVariableMonitor<?>[] tmp = monitors;
            monitors = new IVariableMonitor[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, monitors, 0, mIdx);
        }
        monitors[mIdx++] = monitor;
    }

    @Override
    public void removeMonitor(IVariableMonitor<?> monitor) {
        int i = mIdx - 1;
        for (; i >= 0; i--) {
            if (monitors[i] == monitor) break;
        }
        if (i < mIdx - 1) {
            System.arraycopy(monitors, i + 1, monitors, i, mIdx - (i + 1));
        }
        monitors[--mIdx] = null;
    }

    @Override
    public void subscribeView(IView<?> view, int idx) {
        if (vIdx == views.length) {
            IView<?>[] tmp = views;
            int[] tmpIdx = idxInViews;
            views = new IView[tmp.length * 3 / 2 + 1];
            idxInViews = new int[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, views, 0, vIdx);
            System.arraycopy(tmpIdx, 0, idxInViews, 0, vIdx);
        }
        views[vIdx] = view;
        idxInViews[vIdx] = idx;
        vIdx++;
    }

    @Override
    public final void contradiction(ICause cause, String message) throws ContradictionException {
        assert cause != null;
        model.getSolver().throwsException(cause, this, message);
    }

    public final Model getModel() {
        return model;
    }

    @Override
    public int getNbViews() {
        return vIdx;
    }

    @Override
    public IView<?> getView(int idx) {
        return views[idx];
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * @return <tt>true</tt> if this variable has a domain included in [0,1].
     */
    public final boolean isBool() {
        return (getTypeAndKind() & KIND) == BOOL;
    }

    /**
     * @return <tt>true</tt> if this variable has a singleton domain (different from instantiated)
     */
    public final boolean isAConstant() {
        return (getTypeAndKind() & TYPE) == CSTE;
    }

    /**
     * @return the event scheduler
     */
    public final EvtScheduler<?> getEvtScheduler() {
        return scheduler;
    }

    @Override
    public IntVar asIntVar() {
        return (IntVar) this;
    }

    @Override
    public BoolVar asBoolVar() {
        return (BoolVar) this;
    }

    @Override
    public RealVar asRealVar() {
        return (RealVar) this;
    }

    @Override
    public SetVar asSetVar() {
        return (SetVar) this;
    }

    @Override
    public void storeEvents(int m, ICause cause) {
        assert cause != null : "an event's cause is not supposed to be null";
        if (this.cause == null) {
            this.cause = cause;
        } else if (this.cause != cause) {
            this.cause = Cause.Null;
        }
        mask |= m;
    }

    @Override
    public void clearEvents() {
        this.cause = null;
        mask = 0;
        this.unschedule();
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public ICause getCause() {
        return cause;
    }

    private static class BipartiteList {
        /**
         * The current capacity
         */
        private int capacity;
        /**
         * The position of the first element (inclusive)
         */
        private int first;
        /**
         * The position of the last element (exclusive)
         */
        private int last;
        /**
         * The number of passive propagators, starting from first
         */
        private final IStateInt splitter;

        /**
         * List of propagators
         */
        Propagator<?>[] propagators;

        /**
         * Store the index of each propagator.
         */
        private int[] pindices;

        public BipartiteList(IEnvironment environment) {
            this.splitter = environment.makeInt(0);
            this.first = this.last = 0;
            this.capacity = 10;
            this.propagators = new Propagator[capacity];
            this.pindices = new int[capacity];
        }

        /**
         * Add a propagator <i>p</i> at the end of {@link #propagators}
         * and set at the same position in {@link #pindices} the position
         * of the variable in <i>p</i>.
         * @param propagator the propagator to add
         * @param idxInVar position of the variable in the propagator
         * @return the number of propagators stored
         */
        public int add(Propagator<?> propagator, int idxInVar) {
            if (first > 0 && splitter.get() == 0) {
                shiftTail();
            }
            if (last == capacity - 1) {
                capacity = capacity + (capacity >> 1);
                propagators = Arrays.copyOf(propagators, capacity);
                pindices = Arrays.copyOf(pindices, capacity);
            }
            propagators[last] = propagator;
            pindices[last++] = idxInVar;
            return last - 1;
        }

        /**
         * Remove the propagator <i>p</i> from {@link #propagators}.
         * @param propagator
         * @param idxInProp
         * @param var
         */
        public void remove(Propagator<?> propagator, int idxInProp, final AbstractVariable var) {
            int p = propagator.getVIndice(idxInProp);
            assert p > -1;
            assert propagators[p] == propagator : "Try to unlink from "+var.getName() +":\n" + propagator + "but found:\n" + propagators[p];
            assert propagators[p].getVar(idxInProp) == var;
            // Dynamic addition of a propagator may be not considered yet, so the assertion is not correct
            if (p < splitter.get()) {
                // swap the propagator to remove with the first one
                propagator.setVIndices(idxInProp, -1);
                propagators[p] = propagators[first];
                pindices[p] = pindices[first];
                propagators[p].setVIndices(pindices[p], p);
                propagators[first] = null;
                pindices[first] = 0;
                first++;
            } else {
                // swap the propagator to remove with the last one
                last--;
                if(p < last) {
                    propagators[p] = propagators[last];
                    pindices[p] = pindices[last];
                    propagators[p].setVIndices(pindices[p], p);
                }
                propagators[last] = null;
                pindices[last] = 0;
                propagator.setVIndices(idxInProp, -1);
            }
        }

        public void swap(Propagator<?> propagator, int idxInProp, final AbstractVariable var) {
            int p = propagator.getVIndice(idxInProp);
            assert p != -1;
            assert propagators[p] == propagator : "Try to swap from "+var.getName() +":\n" + propagator + "but found: " + propagators[p];
            assert propagators[p].getVar(idxInProp) == var;
            int pos = splitter.add(1) - 1;
            if (first > 0) {
                if (pos == 0) {
                    shiftTail();
                    //then recompute the position of this
                    p = propagator.getVIndice(idxInProp);
                } else {
                    // s = Math.min(s, first);
                    throw new UnsupportedOperationException();
                }
            }
            if(pos != p) {
                propagators[p] = propagators[pos];
                propagators[pos] = propagator;
                int pi = pindices[p];
                pindices[p] = pindices[pos];
                pindices[pos] = pi;
                propagators[p].setVIndices(pindices[p], p);
                propagators[pos].setVIndices(pindices[pos], pos);
                assert propagators[pos] == propagator;
            }
        }

        public void schedule(ICause cause, PropagationEngine engine, int mask) {
            int s = splitter.get();
            if (first > 0) {
                if (s == 0) {
                    shiftTail();
                } else {
                    throw new UnsupportedOperationException();
                }
            }
            for (int p = s; p < last; p++) {
                Propagator<?> prop = propagators[p];
                if (prop.isActive() && cause != prop) {
                    engine.schedule(prop, pindices[p], mask);
                }
            }
        }

        private void shiftTail() {
            for (int i = 0; i < last - first; i++) {
                propagators[i] = propagators[i + first];
                pindices[i] = pindices[i + first];
                propagators[i].setVIndices(pindices[i], i);
            }
            for (int i = last - first; i < last; i++) {
                propagators[i] = null;
                pindices[i] = 0;
            }
            last -= first;
            first = 0;
        }
    }

}
