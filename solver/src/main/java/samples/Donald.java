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
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * A verbal arithmetic puzzle:
 * <br/>
 * &#32;&#32;&#32;D&#32;O&#32;N&#32;A&#32;L&#32;D<br/>
 * +&#32;G&#32;E&#32;R&#32;A&#32;L&#32;D<br/>
 * ========<br/>
 * &#32;&#32;&#32;R&#32;O&#32;B&#32;E&#32;R&#32;T<br/>
 * <br/>
 * Attribute a different value to each letter, such that the equation is correct.
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class Donald extends AbstractProblem {

    @Option(name = "-c", usage = "Alldifferent consistency.", required = false)
    AllDifferent.Type type = AllDifferent.Type.BC;

    IntVar d, o, n, a, l, g, e, r, b, t;
    IntVar[] letters;

    @Override
    public void createSolver() {
        solver = new Solver("Donald");
    }

    @Override
    public void buildModel() {
        d = VariableFactory.bounded("d", 1, 9, solver);
        o = VariableFactory.bounded("o", 0, 9, solver);
        n = VariableFactory.bounded("n", 0, 9, solver);
        a = VariableFactory.bounded("a", 0, 9, solver);
        l = VariableFactory.bounded("l", 0, 9, solver);
        g = VariableFactory.bounded("g", 1, 9, solver);
        e = VariableFactory.bounded("e", 0, 9, solver);
        r = VariableFactory.bounded("r", 1, 9, solver);
        b = VariableFactory.bounded("b", 0, 9, solver);
        t = VariableFactory.bounded("t", 0, 9, solver);
        letters = new IntVar[]{d, o, n, a, l, g, e, r, b, t};

        solver.post(new AllDifferent(letters, solver, type));
        solver.post(Sum.eq(
                new IntVar[]{d, o, n, a, l, d,
                        g, e, r, a, l, d,
                        r, o, b, e, r, t},
                new int[]{100000, 10000, 1000, 100, 10, 1,
                        100000, 10000, 1000, 100, 10, 1,
                        -100000, -10000, -1000, -100, -10, -1,
                }, 0, solver
        ));


    }

    @Override
    public void configureSearch() {
        solver.set(StrategyFactory.minDomMaxVal(letters, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("donald + gerald = robert ");
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < letters.length; i++) {
            st.append(String.format("%s : %d\n\t", letters[i].getName(), letters[i].getValue()));
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Donald().execute(args);
    }
}
