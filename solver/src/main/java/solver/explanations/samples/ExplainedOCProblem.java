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

package solver.explanations.samples;


import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 01/05/11
 * Time: 13:26
 */
public class ExplainedOCProblem extends AbstractProblem {

    IntVar[] vars ;
    int n = 4;
    int vals = n-1;

    @Override
    public void createSolver() {
        solver = new Solver();
    }

    @Override
    public void buildModel() {
        vars = VariableFactory.enumeratedArray("x", 2*n, 1, vals, solver);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n ; j++)
                solver.post(new Arithmetic(vars[2 * i], "!=", vars[2 * j], solver));
        }
    }

    @Override
    public void configureSearch() {

//        solver.set(StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment()));
//        solver.set(StrategyFactory.random(vars, solver.getEnvironment()));
        solver.set(StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment()));
    }


    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {

        solver.getExplainer().addExplanationMonitor(solver.getExplainer());
        SearchMonitorFactory.log(solver, false, true);
        solver.findSolution();
        if (solver.isFeasible() == Boolean.TRUE) {
            do {
                this.prettyOut();
            }
            while (solver.nextSolution());
        }
    }

    @Override
    public void prettyOut() {
        for (IntVar v : vars) {
//            System.out.println("* variable " + v);
            for (int i = 1; i <= vals; i++) {
                if (!v.contains(i)) {
                    System.out.println(v + " != " + i + " because " + solver.getExplainer().retrieve(v, i));
                }
            }
        }
    }

     public static void main(String[] args) {
        new ExplainedOCProblem().execute(args);
    }
}
