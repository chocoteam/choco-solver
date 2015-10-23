/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 * <p>
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 * must display the following acknowledgement:
 * This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.ACounter;
import org.chocosolver.solver.search.loop.SLF;
import org.chocosolver.solver.search.loop.lns.neighbors.*;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.criteria.Criterion;

/**
 * A Factory for Large Neighborhood Search, with pre-defined configurations.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/07/13
 */
public class LNSFactory {


    // LIST OF AVAILABLE NEIGHBORS

    /**
     * Create a random neighborhood
     *
     * @param solver the solver concerned
     * @param vars   the pool of variables to choose from
     * @param level  the number of tries for each size of fragment
     * @param seed   a seed for the random selection
     * @return a random neighborhood
     */
    public static INeighbor random(Solver solver, IntVar[] vars, int level, long seed) {
        return new RandomNeighborhood(solver, vars, level, seed);
    }

    /**
     * Create a propagation guided neighborhood
     *
     * @param solver   the solver concerned
     * @param vars     the pool of variables to choose from
     * @param fgmtSize fragment size (evaluated against log value)
     * @param listSize size of the list
     * @param seed     a seed for the random selection
     * @return a propagation-guided neighborhood
     */
    public static INeighbor pg(Solver solver, IntVar[] vars, int fgmtSize, int listSize, long seed) {
        return new PropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
    }

    /**
     * Create a reverse propagation guided neighborhood
     *
     * @param solver    the solver concerned
     * @param vars      the pool of variables to choose from
     * @param fgmtSize  the limit size for PG and RPG
     * @param listSize  size of the list
     * @param seed      a seed for the random selection
     * @return a reverse propagation-guided neighborhood
     */
    public static INeighbor rpg(Solver solver, IntVar[] vars, int fgmtSize, int listSize, long seed) {
        return new ReversePropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
    }

    // PREDEFINED LNS

    /**
     * Create a LNS based on a random neighborhood.
     *
     * @param solver    the solver
     * @param vars      the pool of variables to choose from
     * @param level     the number of tries for each size of fragment
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     * @see org.chocosolver.solver.search.loop.SearchLoopFactory#lns(Solver, INeighbor, Criterion)
     */
    public static void rlns(Solver solver, IntVar[] vars, int level, long seed, ACounter frcounter) {
        SLF.lns(solver,
                new RandomNeighborhood(solver, vars, level, seed),
                frcounter);
    }

    /**
     * Create a PGLNS, based on "Propagation-Guided LNS", Perronn Shaw and Furnon, CP2004.
     *
     * @param solver    the solver
     * @param vars      the pool of variables to choose from
     * @param fgmtSize  fragment size (evaluated against log value)
     * @param listSize  size of the list
     * @param level     the number of tries for each size of fragment
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     * @see org.chocosolver.solver.search.loop.SearchLoopFactory#lns(Solver, INeighbor, Criterion)
     */
    public static void pglns(Solver solver, IntVar[] vars, int fgmtSize, int listSize, int level, long seed, ACounter frcounter) {
        SLF.lns(solver,
                new SequenceNeighborhood(
                        pg(solver, vars, fgmtSize, listSize, seed),
                        rpg(solver, vars, fgmtSize, listSize, seed),
                        pg(solver, vars, fgmtSize, 0, seed) // <= state of the art configuration
                ),
                frcounter);
    }

    /**
     * Create a ELNS, an Explanation based LNS
     *
     * @param solver the solver
     * @param vars   the pool of variables to choose from
     * @param level  the number of tries for each size of fragment
     * @param seed   a seed for the random selection
     * @param frcounter a fast restart counter (can be null) for neighborhoods
     * @see org.chocosolver.solver.search.loop.SearchLoopFactory#lns(Solver, INeighbor, Criterion)
     */
    public static void elns(Solver solver, IntVar[] vars, int level, long seed,
                            ACounter frcounter) {
        INeighbor neighbor1 = new ExplainingObjective(solver, level, seed);
        INeighbor neighbor2 = new ExplainingCut(solver, level, seed);
        INeighbor neighbor3 = new RandomNeighborhood(solver, vars, level, seed);

        INeighbor neighbor = new SequenceNeighborhood(neighbor1, neighbor2, neighbor3);
        SLF.lns(solver, neighbor, frcounter);
    }


}
