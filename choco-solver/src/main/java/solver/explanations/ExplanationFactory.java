/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.explanations;

import solver.Solver;
import solver.explanations.strategies.ConflictBasedBackjumping;
import solver.explanations.strategies.IDecisionJumper;
import solver.explanations.strategies.PathRepair;
import solver.explanations.strategies.jumper.MostRecentWorldJumper;
import solver.explanations.strategies.jumper.RandomDecisionJumper;

/**
 * A non exhaustive list of ways to plug and exploit explanations.
 * <br/>
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 19/10/11
 * Time: 19:22
 */
public enum ExplanationFactory {

    NONE {
        @Override
        public void plugin(Solver solver, boolean flattened) {
            solver.set(new ExplanationEngine(solver));
        }
    },
    /**
     * Active explanations, but do not interact with search.
     */
    SILENT {
        @Override
        public void plugin(Solver solver, boolean flattened) {
            plugExpl(solver, flattened);
        }
    },

    LAZY {
        @Override
        public void plugin(Solver solver, boolean flattened) {
            solver.set(new LazyExplanationEngine(solver));
        }
    },
    /**
     * add a Conflict-based jumping policy on contradiction to an explained solver.
     * It backtracks up to most recent decision involved in the explanation, and forget younger decisions.
     */
    CBJ {
        @Override
        public void plugin(Solver solver, boolean flattened) {
            plugExpl(solver, flattened);
            new ConflictBasedBackjumping(solver.getExplainer());
        }
    },
    /**
     * add a Dynamic-Backtracking policy on contradiction to an explained solver.
     * It backtracks up to most recent decision involved in the explanation.
     */
    DBT {
        @Override
        public void plugin(Solver solver, boolean flattened) {
            plugExpl(solver, flattened);
            new PathRepair(solver.getExplainer(), new MostRecentWorldJumper( ));
        }
    };

    /**
     * Plug explanations into coe<code>solver</code>.
     *
     * @param solver    the solver to observe
     * @param flattened should explanations be flattened?
     */
    public abstract void plugin(Solver solver, boolean flattened);


    private static void plugExpl(Solver solver, boolean flattened) {
        assert !solver.getExplainer().isActive() : "Explanations are already turn on!";
        solver.set(flattened ? new FlattenedRecorderExplanationEngine(solver)
                : new RecorderExplanationEngine(solver));
    }

    /**
     * add a path-repair policy on contradiction to an explained solver.
     * It backtracks up to a random decision involved in the explanation.
     *
     * @param solver    solver which is explained
     * @param flattened should explanations be flattened?
     */
    public static void path_repair(Solver solver, long seed, boolean flattened) {
        plugExpl(solver, flattened);
        new PathRepair(solver.getExplainer(), new RandomDecisionJumper(seed));
    }

    /**
     * add a path-repair policy on contradiction to an explained solver.
     * It backtracks up to a decision involved in the explanation, using <code>decisionJumper</code>.
     *
     * @param solver         solver which is explained
     * @param decisionJumper a specific algorithm to decide which decision to jump to.
     * @param flattened      should explanations be flattened?
     */
    public static void path_repair(Solver solver, IDecisionJumper decisionJumper, boolean flattened) {
        plugExpl(solver, flattened);
        new PathRepair(solver.getExplainer(), decisionJumper);
    }
}
