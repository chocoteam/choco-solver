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
package org.chocosolver.samples.explanation;


import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 01/05/11
 * Time: 13:26
 */
public class ExplainedOCProblemSmall extends AbstractProblem {

    IntVar[] vars;
    int n = 4;
    int vals = n - 1;

    @Override
    public void createSolver() {
        solver = new Solver();
    }

    @Override
    public void buildModel() {
        vars = solver.intVarArray("x", n, 1, vals, false);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++)
                solver.post(IntConstraintFactory.arithm(vars[i], "!=", vars[j]));
        }
    }

    @Override
    public void configureSearch() {
//        solver.set(StrategyFactory.random(vars, solver.getEnvironment()));
        solver.set(IntStrategyFactory.lexico_LB(vars));
    }

    @Override
    public void solve() {
        ExplanationFactory.CBJ.plugin(solver, false, false);

        if (solver.findSolution()) {
            do {
                this.prettyOut();
            }
            while (solver.nextSolution());
        }
    }

    @Override
    public void prettyOut() {

    }

    public static void main(String[] args) {
        new ExplainedOCProblemSmall().execute(args);
    }
}
