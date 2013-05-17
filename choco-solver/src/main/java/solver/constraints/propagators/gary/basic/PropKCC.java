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

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.graphOperations.connectivity.ConnectivityFinder;
import util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that the final graph consists in K Connected Components (CC)
 * <p/>
 * simple checker (runs in linear time)
 *
 * @author Jean-Guillaume Fages
 */
public class PropKCC extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private GraphVar g;
    private IntVar k;
    private ConnectivityFinder env_CC_finder, ker_CC_finder;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKCC(GraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR);
        this.g = (GraphVar) vars[0];
        this.k = (IntVar) vars[1];
        env_CC_finder = new ConnectivityFinder(g.getEnvelopGraph());
        ker_CC_finder = new ConnectivityFinder(g.getKernelGraph());
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int maxOrder = g.getEnvelopOrder();
        if ((!g.isDirected()) && k.getUB() == 1 && maxOrder == g.getKernelOrder() && maxOrder > 1) {
            if (!env_CC_finder.isConnectedAndFindIsthma()) {
                contradiction(g, "");
            }
            int nbIsma = env_CC_finder.isthmusFrom.size();
            for (int i = 0; i < nbIsma; i++) {
                g.enforceArc(env_CC_finder.isthmusFrom.get(i), env_CC_finder.isthmusTo.get(i), aCause);
            }
        }
        if (maxOrder == g.getKernelOrder()) {
            env_CC_finder.findAllCC();
            int ee = env_CC_finder.getNBCC();
            k.updateLowerBound(ee, aCause);
            ker_CC_finder.findAllCC();
            int ke = ker_CC_finder.getNBCC();
            k.updateUpperBound(ke, aCause);
        } else {
            env_CC_finder.findAllCC();
            int ccs = env_CC_finder.getNBCC();
            ISet act = g.getKernelGraph().getActiveNodes();
            int minCC = 0;
            for (int cc = 0; cc < ccs; cc++) {
                for (int i = env_CC_finder.getCC_firstNode()[cc]; i >= 0; i = env_CC_finder.getCC_nextNode()[i]) {
					if (act.contain(i)) {
                        minCC++;
                        break;
                    }
                }
            }
            k.updateLowerBound(minCC, aCause);
            ker_CC_finder.findAllCC();
            int ke = ker_CC_finder.getNBCC();
            k.updateUpperBound(ke + maxOrder - g.getKernelOrder(), aCause);
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0); // todo incremental algorithm
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVENODE.mask + EventType.REMOVEARC.mask + EventType.ENFORCENODE.mask + EventType.ENFORCEARC.mask + EventType.INT_ALL_MASK();
    }

    @Override
    public ESat isEntailed() {
        env_CC_finder.findAllCC();
        int ee = env_CC_finder.getNBCC();
        if (k.getUB() < ee) {
            return ESat.FALSE;
        }
        if (g.instantiated()) {
            if (k.contains(ee)) {
                if (k.instantiated()) {
                    return ESat.TRUE;
                } else {
                    return ESat.UNDEFINED;
                }
            }
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }

}
