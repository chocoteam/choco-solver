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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * @deprecated use {@link Resolver}, which extends {@link IMoveFactory}, instead
 * Will be removed after version 3.4.0
 */
@Deprecated
public class SearchLoopFactory {

    SearchLoopFactory() {}

    /**
     * @deprecated use {@link Resolver#dfs(AbstractStrategy)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void dfs(Model aModel, AbstractStrategy<V> aSearchStrategy) {
        Resolver r = aModel.getResolver();
        r.set(r.dfs(aSearchStrategy));
    }

    /**
     * @deprecated use {@link Resolver#lds(AbstractStrategy, int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void lds(Model aModel, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        Resolver r = aModel.getResolver();
        r.set(r.lds(aSearchStrategy,discrepancy));
    }

    /**
     * @deprecated use {@link Resolver#dds(AbstractStrategy, int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void dds(Model aModel, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        Resolver r = aModel.getResolver();
        r.set(r.dds(aSearchStrategy,discrepancy));
    }

    /**
     * @deprecated use {@link Resolver#hbfs(AbstractStrategy, double, double, long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void hbfs(Model aModel, AbstractStrategy<V> aSearchStrategy, double a, double b, long N) {
        Resolver r = aModel.getResolver();
        r.set(r.hbfs(aSearchStrategy,a,b,N));
    }

    /**
     * @deprecated use {@link Resolver#seqMoves(Move...)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void seq(Model aModel, Move... moves) {
        Resolver r = aModel.getResolver();
        r.set(r.seqMoves(moves));
    }

    //****************************************************************************************************************//
    //***********************************  MOVE ***********************************************************************//
    //****************************************************************************************************************//

    /**
     * @deprecated use {@link Resolver#restart(LongCriterion, IRestartStrategy, int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void restart(Model aModel, LongCriterion restartCriterion, IRestartStrategy restartStrategy, int restartsLimit) {
        Resolver r = aModel.getResolver();
        r.set(r.restart(restartCriterion,restartStrategy,restartsLimit));
    }

    /**
     * @deprecated use {@link Resolver#restartOnSolutions()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void restartOnSolutions(Model aModel) {
        Resolver r = aModel.getResolver();
        r.set(r.restartOnSolutions());
    }

    /**
     * @deprecated use {@link Resolver#lns(INeighbor, ICounter)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void lns(Model aModel, INeighbor neighbor, ICounter restartCounter) {
        Resolver r = aModel.getResolver();
        r.set(r.lns(neighbor,restartCounter));
    }


    /**
     * @deprecated use {@link Resolver#lns(INeighbor)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void lns(Model aModel, INeighbor neighbor) {
        Resolver r = aModel.getResolver();
        r.set(r.lns(neighbor));
    }

    /**
     * @deprecated  will be removed after version 3.4.0
     */
    @Deprecated
    private static void tabuDecisionRepair(Model aModel, int tabuListSize) {
        // TODO: incomplete, have to deal with gamma when extending
//        Move currentMove = aModel.getResolver().getMove();
//        MoveLearnBinaryTDR tdr = new MoveLearnBinaryTDR(aModel, currentMove, tabuListSize);
//        aModel.getResolver().set(tdr);
//        aModel.getResolver().set(tdr);
    }

    //****************************************************************************************************************//
    //***********************************  LEARN *********************************************************************//
    //****************************************************************************************************************//

    /**
     * @deprecated use {@link Resolver#learnCBJ(boolean, boolean)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void learnCBJ(Model aModel, boolean nogoodsOn, boolean userFeedbackOn) {
        Resolver r = aModel.getResolver();
        r.set(r.learnCBJ(nogoodsOn,userFeedbackOn));
    }

    /**
     * @deprecated use {@link Resolver#learnDBT(boolean, boolean)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void learnDBT(Model aModel, boolean nogoodsOn, boolean userFeedbackOn) {
        Resolver r = aModel.getResolver();
        r.set(r.learnDBT(nogoodsOn,userFeedbackOn));
    }
}
