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

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Resolver;
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
     * @param model the model concerned
     * @param vars   the pool of variables to choose from
     * @param level  the number of tries for each size of fragment
     * @param seed   a seed for the random selection
     * @return a random neighborhood
     */
    public static INeighbor random(Model model, IntVar[] vars, int level, long seed) {
        return new RandomNeighborhood(model, vars, level, seed);
    }

    /**
     * Create a propagation guided neighborhood
     *
     * @param model   the model concerned
     * @param vars     the pool of variables to choose from
     * @param fgmtSize fragment size (evaluated against log value)
     * @param listSize size of the list
     * @param seed     a seed for the random selection
     * @return a propagation-guided neighborhood
     */
    public static INeighbor pg(Model model, IntVar[] vars, int fgmtSize, int listSize, long seed) {
        return new PropagationGuidedNeighborhood(model, vars, seed, fgmtSize, listSize);
    }

    /**
     * Create a reverse propagation guided neighborhood
     *
     * @param model    the model concerned
     * @param vars      the pool of variables to choose from
     * @param fgmtSize  the limit size for PG and RPG
     * @param listSize  size of the list
     * @param seed      a seed for the random selection
     * @return a reverse propagation-guided neighborhood
     */
    public static INeighbor rpg(Model model, IntVar[] vars, int fgmtSize, int listSize, long seed) {
        return new ReversePropagationGuidedNeighborhood(model, vars, seed, fgmtSize, listSize);
    }

    // PREDEFINED LNS

    /**
     * Create a LNS based on a random neighborhood.
     *
     * @param model    the model
     * @param vars      the pool of variables to choose from
     * @param level     the number of tries for each size of fragment
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     * @see org.chocosolver.solver.Resolver#lns(INeighbor)
     */
    public static void rlns(Model model, IntVar[] vars, int level, long seed, ACounter frcounter) {
        Resolver r = model.getResolver();
        r.set(r.lns(new RandomNeighborhood(model, vars, level, seed), frcounter));
    }

    /**
     * Create a PGLNS, based on "Propagation-Guided LNS", Perronn Shaw and Furnon, CP2004.
     *
     * @param model    the model
     * @param vars      the pool of variables to choose from
     * @param fgmtSize  fragment size (evaluated against log value)
     * @param listSize  size of the list
     * @param level     the number of tries for each size of fragment
     * @param seed      a seed for the random selection
     * @param frcounter a fast restart counter (can be null)
     */
    public static void pglns(Model model, IntVar[] vars, int fgmtSize, int listSize, int level, long seed, ACounter frcounter) {
        Resolver r = model.getResolver();
        r.set(r.lns(
                new SequenceNeighborhood(
                        pg(model, vars, fgmtSize, listSize, seed),
                        rpg(model, vars, fgmtSize, listSize, seed),
                        pg(model, vars, fgmtSize, 0, seed) // <= state of the art configuration
                ),
                frcounter));
    }

    /**
     * Create a ELNS, an Explanation based LNS
     *
     * @param model the model
     * @param vars   the pool of variables to choose from
     * @param level  the number of tries for each size of fragment
     * @param seed   a seed for the random selection
     * @param frcounter a fast restart counter (can be null) for neighborhoods
     */
    public static void elns(Model model, IntVar[] vars, int level, long seed,
                            ACounter frcounter) {
        INeighbor neighbor1 = new ExplainingObjective(model, level, seed);
        INeighbor neighbor2 = new ExplainingCut(model, level, seed);
        INeighbor neighbor3 = new RandomNeighborhood(model, vars, level, seed);

        INeighbor neighbor = new SequenceNeighborhood(neighbor1, neighbor2, neighbor3);
        Resolver r = model.getResolver();
        r.set(r.lns(neighbor, frcounter));
    }
}
