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

package solver.constraints.propagators.gary.basic;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.PairProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.monitor.GraphDeltaMonitor;
import solver.variables.graph.GraphVar;
import solver.variables.setDataStructures.ISet;

/**
 * Propagator that ensures that K arcs/edges belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropKArcs extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected GraphVar g;
    GraphDeltaMonitor gdm;
    protected IntVar k;
    protected IStateInt nbInKer, nbInEnv;
    protected PairProcedure arcEnforced, arcRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKArcs(GraphVar graph, IntVar k, Constraint constraint, Solver sol) {
        super(new Variable[]{graph, k}, sol, constraint, PropagatorPriority.LINEAR);
        g = graph;
        gdm = (GraphDeltaMonitor) g.monitorDelta(this);
        this.k = k;
        nbInEnv = environment.makeInt();
        nbInKer = environment.makeInt();
        arcEnforced = new EnfArc();
        arcRemoved = new RemArc();
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int nbK = 0;
        int nbE = 0;
        ISet env = g.getEnvelopGraph().getActiveNodes();
        for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
            nbE += g.getEnvelopGraph().getSuccessorsOf(i).getSize();
            nbK += g.getKernelGraph().getSuccessorsOf(i).getSize();
        }
        if (!g.isDirected()) {
            nbK /= 2;
            nbE /= 2;
        }
        nbInKer.set(nbK);
        nbInEnv.set(nbE);
        filter(nbK, nbE);
        gdm.unfreeze();
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        gdm.freeze();
        if ((mask & EventType.ENFORCEARC.mask) != 0) {
            gdm.forEachArc(arcEnforced, EventType.ENFORCEARC);
        }
        if ((mask & EventType.REMOVEARC.mask) != 0) {
            gdm.forEachArc(arcRemoved, EventType.REMOVEARC);
        }
        gdm.unfreeze();
        filter(nbInKer.get(), nbInEnv.get());
    }

    private void filter(int nbK, int nbE) throws ContradictionException {
        k.updateLowerBound(nbK, aCause);
        k.updateUpperBound(nbE, aCause);
        if (nbK != nbE && k.instantiated()) {
            ISet nei;
            ISet env = g.getEnvelopGraph().getActiveNodes();
            if (k.getValue() == nbE) {
                for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
                    nei = g.getEnvelopGraph().getSuccessorsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        g.enforceArc(i, j, aCause);
                    }
                }
                nbInKer.set(nbE);
            }
            if (k.getValue() == nbK) {
                ISet neiKer;
                for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
                    nei = g.getEnvelopGraph().getSuccessorsOf(i);
                    neiKer = g.getKernelGraph().getSuccessorsOf(i);
                    for (int j = nei.getFirstElement(); j >= 0; j = nei.getNextElement()) {
                        if (!neiKer.contain(j)) {
                            g.removeArc(i, j, aCause);
                        }
                    }
                }
                nbInEnv.set(nbK);
            }
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask
                + EventType.INCLOW.mask + EventType.DECUPP.mask + EventType.INSTANTIATE.mask;
    }

    @Override
    public ESat isEntailed() {
        int nbK = 0;
        int nbE = 0;
        ISet env = g.getEnvelopGraph().getActiveNodes();
        for (int i = env.getFirstElement(); i >= 0; i = env.getNextElement()) {
            nbE += g.getEnvelopGraph().getSuccessorsOf(i).getSize();
            nbK += g.getKernelGraph().getSuccessorsOf(i).getSize();
        }
        if (!g.isDirected()) {
            nbK /= 2;
            nbE /= 2;
        }
        if (nbK > k.getUB() || nbE < k.getLB()) {
            return ESat.FALSE;
        }
        if (k.instantiated() && g.instantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class EnfArc implements PairProcedure {
        @Override
        public void execute(int i, int j) throws ContradictionException {
            nbInKer.add(1);
        }
    }

    private class RemArc implements PairProcedure {
        @Override
        public void execute(int i, int j) throws ContradictionException {
            nbInEnv.add(1);
        }
    }
}
