/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IVariableMonitor;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.view.IView;

import java.util.Arrays;

/**
 * Class used to factorise code
 * The subclass must implement Variable interface
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @revision CPRU: remove effectless procedures (before + on contradiction)
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
    protected final Solver solver; // Reference to the solver containing this variable.

    protected final String name;

    private Propagator[] propagators; // list of propagators of the variable
    private int[] pindices;    // index of the variable in the i^th propagator
    private int pIdx;

    private IView[] views; // views to inform of domain modification
    private int vIdx; // index of the last view not null in views -- not backtrable

    protected IVariableMonitor[] monitors; // monitors to inform of domain modification
    protected int mIdx; // index of the last view not null in views -- not backtrable

    protected int modificationEvents;

    //////////////////////////////////////////////////////////////////////////////////////

    protected AbstractVariable(String name, Solver solver) {
        this.name = name;
        this.solver = solver;
        views = new IView[2];
        monitors = new IVariableMonitor[2];
        propagators = new Propagator[8];
        pindices = new int[8];
        ID = solver.nextId();
        solver.associates(this);
    }

    @Override
    public int getId() {
        return ID;
    }

    @Override
    public int link(Propagator propagator, int idxInProp) {
        //ensure capacity
        if (pIdx == propagators.length) {
            Propagator[] tmp = propagators;
            propagators = new Propagator[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, propagators, 0, pIdx);

            int[] itmp = pindices;
            pindices = new int[itmp.length * 3 / 2 + 1];
            System.arraycopy(itmp, 0, pindices, 0, pIdx);

        }
        propagators[pIdx] = propagator;
        pindices[pIdx++] = idxInProp;
        return pIdx - 1;
    }

    @Override
    public void recordMask(int mask) {
        modificationEvents |= mask;
    }

    @Override
    public void unlink(Propagator propagator) {
        int i = 0;
        while (i < pIdx && propagators[i] != propagator) {
            i++;
        }
        // Dynamic addition of a propagator may be not considered yet, so the assertion is not correct
        if (i < pIdx) {
            propagators[i] = propagators[pIdx - 1];
            pindices[i] = pindices[--pIdx];
            propagators[pIdx] = null;
            pindices[pIdx] = 0;
        }
    }

    @Override
    public Propagator[] getPropagators() {
        if (propagators.length > pIdx) {
            propagators = Arrays.copyOf(propagators, pIdx);
        }
        return propagators;
    }

    @Override
    public Propagator getPropagator(int idx) {
        return propagators[idx];
    }

    @Override
    public int getNbProps() {
        return pIdx;
    }

    @Override
    public int[] getPIndices() {
        if (pindices.length > pIdx) {
            pindices = Arrays.copyOf(pindices, pIdx);
        }
        return pindices;
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

    @Override
    public boolean instantiated() {
        return isInstantiated();
    }

	public boolean isBool(){
		return (getTypeAndKind()&KIND)==BOOL;
	}
}
