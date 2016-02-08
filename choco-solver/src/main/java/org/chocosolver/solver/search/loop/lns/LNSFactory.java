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
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.*;
import org.chocosolver.solver.variables.IntVar;

/**
 * @deprecated use {@link INeighborFactory} instead
 * Will be removed after version 3.4.0
 */
@Deprecated
public class LNSFactory {

    // LIST OF AVAILABLE NEIGHBORS

    /**
     * @deprecated use {@link INeighborFactory#random(IntVar[])} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static INeighbor random(Model model, IntVar[] vars, int level, long seed) {
        return new RandomNeighborhood(vars, level, seed);
    }

    /**
     * @deprecated use {@link INeighborFactory#propagationGuided(IntVar[])} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static INeighbor pg(Model model, IntVar[] vars, int fgmtSize, int listSize, long seed) {
        return new PropagationGuidedNeighborhood(vars, fgmtSize, listSize, seed);
    }

    /**
     * @deprecated use {@link INeighborFactory#reversedPropagationGuided(IntVar[])} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static INeighbor rpg(Model model, IntVar[] vars, int fgmtSize, int listSize, long seed) {
        return new ReversePropagationGuidedNeighborhood(vars, fgmtSize, listSize, seed);
    }

    /**
     * @deprecated use {@link Resolver#setLNS(INeighbor, ICounter)}
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void rlns(Model model, IntVar[] vars, int level, long seed, ACounter frcounter) {
        model.getResolver().setLNS(new RandomNeighborhood(vars, level, seed), frcounter);
    }

    /**
     * @deprecated use {@link Resolver#setLNS(INeighbor, ICounter)}
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void pglns(Model model, IntVar[] vars, int fgmtSize, int listSize, int level, long seed, ACounter frcounter) {
        Resolver r = model.getResolver();
        r.setLNS(
                new SequenceNeighborhood(
                        pg(model, vars, fgmtSize, listSize, seed),
                        rpg(model, vars, fgmtSize, listSize, seed),
                        pg(model, vars, fgmtSize, 0, seed) // <= state of the art configuration
                ),
                frcounter);
    }

    /**
     * @deprecated use {@link Resolver#setLNS(INeighbor, ICounter)}
     * and {@link INeighborFactory#explanationBased(IntVar...)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void elns(Model model, IntVar[] vars, int level, long seed,
                            ACounter frcounter) {
        INeighbor neighbor1 = new ExplainingObjective(model, level, seed);
        INeighbor neighbor2 = new ExplainingCut(model, level, seed);
        INeighbor neighbor3 = new RandomNeighborhood(vars, level, seed);

        INeighbor neighbor = new SequenceNeighborhood(neighbor1, neighbor2, neighbor3);
        model.getResolver().setLNS(neighbor, frcounter);
    }
}
