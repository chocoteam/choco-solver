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

package solver.constraints.gary.basic;

import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.graph.GraphVar;
import util.ESat;
import util.objects.setDataStructures.ISet;

/**
 * Propagator that ensures that K nodes belong to the final graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropKNodes extends Propagator {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private GraphVar g;
    private IntVar k;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropKNodes(GraphVar graph, IntVar k) {
        super(new Variable[]{graph, k}, PropagatorPriority.LINEAR, true);
        this.g = (GraphVar) vars[0];
        this.k = (IntVar) vars[1];
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int env = g.getEnvelopGraph().getActiveNodes().getSize();
        int ker = g.getKernelGraph().getActiveNodes().getSize();
        k.updateLowerBound(ker, aCause);
        k.updateUpperBound(env, aCause);
        if (ker == env) {
            setPassive();
        } else if (k.instantiated()) {
            int v = k.getValue();
            ISet envNodes = g.getEnvelopGraph().getActiveNodes();
            if (v == env) {
                for (int i = envNodes.getFirstElement(); i >= 0; i = envNodes.getNextElement()) {
                    g.enforceNode(i, aCause);
                }
                setPassive();
            } else if (v == ker) {
                ISet kerNodes = g.getKernelGraph().getActiveNodes();
                for (int i = envNodes.getFirstElement(); i >= 0; i = envNodes.getNextElement()) {
                    if (!kerNodes.contain(i)) {
                        g.removeNode(i, aCause);
                    }
                }
                setPassive();
            }
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        propagate(0);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVENODE.mask + EventType.ENFORCENODE.mask + EventType.INSTANTIATE.mask + EventType.INCLOW.mask + EventType.DECUPP.mask;
    }

    @Override
    public ESat isEntailed() {
        int env = g.getEnvelopGraph().getActiveNodes().getSize();
        int ker = g.getKernelGraph().getActiveNodes().getSize();
        if (env < k.getLB() || ker > k.getUB()) {
            return ESat.FALSE;
        }
        if (env == ker) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
