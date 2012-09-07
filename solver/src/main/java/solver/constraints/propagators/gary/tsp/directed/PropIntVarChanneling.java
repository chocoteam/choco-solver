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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 03/10/11
 * Time: 19:56
 */

package solver.constraints.propagators.gary.tsp.directed;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.procedure.PairProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IIntDeltaMonitor;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraphVar;

public class PropIntVarChanneling extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    DirectedGraphVar g;
    GraphDeltaMonitor gdm;
    int n;
    IntVar[] intVars;
    protected final IIntDeltaMonitor[] idms;
    private int varIdx;
    private PairProcedure arcEnforced, arcRemoved;
    private IntProcedure valRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Links intVars and the graph
     * arc (x,y)=var[x]=y
     * values outside range [0,n-1] are not considered
     *
     * @param intVars
     * @param graph
     * @param constraint
     * @param solver
     */
    public PropIntVarChanneling(IntVar[] intVars, DirectedGraphVar graph, Constraint constraint, Solver solver) {
        super(ArrayUtils.append(intVars, new Variable[]{graph}), solver, constraint, PropagatorPriority.LINEAR);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.intVars = intVars;
        this.idms = new IIntDeltaMonitor[intVars.length];
        for (int i = 0; i < intVars.length; i++) {
            idms[i] = intVars[i].monitorDelta(this);
        }
        this.n = g.getEnvelopGraph().getNbNodes();
        valRemoved = new ValRem(this);
        arcEnforced = new EnfArc(this);
        if (intVars[0].hasEnumeratedDomain()) {
            arcRemoved = new RemArcAC(this);
        } else {
            arcRemoved = new RemArcBC(this);
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        INeighbors nei;
        IntVar v;
        for (int i = 0; i < n; i++) {
            nei = g.getEnvelopGraph().getSuccessorsOf(i);
            for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                if (!intVars[i].contains(j)) {
                    g.removeArc(i, j, aCause);
                }
            }
            v = intVars[i];
            int ub = v.getUB();
            for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
                if (j < n && !g.getEnvelopGraph().arcExists(i, j)) {
                    v.removeValue(j, aCause);
                }
            }
            if (!v.hasEnumeratedDomain()) {
                ub = v.getUB();
                while (ub >= 0 && ub < n && !g.getEnvelopGraph().arcExists(i, ub)) {
                    v.removeValue(ub, aCause);
                    ub--;
                }
            }
        }
        gdm.unfreeze();
        for (int i = 0; i < idms.length; i++) {
            idms[i].unfreeze();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if ((vars[idxVarInProp].getTypeAndKind() & Variable.GRAPH) != 0) {
            gdm.freeze();
            if ((mask & EventType.ENFORCEARC.mask) != 0) {
                gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
            }
            if ((mask & EventType.REMOVEARC.mask) != 0) {
                gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
            }
            gdm.unfreeze();
        } else {
            varIdx = idxVarInProp;
            int val = intVars[varIdx].getLB();
            if ((mask & EventType.INSTANTIATE.mask) != 0 && val < n) {
                g.enforceArc(varIdx, val, aCause);
            }
            idms[varIdx].freeze();
            idms[idxVarInProp].forEach(valRemoved, EventType.REMOVE);
            idms[varIdx].unfreeze();
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        for (int i = 0; i < vars.length; i++) {
            if (!vars[i].instantiated()) {
                return ESat.UNDEFINED;
            }
        }
        int val;
        for (int i = 0; i < n; i++) {
            val = intVars[i].getValue();
            if (val < n && !g.getEnvelopGraph().arcExists(i, val)) {
                return ESat.FALSE;
            }
            if (g.getEnvelopGraph().getSuccessorsOf(i).neighborhoodSize() > 1) {
                return ESat.FALSE;
            }
        }
        return ESat.TRUE;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class ValRem implements IntProcedure {
        private Propagator p;

        private ValRem(Propagator p) {
            this.p = p;
        }

        @Override
        public void execute(int i) throws ContradictionException {
            g.removeArc(varIdx, i, p);
        }
    }

    private class EnfArc implements PairProcedure {
        private Propagator p;

        private EnfArc(Propagator p) {
            this.p = p;
        }

        @Override
        public void execute(int from, int to) throws ContradictionException {
            intVars[from].instantiateTo(to, p);
        }
    }

    private class RemArcAC implements PairProcedure {
        private Propagator p;

        private RemArcAC(Propagator p) {
            this.p = p;
        }

        @Override
        public void execute(int from, int to) throws ContradictionException {
            intVars[from].removeValue(to, p);
        }
    }

    private class RemArcBC implements PairProcedure {
        private Propagator p;

        private RemArcBC(Propagator p) {
            this.p = p;
        }

        @Override
        public void execute(int from, int to) throws ContradictionException {
            if (to == intVars[from].getLB()) {
                while (to < n && !g.getEnvelopGraph().arcExists(from, to)) {
                    to++;
                }
                intVars[from].updateLowerBound(to, p);
            } else if (to == intVars[from].getUB()) {
                while (to >= 0 && !g.getEnvelopGraph().arcExists(from, to)) {
                    to--;
                }
                intVars[from].updateUpperBound(to, p);
            }
        }
    }
}
