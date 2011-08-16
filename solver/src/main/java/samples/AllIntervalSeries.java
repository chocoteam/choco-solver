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

import choco.kernel.common.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.binary.Absolute;
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.unary.Relation;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.*;
import solver.propagation.engines.comparators.predicate.MemberC;
import solver.propagation.engines.comparators.predicate.Not;
import solver.propagation.engines.comparators.predicate.Predicate;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class AllIntervalSeries extends AbstractProblem {
    @Option(name = "-o", usage = "All interval series size.", required = false)
    private int m = 500;

    @SuppressWarnings({"FieldCanBeLocal"})
    @Option(name = "-a", aliases = "--abs", usage = "Force ABSOLUTE constraint declaration.", required = false)
    private boolean abs = false;

    IntVar[] vars;
    IntVar[] dist;
    IntVar[] tmp;

    Constraint[] ALLDIFF;
    Constraint[] DISTANCE;
    Constraint[] OTHERS;

    @Override
    public void buildModel() {

        solver = new Solver();
        vars = VariableFactory.enumeratedArray("v", m, 0, m - 1, solver);
        if (abs) {
            dist = VariableFactory.enumeratedArray("d", m - 1, 1, m - 1, solver);
        } else {
            dist = new IntVar[m - 1];
        }

        tmp = VariableFactory.enumeratedArray("tmp", m - 1, -(m - 1), m - 1, solver);

        List<Constraint> distance = new ArrayList<Constraint>();
        for (int i = 0; i < m - 1; i++) {
            distance.add(Sum.eq(new IntVar[]{vars[i + 1], vars[i], tmp[i]}, new int[]{1, -1, -1}, 0, solver));
            if (abs) {
                distance.add(new Absolute(dist[i], tmp[i], solver));
            } else {
                dist[i] = VariableFactory.abs(tmp[i]);
                solver.post(new Relation(dist[i], Relation.R.GT,0, solver));
                solver.post(new Relation(dist[i], Relation.R.LT,m, solver));
            }
        }
        DISTANCE = distance.toArray(new Constraint[distance.size()]);
        solver.post(DISTANCE);

        ALLDIFF = new Constraint[2];
        ALLDIFF[0] = (new AllDifferent(vars, solver));
        ALLDIFF[1] = (new AllDifferent(dist, solver));
        solver.post(ALLDIFF);

        // break symetries
        OTHERS = new Constraint[2];
        OTHERS[0] = (new GreaterOrEqualX_YC(vars[1], vars[0], 1, solver));
        OTHERS[1] = (new GreaterOrEqualX_YC(dist[0], dist[m - 2], 1, solver));
        solver.post(OTHERS);
    }

    @Override
    public void configureSolver() {
        //TODO: changer la strategie pour une plus efficace
        solver.set(StrategyFactory.minDomMinVal(vars, solver.getEnvironment()));

        // TODO chercher un meilleur ordre de propagation
        // la clŽ semble se trouver dans la contrainte AllDiff sur les distances
        //EngineStrategyFactory.constraintOriented(solver);
        //EngineStrategyFactory.variableOriented(solver);
        solver.getEngine().addGroup(
                Group.buildGroup(
                        new Not(new MemberC(new HashSet<Constraint>(Arrays.asList(ArrayUtils.append(ALLDIFF, DISTANCE))))),
                        IncrArityP.get(),
                        Policy.ITERATE
                ));
        solver.getEngine().addGroup(
                Group.buildGroup(
                        new Not(new MemberC(new HashSet<Constraint>(Arrays.asList(ALLDIFF)))),
                        new Seq(
                                new IncrOrderC(DISTANCE),
                                new Decr(IncrPosP.get())
                                ),
                        Policy.ONE
                ));
        solver.getEngine().addGroup(
                Group.buildGroup(
                        Predicate.TRUE,
                        new Seq(
                                new Decr(new IncrOrderC(ALLDIFF)),
                                new IncrOrderV(vars)
                                ),
                        Policy.ONE
                ));

    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("All interval series({})", m);
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < m - 1; i++) {
            st.append(String.format("%d <%d> ", vars[i].getValue(), dist[i].getValue()));
            if (i % 10 == 9) {
                st.append("\n\t");
            }
        }
        st.append(String.format("%d", vars[m - 1].getValue()));
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new AllIntervalSeries().execute(args);
    }
}
