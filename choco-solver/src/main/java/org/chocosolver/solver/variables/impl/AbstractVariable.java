/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.ISolver;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.impl.scheduler.BoolEvtScheduler;
import org.chocosolver.solver.variables.impl.scheduler.IntEvtScheduler;
import org.chocosolver.solver.variables.impl.scheduler.RealEvtScheduler;
import org.chocosolver.solver.variables.impl.scheduler.SetEvtScheduler;
import org.chocosolver.solver.variables.view.IView;
import org.chocosolver.util.iterators.EvtScheduler;

import java.util.Arrays;

/**
 * Class used to factorise code
 * The subclass must implement Variable interface
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @author Charles Prud'homme
 * @since 30 june 2011
 */
public abstract class AbstractVariable implements Variable {

    private static final long serialVersionUID = 1L;
    public static final String MSG_REMOVE = "remove last value";
    public static final String MSG_EMPTY = "empty domain";
    public static final String MSG_INST = "already instantiated";
    public static final String MSG_UNKNOWN = "unknown value";
    public static final String MSG_UPP = "new lower bound is greater than upper bound";
    public static final String MSG_LOW = "new upper bound is lesser than lower bound";
    public static final String MSG_BOUND = "new bounds are incorrect";

    private final int ID; // unique id of this
    protected final ISolver isolver; // Reference to the solver containing this variable.
    protected final Solver solver; // Reference to the solver containing this variable.

    protected final String name;

    private Propagator[] propagators; // list of propagators of the variable
    private int[] pindices;    // index of the variable in the i^th propagator
    private int[] dindices; // dependency indices -- for scheduling
    private int nbPropagators;

    private IView[] views; // views to inform of domain modification
    private int vIdx; // index of the last view not null in views -- not backtrable

    protected IVariableMonitor[] monitors; // monitors to inform of domain modification
    protected int mIdx; // index of the last view not null in views -- not backtrable

    protected int modificationEvents;

    protected final boolean _plugexpl;

    private EvtScheduler scheduler;

    //////////////////////////////////////////////////////////////////////////////////////

    protected AbstractVariable(String name, ISolver isolver) {
        this.name = name;
        this.isolver = isolver;
        this.solver = isolver._fes_();
        this.views = new IView[2];
        this.monitors = new IVariableMonitor[2];
        this.propagators = new Propagator[8];
        this.pindices = new int[8];
        this.dindices = new int[6];
        this.ID = this.solver.nextId();
        this._plugexpl = this.solver.getSettings().plugExplanationIn();
        this.isolver.associates(this);
        int kind = getTypeAndKind() & Variable.KIND;
        switch (kind) {
            case Variable.BOOL:
                this.scheduler = new BoolEvtScheduler();
                break;
            case Variable.INT:
                this.scheduler = new IntEvtScheduler();
                break;
            case Variable.REAL:
                this.scheduler = new RealEvtScheduler();
                break;
            case Variable.SET:
                this.scheduler = new SetEvtScheduler();
                break;
        }
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public int link(Propagator propagator, int idxInProp) {
        // 1. ensure capacity
        if (nbPropagators == propagators.length) {
            Propagator[] tmp = propagators;
            propagators = new Propagator[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, propagators, 0, nbPropagators);

            int[] itmp = pindices;
            pindices = new int[itmp.length * 3 / 2 + 1];
            System.arraycopy(itmp, 0, pindices, 0, nbPropagators);

        }
        // 2. put it in the right place
        subscribe(propagator, idxInProp, scheduler.select(propagator.getPropagationConditions(idxInProp)));
        return nbPropagators++;
    }


    private void subscribe(Propagator p, int ip, int i) {
        for (int j = 4; j >= i; j--) {
            propagators[dindices[j + 1]] = propagators[dindices[j]];
            pindices[dindices[j + 1]] = pindices[dindices[j]];
            dindices[j + 1] = dindices[j + 1] + 1;
        }
        propagators[dindices[i]] = p;
        pindices[dindices[i]] = ip;
    }


    @Override
    public void recordMask(int mask) {
        modificationEvents |= mask;
    }

    @Override
    public void unlink(Propagator propagator) {
        int i = 0;
        while (i < nbPropagators && propagators[i] != propagator) {
            i++;
        }
        // Dynamic addition of a propagator may be not considered yet, so the assertion is not correct
        if (i < nbPropagators) {
            cancel(i, scheduler.select(propagator.getPropagationConditions(pindices[i])));
            nbPropagators--;
        }
    }

    private void cancel(int pp, int i) {
        propagators[pp] = propagators[dindices[i + 1] - 1];
        for (int k = i + 1; k < 5; k++) {
            propagators[dindices[k] - 1] = propagators[dindices[k + 1] - 1];
            pindices[dindices[k] - 1] = pindices[dindices[k + 1] - 1];
            dindices[k] = dindices[k] - 1;
        }
        propagators[nbPropagators - 1] = null;
        pindices[nbPropagators - 1] = -1;
        dindices[5] = dindices[5] - 1;
    }

    @Override
    public Propagator[] getPropagators() {
        if (propagators.length > nbPropagators) {
            propagators = Arrays.copyOf(propagators, nbPropagators);
        }
        return propagators;
    }

    @Override
    public Propagator getPropagator(int idx) {
        return propagators[idx];
    }

    @Override
    public int getNbProps() {
        return nbPropagators;
    }

    @Override
    public int[] getPIndices() {
        if (pindices.length > nbPropagators) {
            pindices = Arrays.copyOf(pindices, nbPropagators);
        }
        return pindices;
    }

    @Override
    public int getDindex(int i) {
        return dindices[i];
    }

    @Override
    public int getIndexInPropagator(int pidx) {
        return pindices[pidx];
    }

    @Override
    public String getName() {
        return this.name;
    }

    ////////////////////////////////////////////////////////////////
    ///// 	methodes 		de 	  l'interface 	  Variable	   /////
    ////////////////////////////////////////////////////////////////

    @Override
    public void notifyPropagators(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        notifyMonitors(event);
        if ((modificationEvents & event.getMask()) != 0) {
            solver.getEngine().onVariableUpdate(this, event, cause);
        }
        notifyViews(event, cause);
    }

    @Override
    public void notifyViews(IEventType event, ICause cause) throws ContradictionException {
        assert cause != null;
        if (cause == Cause.Null) {
            for (int i = vIdx - 1; i >= 0; i--) {
                views[i].transformEvent(event, cause);
            }
        } else {
            for (int i = vIdx - 1; i >= 0; i--) {
                if (views[i] != cause) { // reference is enough
                    views[i].transformEvent(event, cause);
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
        throw new UnsupportedOperationException("not yet implemented");
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
    public Solver getSolver() {
        return solver;
    }

    @Override
    public ISolver _bes_() {
        return isolver;
    }

    @Override
    public IView[] getViews() {
        return Arrays.copyOfRange(views, 0, vIdx);
    }

    @Override
    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }

    @Override
    public String toString() {
        return getName();
    }

    public boolean isBool() {
        return (getTypeAndKind() & KIND) == BOOL;
    }

    public EvtScheduler _schedIter() {
        return scheduler;
    }

    public void _setschedIter(EvtScheduler siter) {
        this.scheduler = siter;
    }

}
