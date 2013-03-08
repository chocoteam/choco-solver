/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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

package solver.variables;

import solver.Cause;
import solver.ICause;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.delta.IDelta;
import solver.variables.view.IView;

import java.io.Serializable;
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
public abstract class AbstractVariable<D extends IDelta, V extends Variable<D>>
        implements Serializable {

    private static final long serialVersionUID = 1L;
    public static final String
            MSG_REMOVE = "remove last value";
    public static final String MSG_EMPTY = "empty domain";
    public static final String MSG_INST = "already instantiated";
    public static final String MSG_UNKNOWN = "unknown value";
    public static final String MSG_UPP = "new lower bound is greater than upper bound";
    public static final String MSG_LOW = "new upper bound is lesser than lower bound";
    public static final String MSG_BOUND = "new bounds are incorrect";
    protected static final String NO_NAME = "";


    private final int ID; // unique id of this
    protected final Solver solver; // Reference to the solver containing this variable.

    protected Constraint[] constraints = new Constraint[8];
    private int cLast = 0;

    protected final String name;

    private Propagator[] propagators; // list of propagators of the variable
    private int[] pindices;    // index of the variable in the i^th propagator
    private int pIdx;

    private IView[] views; // views to inform of domain modification
    private int vIdx; // index of the last view not null in views -- not backtrable

    protected IVariableMonitor<V>[] monitors; // monitors to inform of domain modification
    protected int mIdx; // index of the last view not null in views -- not backtrable

    protected int modificationEvents;

    //////////////////////////////////////////////////////////////////////////////////////

    protected AbstractVariable(Solver solver) {
        this(NO_NAME, solver);
    }

    protected AbstractVariable(String name, Solver solver) {
        this.name = name;
        this.solver = solver;
        views = new IView[2];
        monitors = (IVariableMonitor<V>[]) new IVariableMonitor[2];
        propagators = new Propagator[8];
        pindices = new int[8];
        ID = solver.nextId();
    }

    public int getId() {
        return ID;
    }

    /**
     * Returns the array of constraints <code>this</code> appears in.
     *
     * @return array of constraints
     */
    public Constraint[] getConstraints() {
        if (cLast < constraints.length) {
            constraints = Arrays.copyOfRange(constraints, 0, cLast);
        }
        return constraints;
    }

    /**
     * Link a constraint within a variable
     *
     * @param constraint a constraint
     */
    public void declareIn(Constraint constraint) {
        if (cLast >= constraints.length) {
            Constraint[] tmp = constraints;
            constraints = new Constraint[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, constraints, 0, cLast);
        }
        constraints[cLast++] = constraint;
    }

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

    public void recordMask(int mask) {
        modificationEvents |= mask;
    }

    public void unlink(Propagator propagator, int idxInProp) {
//        int i = 0;
//        while (i < pIdx && propagators[i] != propagator) {
//            i++;
//        }
//        assert idxInProp == i: "wrong index";
//        assert i < pIdx : "remove unknown propagator";
        assert idxInProp < pIdx : "remove unknown propagator";
        // swap it with the last one
        pIdx--;
        if (pIdx > idxInProp) {
            // swap with the pidx^th propagator
            propagators[idxInProp] = propagators[pIdx];
            assert propagators[idxInProp].getVar(pindices[pIdx]).getId() == this.ID;
            propagators[idxInProp].setVIndices(pindices[pIdx], idxInProp);
            pindices[idxInProp] = pindices[pIdx];
        }
        propagators[pIdx] = null;
        pindices[pIdx] = 0;

    }

    public Propagator[] getPropagators() {
        if (propagators.length > pIdx) {
            propagators = Arrays.copyOf(propagators, pIdx);
        }
        return propagators;
    }

    public Propagator getPropagator(int idx) {
        return propagators[idx];
    }

    public int getNbProps() {
        return pIdx;
    }

    public int[] getPIndices() {
        if (pindices.length > pIdx) {
            pindices = Arrays.copyOf(pindices, pIdx);
        }
        return pindices;
    }

    public int getIndiceInPropagator(int pidx) {
        return pindices[pidx];
    }

    public String getName() {
        return this.name;
    }

    ////////////////////////////////////////////////////////////////
    ///// 	methodes 		de 	  l'interface 	  Variable	   /////
    ////////////////////////////////////////////////////////////////

    public void notifyViews(EventType event, ICause cause) throws ContradictionException {
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

    public void addMonitor(IVariableMonitor monitor) {
        // 1. check the non redundancy of a monitor
        for (int i = 0; i < mIdx; i++) {
            if (monitors[i] == monitor) return;
        }
        // 2. then add the monitor
        if (mIdx == monitors.length) {
            IVariableMonitor<V>[] tmp = monitors;
            monitors = (IVariableMonitor<V>[]) new IVariableMonitor[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, monitors, 0, mIdx);
        }
        monitors[mIdx++] = monitor;
    }

    public void removeMonitor(IVariableMonitor monitor) {
        throw new UnsupportedOperationException("not yet implemented");
    }

    public void subscribeView(IView view) {
        if (vIdx == views.length) {
            IView[] tmp = views;
            views = new IView[tmp.length * 3 / 2 + 1];
            System.arraycopy(tmp, 0, views, 0, vIdx);
        }
        views[vIdx++] = view;
    }

    public int nbConstraints() {
        return pIdx;
    }

    public Solver getSolver() {
        return solver;
    }

    public IView[] getViews() {
        return Arrays.copyOfRange(views, 0, vIdx);
    }

    public int compareTo(Variable o) {
        return this.getId() - o.getId();
    }
}
