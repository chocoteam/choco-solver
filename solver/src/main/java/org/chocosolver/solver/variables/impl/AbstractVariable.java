/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.iterators.EvtScheduler;

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
    protected static final String MSG_INST = "already instantiated";

    /**
     * Default exception message.
     */
    static final String MSG_UNKNOWN = "unknown value";

    /**
     * Message associated with wrong upper bound exception.
     */
    static final String MSG_UPP = "new lower bound is greater than upper bound";

    /**
     * Message associated with wrong lower bound exception.
     */
    static final String MSG_LOW = "new upper bound is lesser than lower bound";

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
     * List of propagators of this variable.
     */
    protected Propagator[] propagators;

    /**
     * Store the index of this variable in each of its propagators.
     */
    int[] pindices;

    /**
     * Dependency indices, for efficient scheduling purpose.
     */
    private int[] dindices;
    /**
     * Nb dependencies
     */
    private int dsize;

    /**
     * List of views based on this variable.
     */
    private IView[] views;

    /**
     * Index of the last not null view in <code>views</code>.
     */
    private int vIdx;

    /**
     * List of monitors observing this variable.
     */
    protected IVariableMonitor[] monitors;

    /**
     * Index of the last not null monitor in <code>monitors</code>.
     */
    protected int mIdx;

    /**
     * The event scheduler of this variable, for efficient scheduling purpose. It stores propagators
     * wrt the propagation conditions.
     */
    private EvtScheduler scheduler;

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
        this.monitors = new IVariableMonitor[2];
        this.propagators = new Propagator[8];
        this.pindices = new int[8];
        this.ID = this.model.nextId();
        this.model.associates(this);
        this.scheduler = createScheduler();
        this.dsize = this.scheduler.select(0) + 1;
        this.dindices = new int[dsize + 1];
    }

    protected abstract EvtScheduler createScheduler();

    @Override
    public final int getId() {
        return ID;
    }

    @Override
    public final int link(Propagator propagator, int idxInProp) {
        // 1. ensure capacity
        if (dindices[dsize] == propagators.length) {
            Propagator[] tmp = propagators;
            propagators = new Propagator[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, propagators, 0, dindices[dsize]);

            int[] itmp = pindices;
            pindices = new int[itmp.length * 3 / 2 + 1];
            System.arraycopy(itmp, 0, pindices, 0, dindices[dsize]);
            if (pindices.length != propagators.length) {
                throw new UnsupportedOperationException("error: pindices.length != propagators.length in " + this);
            }

        }
        // 2. put it in the right place
        int pc = propagator.getPropagationConditions(idxInProp);
        return subscribe(propagator, idxInProp, scheduler.select(pc));
    }

    /**
     * Move a propagator from position 'from' to position 'to' in {@link #propagators}. The element
     * at position 'from' will then be 'null'.
     *
     * @param from a position in {@link #propagators}
     * @param to   a position in {@link #propagators}
     */
    private void move(int from, int to) {
        if (propagators[from] != null) {
            propagators[to] = propagators[from];
            pindices[to] = pindices[from];
            propagators[to].setVIndices(pindices[from], to);
            propagators[from] = null;
        }
    }

    int subscribe(Propagator p, int ip, int i) {
        int j = dsize - 1;
        for (; j >= i; j--) {
            move(dindices[j], dindices[j + 1]);
            dindices[j + 1]++;
        }
        propagators[dindices[i]] = p;
        pindices[dindices[i]] = ip;
        return dindices[i];
    }

    @Override
    public int swapOnPassivate(Propagator propagator, int idxInProp) {
        int pp = propagator.getVIndice(idxInProp);
        assert pp != -1;
        int i = scheduler.select(propagator.getPropagationConditions(idxInProp));
        move(dindices[i + 1] - 1, pp);
        for (int j = i + 1; j < dsize; j++) {
            move(dindices[j + 1] - 1, dindices[j] - 1);
            dindices[j]--;
        }
        propagators[dindices[dsize] - 1] = propagator;
        pindices[dindices[dsize] - 1] = idxInProp;
        return dindices[dsize] - 1;
    }

    @Override
    public int swapOnActivate(Propagator propagator, int idxInProp) {
        int pp = propagator.getVIndice(idxInProp);
        if(pp > -1) {
            assert propagators[pp] == propagator;
            assert dindices[dsize - 1] <= pp && pp <= dindices[dsize];
            int i = scheduler.select(propagator.getPropagationConditions(idxInProp));
            move(--dindices[dsize], pp);
            for (int j = dsize - 1; j >= i; j--) {
                move(dindices[j], dindices[j + 1]);
                dindices[j + 1]++;
            }
            propagators[dindices[i]] = propagator;
            pindices[dindices[i]] = idxInProp;
            return dindices[i];
        }else{
            assert propagator.getConstraint().getStatus() == Constraint.Status.FREE;
            return -1;
        }
    }

    @Override
    public final void unlink(Propagator propagator, int idxInProp) {
        int i = propagator.getVIndice(idxInProp); // todo deal with -1
        if (i > -1) {
            assert propagators[i] == propagator : "Try to unlink :\n" + propagator + "\nfrom " + this.getName() + " but found:\n" + propagators[i];
            // Dynamic addition of a propagator may be not considered yet, so the assertion is not correct
            int mask = 0;
            if(i < dindices[dsize-1]){ // the position above this limit indicates a propagator that was swapped to passivate
                mask = propagator.getPropagationConditions(pindices[i]);
            }
            cancel(i, scheduler.select(mask));
            propagator.setVIndices(idxInProp, -1);
        }
    }

    void cancel(int pp, int i) {
        // start moving the other ones
        move(dindices[i + 1] - 1, pp);
        for (int j = i + 1; j < dsize; j++) {
            move(dindices[j + 1] - 1, dindices[j] - 1);
            dindices[j]--;
        }
        dindices[dsize]--;
    }

    @Override
    public final Propagator[] getPropagators() {
        return propagators;
    }

    @Override
    public final Propagator getPropagator(int idx) {
        return propagators[idx];
    }

    @Override
    public final int getNbProps() {
        return dindices[dsize];
    }

    @Override
    public final int[] getPIndices() {
        return pindices;
    }

    @Override
    public final void setPIndice(int pos, int val) {
        pindices[pos] = val;
    }

    @Override
    public final int getDindex(int i) {
        return dindices[i];
    }

    @Override
    public final int getIndexInPropagator(int pidx) {
        return pindices[pidx];
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
        model.getSolver().getEngine().onVariableUpdate(this, event, cause);
        notifyMonitors(event);
        notifyViews(event, cause);
    }

    public void notifyMonitors(IEventType event) throws ContradictionException {
        for (int i = mIdx - 1; i >= 0; i--) {
            monitors[i].onUpdate(this, event);
        }
    }

    @Override
    public void notifyViews(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        if (cause == Cause.Null) {
            for (int i = vIdx - 1; i >= 0; i--) {
                views[i].notify(event);
            }
        } else {
            for (int i = vIdx - 1; i >= 0; i--) {
                if (views[i] != cause) { // reference is enough
                    views[i].notify(event);
                }
            }
        }
    }

    @Override
    public void addMonitor(IVariableMonitor monitor) {
        // 1. check the non redundancy of a monitor
        for (int i = 0; i < mIdx; i++) {
            if (monitors[i] == monitor) return;
        }
        // 2. then add the monitor
        if (mIdx == monitors.length) {
            IVariableMonitor[] tmp = monitors;
            monitors = new IVariableMonitor[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, monitors, 0, mIdx);
        }
        monitors[mIdx++] = monitor;
    }

    @Override
    public void removeMonitor(IVariableMonitor monitor) {
        int i = mIdx - 1;
        for (; i >= 0 ; i--) {
            if (monitors[i] == monitor) break;
        }
        if(i< mIdx-1) {
            System.arraycopy(monitors, i + 1, monitors, i, mIdx - (i+1));
        }
        monitors[--mIdx] = null;
    }

    @Override
    public void subscribeView(IView view) {
        if (vIdx == views.length) {
            IView[] tmp = views;
            views = new IView[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, views, 0, vIdx);
        }
        views[vIdx++] = view;
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
    public int getNbViews(){
        return vIdx;
    }

    @Override
    public IView getView(int idx) {
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
    @SuppressWarnings("unchecked")
    public final EvtScheduler getEvtScheduler() {
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
        assert cause != null:"an event's cause is not supposed to be null";
        if(this.cause == null){
            this.cause = cause;
        }else if (this.cause != cause){
            this.cause = Cause.Null;
        }
        mask |= m;
    }

    @Override
    public void clearEvents() {
        this.cause = null;
        mask = 0;
    }

    @Override
    public int getMask() {
        return mask;
    }

    @Override
    public ICause getCause() {
        return cause;
    }

}
