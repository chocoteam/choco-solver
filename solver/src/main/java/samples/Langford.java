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
package samples;

import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.AllDifferent;
import solver.propagation.generator.*;
import solver.propagation.generator.sorter.evaluator.EvtRecEvaluators;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

/**
 * CSPLib prob024:<br/>
 * "Consider two sets of the numbers from 1 to 4.
 * The problem is to arrange the eight numbers in the two sets into a single sequence in which
 * the two 1's appear one number apart,
 * the two 2's appear two numbers apart,
 * the two 3's appear three numbers apart,
 * and the two 4's appear four numbers apart.
 * <p/>
 * The problem generalizes to the L(k,n) problem,
 * which is to arrange k sets of numbers 1 to n,
 * so that each appearance of the number m is m numbers on from the last.
 * <br/>
 * For example, the L(3,9) problem is to arrange 3 sets of the numbers 1 to 9 so that
 * the first two 1's and the second two 1's appear one number apart,
 * the first two 2's and the second two 2's appear two numbers apart, etc."
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/08/11
 * @revision 04/03/12 revise model
 */
public class Langford extends AbstractProblem {

    @Option(name = "-k", usage = "Number of sets.", required = false)
    private int k = 2;

    @Option(name = "-n", usage = "Upper bound.", required = false)
    private int n = 10;

    IntVar[] position;

    Constraint[] lights;
    Constraint alldiff;

    @Override
    public void buildModel() {
        solver = new Solver("Langford number");
        // position of the colors
        // position[i], position[i+k], position[i+2*k]... occurrence of the same color
        position = VariableFactory.enumeratedArray("p", n * k, 0, k * n - 1, solver);
        lights = new Constraint[(k - 1) * n + 1];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < this.k - 1; j++) {
                lights[i + j * n] = ConstraintFactory.eq(Views.offset(position[i + j * n], i + 2), position[i + (j + 1) * n], solver);
            }
        }
        lights[(k - 1) * n] = ConstraintFactory.lt(position[0], position[n * k - 1], solver);
        solver.post(lights);
        alldiff = new AllDifferent(position, solver);
        solver.post(alldiff);
    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.minDomMaxVal(position, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
        /*Generator g1 = new PVar(position, new Predicate[]{new InCstrSet(lights)});
        Generator g2 = new PCons(alldiff);
        solver.set(new Sort(new Sort(new Queue(g1), g2).clearOut(), new PCoarse(solver.getCstrs())).clearOut());*/
        solver.set(new Sort(
                    new Queue(new PCoarse(solver.getCstrs())),
                    new SortDyn(EvtRecEvaluators.MinDomSize, new PVar(solver.getVars()))
            ));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Langford's number ({},{})", k, n);
        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() == Boolean.TRUE) {
            int[] values = new int[k * n];
            for (int i = 0; i < k; i++) {
                for (int j = 0; j < n; j++) {
                    values[position[i * n + j].getValue()] = j + 1;
                }
            }
            st.append("\t");
            for (int i = 0; i < values.length; i++) {
                st.append(values[i]).append(" ");
            }
            st.append("\n");
        } else {
            st.append("\tINFEASIBLE");
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Langford().execute();
    }

}
