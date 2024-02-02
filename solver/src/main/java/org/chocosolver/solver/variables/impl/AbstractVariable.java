/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

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
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;
import java.util.Spliterator;
import java.util.function.BiConsumer;
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
    final IBipartiteList[] propagators;
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
        this.views = null;
        this.idxInViews = null;
        this.monitors = null;
        this.scheduler = createScheduler();
        int dsize = this.scheduler.select(0);
        this.propagators = new IBipartiteList[dsize + 1];
        for (int i = 0; i < dsize + 1; i++) {
            this.propagators[i] = IBipartiteList.empty();
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
        if (propagators[i] == IBipartiteList.EMPTY) {
            propagators[i] = new BipartiteList(model.getEnvironment());
        }
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
    public Stream<Propagator<?>> streamPropagators() {
        //noinspection Convert2Diamond
        Spliterator<Propagator<?>> it = new Spliterator<Propagator<?>>() {

            int c = 0;
            int i = propagators[c].getFirst();

            @Override
            public boolean tryAdvance(Consumer<? super Propagator<?>> action) {
                do {
                    if (i < propagators[c].getLast()) {
                        action.accept(propagators[c].get(i++));
                        return true;
                    } else {
                        c++;
                        if (c < propagators.length) {
                            i = propagators[c].getFirst();
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
    public void forEachPropagator(BiConsumer<Variable, Propagator<?>> action) {
        int c = 0;
        int i = propagators[c].getFirst();
        do {
            if (i < propagators[c].getLast()) {
                action.accept(this, propagators[c].get(i++));
            } else {
                c++;
                if (c < propagators.length) {
                    i = propagators[c].getFirst();
                }
            }
        } while (c < propagators.length);
    }

    @Override
    public final int getNbProps() {
        return nbPropagators;
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
        if(monitors == null){
            this.monitors = new IVariableMonitor[1];
        }
        // 1. check the non redundancy of a monitor if expected.
        if (model.getSettings().checkDeclaredMonitors()) {
            for (int i = 0; i < mIdx; i++) {
                if (monitors[i] == monitor) return;
            }
        }
        // 2. then add the monitor
        if (mIdx == monitors.length) {
            monitors = Arrays.copyOf(monitors, ArrayUtils.newBoundedSize(monitors.length, 16));
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
        if (views == null) {
            this.views = new IView[1];
            this.idxInViews = new int[1];
        }
        if (vIdx == views.length) {
            views = Arrays.copyOf(views, ArrayUtils.newBoundedSize(views.length, 16));
            idxInViews = Arrays.copyOf(idxInViews, ArrayUtils.newBoundedSize(idxInViews.length, 16));
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AbstractVariable)) return false;
        AbstractVariable that = (AbstractVariable) o;
        return ID == that.ID;
    }

    @Override
    public int hashCode() {
        return ID;
    }
}
