/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.lns;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.lns.neighbors.*;
import org.chocosolver.solver.variables.IntVar;

/**
 * Factory to creates INeighbor objects that configures LNS
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public class INeighborFactory {

    /**
     * Creates a black-box LNS neighbor
     * @param vars the pool of variables to be freezed
     * @return a black-box LNS neighbor
     */
    public static INeighbor blackBox(IntVar... vars) {
        return sequencer(
                propagationGuided(vars),
                reversedPropagationGuided(vars),
                random(vars)
        );
    }

    /**
     * Create a random neighborhood fixing variables randomly
     * @param vars   the pool of variables to be freezed
     * @return a random neighborhood fixing variables randomly
     */
    public static INeighbor random(IntVar... vars) {
        return new RandomNeighborhood(vars, 3, 0);
    }

    /**
     * Create a propagation guided neighborhood fixing variables based on constraint propagation
     * Based on "Propagation-Guided LNS", Perronn Shaw and Furnon, CP2004
     * @param vars     the pool of variables to be freezed
     * @return a propagation-guided neighborhood
     */
    public static INeighbor propagationGuided(IntVar... vars) {
        return new PropagationGuidedNeighborhood(vars, 30, 10, 0);
    }

    /**
     * Create a reverse propagation guided neighborhood fixing variables based on constraint propagation
     * @param vars      the pool of variables to be freezed
     * @return a reverse propagation-guided neighborhood
     */
    public static INeighbor reversedPropagationGuided(IntVar... vars) {
        return new ReversePropagationGuidedNeighborhood(vars, 0, 30, 10);
    }

    /**
     * Creates an Explanation based LNS Neighbor
     * @param vars   the pool of variables to be freezed
     */
    public static INeighbor explanationBased(IntVar... vars) {
        Model model = vars[0].getModel();
//        INeighbor neighbor1 = new ExplainingObjective(model, 5, 0);
        INeighbor neighbor2 = new ExplainingCut(model, 5, 0);
        INeighbor neighbor3 = new RandomNeighborhood(vars, 5, 0);
        return sequencer(/*neighbor1, */neighbor2, neighbor3);
    }

	/**
     * Creates a composite Neighbor grouping a set of neighbors
     * @param neighbors a set of neighbors to be grouped
     * @return a composite Neighbor grouping a set of neighbors
     */
    public static INeighbor sequencer(INeighbor... neighbors) {
        return new SequenceNeighborhood(neighbors);
    }
}
