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
import solver.constraints.binary.Element;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.lex.Lex;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.predicate.*;
import solver.propagation.engines.group.Group;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.List;

import static solver.propagation.engines.comparators.predicate.Predicates.*;

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
    public void buildModel() {
        solver = new Solver("Latin square" + m);
        int mm = m * m;
        square1 = VariableFactory.boundedArray("s1", mm, 1, m, solver);
        square2 = VariableFactory.boundedArray("s2", mm, 1, m, solver);
        vars = VariableFactory.enumeratedArray("vars", mm, 0, mm - 1, solver);

        List<Constraint> ADS = new ArrayList<Constraint>();

        Constraint cc = new AllDifferent(vars, solver);
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
            solver.post(new Element(square1[i], mod, vars[i], solver));
            solver.post(new Element(square2[i], div, vars[i], solver));
        }


        // Rows
        for (int i = 0; i < m; i++) {
            IntVar[] ry = new IntVar[m];
            System.arraycopy(square1, i * m, ry, 0, m);
            cc = new AllDifferent(ry, solver);
            solver.post(cc);
            ADS.add(cc);
            ry = new IntVar[m];
            System.arraycopy(square2, i * m, ry, 0, m);
            cc = new AllDifferent(ry, solver);
            solver.post(cc);
            ADS.add(cc);
        }
        for (int j = 0; j < m; j++) {
            IntVar[] cy = new IntVar[m];
            for (int i = 0; i < m; i++) {
                cy[i] = square1[i * m + j];
            }
            cc = new AllDifferent(cy, solver);
            solver.post(cc);
            ADS.add(cc);
            cy = new IntVar[m];
            for (int i = 0; i < m; i++) {
                cy[i] = square2[i * m + j];
            }
            cc = new AllDifferent(cy, solver);
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
            solver.post(new Lex(ry1, ry2, true, solver));
        }

    }

    @Override
    public void configureSolver() {
        HeuristicValFactory.indomainMiddle(vars);
        solver.set(StrategyVarValAssign.dyn(vars,
                SorterFactory.minDomain(),
                ValidatorFactory.instanciated,
                solver.getEnvironment()));
        //TODO: propagation
        Predicate light = light();
        solver.getEngine().addGroup(
                Group.buildQueue(
                        but(light, member(ALLDIFFS)),
                        Policy.FIXPOINT
                ));
        solver.getEngine().addGroup(
                Group.buildQueue(
                        member_light(ALLDIFFS),
                        Policy.FIXPOINT
                ));

        solver.getEngine().addGroup(
                Group.buildQueue(
                        all(),
                        Policy.ONE
                ));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Ortho latin square({})", m);
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
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new OrthoLatinSquare().execute(args);
    }
}
