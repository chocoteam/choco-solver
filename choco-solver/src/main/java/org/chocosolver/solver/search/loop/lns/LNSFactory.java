/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.ACounter;
import org.chocosolver.solver.search.loop.lns.neighbors.*;
import org.chocosolver.solver.variables.IntVar;

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
     * @param solver    the solver concerned
     * @param vars      the pool of variables to choose from
     * @param level     the number of tries for each size of fragment
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     * @return a random neighborhood
     * @deprecated
     */
    @Deprecated
    public static INeighbor random(Solver solver, IntVar[] vars, int level, long seed, ACounter frcounter) {
        INeighbor neighbor = new RandomNeighborhood(solver, vars, level, seed);
        return neighbor;
    }

    /**
     * Create a propagation guided neighborhood
     *
     * @param solver    the solver concerned
     * @param vars      the pool of variables to choose from
     * @param fgmtSize  fragment size (evaluated against log value)
     * @param listSize  size of the list
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     * @return a propagation-guided neighborhood
     * @deprecated
     */
    @Deprecated
    public static INeighbor pg(Solver solver, IntVar[] vars, int fgmtSize, int listSize, long seed, ACounter frcounter) {
        INeighbor neighbor = new PropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
        return neighbor;
    }

    /**
     * Create a reverse propagation guided neighborhood
     *
     * @param solver    the solver concerned
     * @param vars      the pool of variables to choose from
     * @param fgmtSize  the limit size for PG and RPG
     * @param listSize  size of the list
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     * @return a reverse propagation-guided neighborhood
     * @deprecated
     */
    @Deprecated
    public static INeighbor rpg(Solver solver, IntVar[] vars, int fgmtSize, int listSize, long seed, ACounter frcounter) {
        INeighbor neighbor = new ReversePropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
        return neighbor;
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
     * @return a Random LNS
     * @deprecated
     */
    @Deprecated
    public static LargeNeighborhoodSearch rlns(Solver solver, IntVar[] vars, int level, long seed, ACounter frcounter) {
        INeighbor neighbor = random(solver, vars, level, seed, frcounter);
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(solver, neighbor, true);
        solver.getSearchLoop().plugSearchMonitor(lns);
        return lns;
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
     * @return a Propagation-Guided LNS
     * @deprecated
     */
    @Deprecated
    public static LargeNeighborhoodSearch pglns(Solver solver, IntVar[] vars, int fgmtSize, int listSize, int level, long seed, ACounter frcounter) {
        INeighbor neighbor = new SequenceNeighborhood(
                pg(solver, vars, fgmtSize, listSize, seed, frcounter),
                rpg(solver, vars, fgmtSize, listSize, seed, frcounter),
//                random(solver, vars, level, seed, frcounter)
                pg(solver, vars, fgmtSize, 0, seed, frcounter) // <= state of the art configuration
        );
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(solver, neighbor, true);
        solver.getSearchLoop().plugSearchMonitor(lns);
        return lns;
    }

    /**
     * Create a ELNS, an Explanation based LNS
     *
     * @param solver the solver
     * @param vars   the pool of variables to choose from
     * @param level  the number of tries for each size of fragment
     * @param seed   a seed for the random selection
     * @param fr4exp a fast restart counter (can be null) for explained neighborhoods
     * @param fr4rnd a fast restart counter (can be null) for random neighborhoods
     * @return an Explanation based LNS
     * @deprecated
     */
    @Deprecated
    public static LargeNeighborhoodSearch elns(Solver solver, IntVar[] vars, int level, long seed,
                                               ACounter fr4exp, ACounter fr4rnd) {
        INeighbor neighbor1 = new ExplainingObjective(solver, level, seed);
        INeighbor neighbor2 = new ExplainingCut(solver, level, seed);
        INeighbor neighbor3 = new RandomNeighborhood(solver, vars, level, seed);

        INeighbor neighbor = new SequenceNeighborhood(neighbor1, neighbor2, neighbor3);
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(solver, neighbor, true);
        solver.getSearchLoop().plugSearchMonitor(lns);
        return lns;
    }

    /**
     * Create a combination of PGLNS and ELNS (an Explanation based LNS)
     *
     * @param solver   the solver
     * @param vars     the pool of variables to choose from
     * @param level    the number of tries for each size of fragment
     * @param seed     a seed for the random selection
     * @param fgmtSize fragment size (evaluated against log value)
     * @param listSize size of the list
     * @param fr4exp   a fast restart counter (can be null) for explained neighborhoods
     * @param fr4rnd   a fast restart counter (can be null) for random neighborhoods
     * @return an Explanation based LNS
     * @deprecated
     */
    @Deprecated
    public static LargeNeighborhoodSearch pgelns(Solver solver, IntVar[] vars, int level, long seed,
                                                 int fgmtSize, int listSize,
                                                 ACounter fr4exp, ACounter fr4rnd) {
        INeighbor neighbor1 = new ExplainingObjective(solver, level, seed);
        INeighbor neighbor2 = new PropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
        INeighbor neighbor3 = new ReversePropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
        INeighbor neighbor4 = new ExplainingCut(solver, level, seed);
        INeighbor neighbor5 = new RandomNeighborhood(solver, vars, fgmtSize, seed);

        INeighbor neighbor = new SequenceNeighborhood(neighbor1, neighbor2, neighbor3, neighbor4, neighbor5);
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(solver, neighbor, true);
        solver.getSearchLoop().plugSearchMonitor(lns);
        return lns;
    }

    /**
     * Create a combination of PGLNS and ELNS (an Explanation based LNS), with adaptive neighborhood selection
     *
     * @param solver   the solver
     * @param vars     the pool of variables to choose from
     * @param level    the number of tries for each size of fragment
     * @param seed     a seed for the random selection
     * @param fgmtSize fragment size (evaluated against log value)
     * @param listSize size of the list
     * @param fr4exp   a fast restart counter (can be null) for explained neighborhoods
     * @param fr4rnd   a fast restart counter (can be null) for random neighborhoods
     * @return an Explanation based LNS
     * @deprecated
     */
    @Deprecated
    public static LargeNeighborhoodSearch apgelns(Solver solver, IntVar[] vars, int level, long seed,
                                                  int fgmtSize, int listSize,
                                                  ACounter fr4exp, ACounter fr4rnd) {
        INeighbor neighbor1 = new ExplainingObjective(solver, level, seed);
        INeighbor neighbor2 = new PropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
        INeighbor neighbor3 = new ReversePropagationGuidedNeighborhood(solver, vars, seed, fgmtSize, listSize);
        INeighbor neighbor4 = new ExplainingCut(solver, level, seed);
        INeighbor neighbor5 = new RandomNeighborhood(solver, vars, fgmtSize, seed);

        INeighbor neighbor = new AdaptiveNeighborhood(seed, neighbor1, neighbor2, neighbor3, neighbor4, neighbor5);
        LargeNeighborhoodSearch lns = new LargeNeighborhoodSearch(solver, neighbor, true);
        solver.getSearchLoop().plugSearchMonitor(lns);
        return lns;
    }


}
