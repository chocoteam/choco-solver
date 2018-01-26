/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
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
