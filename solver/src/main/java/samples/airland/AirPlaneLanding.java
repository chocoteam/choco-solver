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

package samples.airland;

import choco.kernel.ResolutionPolicy;
import choco.kernel.common.util.tools.ArrayUtils;
import gnu.trove.TObjectIntHashMap;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.reified.ReifiedConstraint;
import solver.constraints.ternary.MaxXYZ;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.*;
import solver.propagation.engines.comparators.predicate.MemberC;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/04/11
 */
public class AirPlaneLanding extends AbstractProblem {

    @Option(name = "-f", usage = "File name.", required = true)
    String filename;

    int n;

    //DATA
    private int[][] data;
    //    private static final int AT = 0;
    private static final int ELT = 1;
    private static final int TT = 2;
    private static final int LLT = 3;
    private static final int PCBT = 4;
    private static final int PCAT = 5;
    private static final int ST = 6;


    IntVar[] planes, tardiness, earliness;
    BoolVar[] bVars;
    TObjectIntHashMap<Constraint> ranking = new TObjectIntHashMap<Constraint>();
    int[] costLAT;
    TObjectIntHashMap<IntVar> maxCost;
    int[] LLTs;

    IntVar objective;

    public void setUp() {
        ParserAL parser = new ParserAL();
        data = parser.parse(filename);
        n = data.length;
    }

    @Override
    public void buildModel() {
        setUp();
        solver = new Solver("Air plane landing");
        planes = new IntVar[n];
        tardiness = new IntVar[n];
        earliness = new IntVar[n];
        LLTs = new int[n];
        int obj_ub = 0;
        for (int i = 0; i < n; i++) {
            planes[i] = VariableFactory.bounded("p_" + i, data[i][ELT], data[i][LLT], solver);

            earliness[i] = VariableFactory.bounded("a_" + i, 0, data[i][TT] - data[i][ELT], solver);
            tardiness[i] = VariableFactory.bounded("t_" + i, 0, data[i][LLT] - data[i][TT], solver);

            obj_ub += Math.max(
                    (data[i][TT] - data[i][ELT]) * data[i][PCBT],
                    (data[i][LLT] - data[i][TT]) * data[i][PCAT]
            );

            IntVar e = VariableFactory.bounded(i + "_e", -9999, 9999, solver);
            IntVar t = VariableFactory.bounded(i + "_t", -9999, 9999, solver);
            solver.post(Sum.eq(new IntVar[]{e, planes[i]}, new int[]{1, 1}, data[i][TT], solver));
            solver.post(new MaxXYZ(earliness[i], VariableFactory.fixed(0), e, solver));
            solver.post(Sum.eq(new IntVar[]{t, planes[i]}, new int[]{1, -1}, -data[i][TT], solver));
            solver.post(new MaxXYZ(tardiness[i], VariableFactory.fixed(0), t, solver));
            LLTs[i] = data[i][LLT];
        }
        List<BoolVar> booleans = new ArrayList<BoolVar>();
        //disjunctive
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                BoolVar boolVar = VariableFactory.bool("b_" + i + "_" + j, solver);
                booleans.add(boolVar);

                Constraint c1 = precedence(planes[i], data[i][ST + j], planes[j], solver);
                Constraint c2 = precedence(planes[j], data[j][ST + i], planes[i], solver);
                Constraint cr = new ReifiedConstraint(boolVar, c1, c2, solver);
                solver.post(cr);
                ranking.put(cr,
                        Math.min((data[i][LLT] - data[i][TT]) * data[i][PCAT],
                                (data[j][LLT] - data[j][TT]) * data[j][PCAT]));
            }
        }

        bVars = booleans.toArray(new BoolVar[booleans.size()]);

        objective = VariableFactory.bounded("obj", 0, obj_ub, solver);

        // build cost array
        costLAT = new int[2 * n];
        maxCost = new TObjectIntHashMap<IntVar>();
        for (int i = 0; i < n; i++) {
            costLAT[i] = data[i][PCBT];
            costLAT[n + i] = data[i][PCAT];
            maxCost.put(planes[i], Math.max(data[i][PCBT], data[i][PCAT]));
        }

//        solver.post(Sum.eq(ArrayUtils.append(earliness, tardiness), costLAT, objective, 1, solver));
        IntVar obj_e = VariableFactory.bounded("obj_e", 0, obj_ub, solver);
        solver.post(Sum.eq(earliness, Arrays.copyOfRange(costLAT, 0, n), obj_e, 1, solver));

        IntVar obj_t = VariableFactory.bounded("obj_e", 0, obj_ub, solver);
        solver.post(Sum.eq(tardiness, Arrays.copyOfRange(costLAT, n, 2 * n), obj_t, 1, solver));
        solver.post(Sum.eq(new IntVar[]{obj_e, obj_t, objective}, new int[]{1, 1, -1}, 0, solver));

        solver.post(new AllDifferent(planes, solver));
    }

    static Constraint precedence(IntVar x, int duration, IntVar y, Solver solver) {
        return Sum.leq(new IntVar[]{x, y}, new int[]{1, -1}, -duration, solver);
    }

    @Override
    public void configureSolver() {
        Arrays.sort(planes, new Comparator<IntVar>() {
            @Override
            public int compare(IntVar o1, IntVar o2) {
                return maxCost.get(o2) - maxCost.get(o1);
            }
        });
//        solver.set(StrategyFactory.inputOrderInDomainMin(planes, solver.getEnvironment()));

        solver.set(StrategyFactory.firstFailInDomainMin(planes, solver.getEnvironment()));
//        solver.set(StrategyFactory.domwdegMindom(planes, solver));

//        solver.getSearchLoop().getLimitsFactory().setTimeLimit(3000);
        solver.getSearchLoop().getLimitsFactory().setNodeLimit(500000);

        IPropagationEngine engine = solver.getEngine();
        engine.setDefaultComparator(
                new Cond(
                        new MemberC(new HashSet(Arrays.asList(ranking.keys(new Constraint[ranking.size()])))),
                        new Seq(
                                new MappingC(ranking),
                                new IncrOrderV(ArrayUtils.append(bVars, planes))),
                        new MappingV(planes, costLAT)
                )
        );
        engine.setDefaultPolicy(Policy.FIXPOINT);
//        SearchMonitorFactory.log(solver, false, true);
//        SearchMonitorFactory.statEveryXXms(solver, 200);

    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, objective);
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < n; i++) {
            st.append("plane ").append(i).append(" [").
                    append(planes[i].getValue()).append(",+").
                    append("]\n");
        }
        LoggerFactory.getLogger("bench").info(st.toString());

    }

    public static void main(String[] args) {
        new AirPlaneLanding().execute(args);
    }
}
