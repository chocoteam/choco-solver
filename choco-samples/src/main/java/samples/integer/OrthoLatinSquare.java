/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package samples.integer;

import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.logger.LoggerFactory;

import java.util.ArrayList;
import java.util.List;


/**
 * Orthogonal latin square
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class OrthoLatinSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Ortho latin square size.", required = false)
    int m = 5;
    IntVar[] square1, square2, vars;
    Constraint[] ALLDIFFS;

    @Override
    public void createSolver() {
        solver = new Solver("Ortho Latin square " + m);
    }

    @Override
    public void buildModel() {
        int mm = m * m;
        square1 = VariableFactory.boundedArray("s1", mm, 1, m, solver);
        square2 = VariableFactory.boundedArray("s2", mm, 1, m, solver);
        vars = VariableFactory.enumeratedArray("vars", mm, 0, mm - 1, solver);

        List<Constraint> ADS = new ArrayList<Constraint>();

        Constraint cc = IntConstraintFactory.alldifferent(vars, "AC");
        solver.post(cc);
        ADS.add(cc);

        int[] mod = new int[mm];
        int[] div = new int[mm];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                mod[i * m + j] = j + 1;
                div[i * m + j] = i + 1;
            }
        }
        for (int i = 0; i < mm; i++) {
            solver.post(IntConstraintFactory.element(square1[i], mod, vars[i], 0, "detect"));
            solver.post(IntConstraintFactory.element(square2[i], div, vars[i], 0, "detect"));
        }

        // Rows
        for (int i = 0; i < m; i++) {
            IntVar[] ry = new IntVar[m];
            System.arraycopy(square1, i * m, ry, 0, m);
            cc = IntConstraintFactory.alldifferent(ry, "BC");
            solver.post(cc);
            ADS.add(cc);
            ry = new IntVar[m];
            System.arraycopy(square2, i * m, ry, 0, m);
            cc = IntConstraintFactory.alldifferent(ry, "BC");
            solver.post(cc);
            ADS.add(cc);
        }
        for (int j = 0; j < m; j++) {
            IntVar[] cy = new IntVar[m];
            for (int i = 0; i < m; i++) {
                cy[i] = square1[i * m + j];
            }
            cc = IntConstraintFactory.alldifferent(cy, "BC");
            solver.post(cc);
            ADS.add(cc);
            cy = new IntVar[m];
            for (int i = 0; i < m; i++) {
                cy[i] = square2[i * m + j];
            }
            cc = IntConstraintFactory.alldifferent(cy, "BC");
            solver.post(cc);
            ADS.add(cc);
        }
        ALLDIFFS = new Constraint[ADS.size()];
        ADS.toArray(ALLDIFFS);
        //TODO: ajouter LEX
        for (int i = 1; i < m; i++) {
            IntVar[] ry1 = new IntVar[m];
            IntVar[] ry2 = new IntVar[m];
            for (int j = 0; j < m; j++) {
                ry1[j] = square1[(i - 1) * m + j];
                ry2[j] = square2[i * m + j];
            }
            solver.post(IntConstraintFactory.lex_less(ry1, ry2));
        }
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_MidValue(vars));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger().info("Ortho latin square({})", m);
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                st.append(String.format("%d ", square1[i * m + j].getValue()));
            }
            st.append("\n\t");
        }
        st.append("\n\t");
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                st.append(String.format("%d ", square2[i * m + j].getValue()));
            }
            st.append("\n\t");
        }
        LoggerFactory.getLogger().info(st.toString());
    }

    public static void main(String[] args) {
        new OrthoLatinSquare().execute(args);
    }
}
