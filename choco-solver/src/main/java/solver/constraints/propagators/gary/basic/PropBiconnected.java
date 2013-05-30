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
import solver.variables.graph.UndirectedGraphVar;
import util.ESat;
import util.graphOperations.connectivity.ConnectivityFinder;

/**
 * Propagator that ensures that the final graph consists in K Connected Components (CC)
 * simple checker (runs in linear time)
 * already too slow for managing thousands of nodes
 *
 * @author Jean-Guillaume Fages
 */
public class PropBiconnected extends Propagator<UndirectedGraphVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private UndirectedGraphVar g;
    private ConnectivityFinder env_CC_finder;


    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropBiconnected(UndirectedGraphVar graph) {
        super(new UndirectedGraphVar[]{graph}, PropagatorPriority.LINEAR,false);
        this.g = vars[0];
        env_CC_finder = new ConnectivityFinder(g.getEnvelopGraph());
    }

    //***********************************************************************************
    // PROPAGATIONS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (g.getEnvelopOrder() == g.getKernelOrder() && !env_CC_finder.isBiconnected()) {
            contradiction(g, "");
        }
    }

    long timestamp = 0;

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        if (timestamp != solver.getSearchLoop().timeStamp) {
            timestamp = solver.getSearchLoop().timeStamp;
            propagate(0);
        }
        // todo incremental behavior ?
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public int getPropagationConditions(int vIdx) {
        return EventType.REMOVENODE.mask + EventType.REMOVEARC.mask + EventType.ENFORCENODE.mask;
    }

    @Override
    public ESat isEntailed() {
        if (!env_CC_finder.isBiconnected()) {
            return ESat.FALSE;
        }
        if (g.instantiated()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }
}
