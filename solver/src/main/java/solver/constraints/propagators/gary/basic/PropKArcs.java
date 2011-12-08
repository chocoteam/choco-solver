/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.propagators.gary.basic;

import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.memory.IStateInt;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;

/**
 * Propagator that ensures that K arcs belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public abstract class PropKArcs<V extends Variable, G extends GraphVar> extends GraphPropagator<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected G g;
    protected IntVar k;
    protected IStateInt nbInKer, nbInEnv;
    protected int n;
    protected IntProcedure arcEnforced;
    protected IntProcedure arcRemoved;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKArcs(G graph, Solver sol, Constraint<V, Propagator<V>> constraint, IntVar k) {
        super((V[]) new Variable[]{graph, k}, sol, constraint, PropagatorPriority.LINEAR, false);
        g = graph;
        this.k = k;
        n = g.getEnvelopGraph().getNbNodes();
        nbInEnv = environment.makeInt();
        nbInKer = environment.makeInt();
        arcEnforced = new EnfArc();
        arcRemoved = new RemArc();
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
        Variable var = vars[idxVarInProp];
        if (var.getType() == Variable.GRAPH) {
            if ((mask & EventType.ENFORCEARC.mask) != 0) {
                eventRecorder.getDeltaMonitor(g).forEach(arcEnforced, EventType.ENFORCEARC);
            }
            if ((mask & EventType.REMOVEARC.mask) != 0) {
                eventRecorder.getDeltaMonitor(g).forEach(arcRemoved, EventType.REMOVEARC);
            }

            k.updateLowerBound(nbInKer.get(), this, false);
            k.updateUpperBound(nbInEnv.get(), this, false);
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVEARC.mask + EventType.ENFORCEARC.mask + EventType.INSTANTIATE.mask;
    }

    //***********************************************************************************
    // PROCEDURES
    //***********************************************************************************

    private class EnfArc implements IntProcedure {
        @Override
        public void execute(int i) throws ContradictionException {
            int from = i / n - 1;
            int to = i % n;
            if (from == to) {
                nbInKer.set(nbInKer.get() + 1);
            }
        }
    }

    private class RemArc implements IntProcedure {
        @Override
        public void execute(int i) throws ContradictionException {
            int from = i / n - 1;
            int to = i % n;
            if (from == to) {
                nbInEnv.set(nbInEnv.get() - 1);
            }
        }
    }
}
