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

package solver.constraints.propagators.gary.constraintSpecific;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.TIntIntHashMap;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.requests.IRequest;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.undirectedGraph.UndirectedGraphVar;

/**
 * This class enables to manage bounded integer variables in a graph context
 * It maintains the bounds of integers variables
 *
 * @author Jean-Guillaume Fages
 * @param <V>
 */
public class PropGraphAllDiffBC<V extends Variable> extends GraphPropagator<V> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private UndirectedGraphVar g;
    private IntVar[] intVars;
    private TIntIntHashMap valuesHash;
    private IntProcedure valRemoved;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropGraphAllDiffBC(IntVar[] vars, UndirectedGraphVar graph, Solver solver, Constraint mixtedAllDiff, PropagatorPriority storeThreshold, TIntIntHashMap vH) {
        super((V[]) ArrayUtils.append(vars, new Variable[]{graph}), solver, mixtedAllDiff, storeThreshold, false);
        g = graph;
        intVars = vars;
        this.valuesHash = vH;
        final PropGraphAllDiffBC instance = this;
        valRemoved = new IntProcedure() {
            public void execute(int i) throws ContradictionException {
                int lb = intVars[i].getLB();
                while (!g.getEnvelopGraph().edgeExists(i, valuesHash.get(lb))) {
                    intVars[i].removeValue(lb, instance, false);
                    lb = intVars[i].getLB();
                }
                int ub = intVars[i].getUB();
                while (!g.getEnvelopGraph().edgeExists(i, valuesHash.get(ub))) {
                    intVars[i].removeValue(ub, instance, false);
                    ub = intVars[i].getUB();
                }
            }
        };
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // BEWARE the graph is created from the variables so it is initially correct (true for a standard use)
    }

    @Override
    public void propagateOnRequest(IRequest<V> request, int idxVarInProp, int mask) throws ContradictionException {
        valRemoved.execute(idxVarInProp);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.INCLOW.mask + EventType.DECUPP.mask;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
