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
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.move.IMoveFactory;
import org.chocosolver.solver.search.loop.move.Move;
import org.chocosolver.solver.search.restart.IRestartStrategy;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.criteria.LongCriterion;

/**
 * @deprecated use {@link Solver}, which extends {@link IMoveFactory}, instead
 * Will be removed after version 3.4.0
 */
@Deprecated
public class SearchLoopFactory {

    SearchLoopFactory() {}

    /**
     * @deprecated use {@link Solver#setDFS()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void dfs(Model aModel, AbstractStrategy<V> aSearchStrategy) {
        Solver r = aModel.getSolver();
        r.setDFS();
        r.set(aSearchStrategy);
    }

    /**
     * @deprecated use {@link Solver#setLDS(int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void lds(Model aModel, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        Solver r = aModel.getSolver();
        r.set(aSearchStrategy);
        r.setLDS(discrepancy);
    }

    /**
     * @deprecated use {@link Solver#setDDS(int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void dds(Model aModel, AbstractStrategy<V> aSearchStrategy, int discrepancy) {
        Solver r = aModel.getSolver();
        r.set(aSearchStrategy);
        r.setDDS(discrepancy);
    }

    /**
     * @deprecated use {@link Solver#setHBFS(double, double, long)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void hbfs(Model aModel, AbstractStrategy<V> aSearchStrategy, double a, double b, long N) {
        Solver r = aModel.getSolver();
        r.set(aSearchStrategy);
        r.setHBFS(a, b, N);
    }

    /**
     * @deprecated use {@link Solver#set(Move...)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static <V extends Variable> void seq(Model aModel, Move... moves) {
        aModel.getSolver().set(moves);
    }

    //****************************************************************************************************************//
    //***********************************  MOVE ***********************************************************************//
    //****************************************************************************************************************//

    /**
     * @deprecated use {@link Solver#setRestarts(LongCriterion, IRestartStrategy, int)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void restart(Model aModel, LongCriterion restartCriterion, IRestartStrategy restartStrategy, int restartsLimit) {
        Solver r = aModel.getSolver();
        r.setRestarts(restartCriterion, restartStrategy, restartsLimit);
    }

    /**
     * @deprecated use {@link Solver#setRestartOnSolutions()} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void restartOnSolutions(Model aModel) {
        Solver r = aModel.getSolver();
        r.setRestartOnSolutions();
    }

    /**
     * @deprecated use {@link Solver#setLNS(INeighbor, ICounter)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void lns(Model aModel, INeighbor neighbor, ICounter restartCounter) {
        aModel.getSolver().setLNS(neighbor,restartCounter);
    }


    /**
     * @deprecated use {@link Solver#setLNS(INeighbor)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void lns(Model aModel, INeighbor neighbor) {
        Solver r = aModel.getSolver();
        r.setLNS(neighbor);
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
     * @deprecated use {@link Solver#setCBJLearning(boolean, boolean)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void learnCBJ(Model aModel, boolean nogoodsOn, boolean userFeedbackOn) {
        aModel.getSolver().setCBJLearning(nogoodsOn,userFeedbackOn);
    }

    /**
     * @deprecated use {@link Solver#setDBTLearning(boolean, boolean)} instead
     * Will be removed after version 3.4.0
     */
    @Deprecated
    public static void learnDBT(Model aModel, boolean nogoodsOn, boolean userFeedbackOn) {
        aModel.getSolver().setDBTLearning(nogoodsOn,userFeedbackOn);
    }
}
