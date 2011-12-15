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

import choco.kernel.ResolutionPolicy;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.reified.ReifiedConstraint;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.*;
import java.util.regex.Pattern;

/**
 * OR-LIBRARY:<br/>
 * "Given a set of planes and runways, the objective is to minimize the total (weighted) deviation from
 * the target landing time for each plane.
 * There are costs associated with landing either earlier or later than a target landing time for each plane.
 * Each plane has to land on one of the runways within its predetermined time windows such that
 * separation criteria between all pairs of planes are satisfied.
 * This type of problem is a large-scale optimization problem, which occurs at busy airports where
 * making optimal use of the bottleneck resource (the runways) is crucial to keep the airport operating smoothly."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/04/11
 */
public class AirPlaneLanding extends AbstractProblem {

    private static final String groupSeparator = "\\,";
    private static final String decimalSeparator = "\\.";
    private static final String non0Digit = "[\\p{javaDigit}&&[^0]]";
    private static Pattern decimalPattern;

    static {
        // \\p{javaDigit} may not be perfect, see above
        String digit = "([0-9])";
        String groupedNumeral = "(" + non0Digit + digit + "?" + digit + "?(" +
                groupSeparator + digit + digit + digit + ")+)";
        // Once again digit++ is used for performance, as above
        String numeral = "((" + digit + "++)|" + groupedNumeral + ")";
        String decimalNumeral = "(" + numeral + "|" + numeral +
                decimalSeparator + digit + "*+|" + decimalSeparator +
                digit + "++)";
        String decimal = "([-+]?" + decimalNumeral + ")";
        decimalPattern = Pattern.compile(decimal);
    }


    @Option(name = "-d", usage = "Airplan landing Data.", required = false)
    Data mData = Data.airland1;

    //DATA
    private int[][] data;
    int n;

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


    @Override
    public void buildModel() {
        data = parse(mData.source());
        n = data.length;

        solver = new Solver("Air plane landing");
        planes = new IntVar[n];
        tardiness = new IntVar[n];
        earliness = new IntVar[n];
        LLTs = new int[n];
        int obj_ub = 0;
        IntVar ZERO = Views.fixed(0, solver);
        for (int i = 0; i < n; i++) {
            planes[i] = VariableFactory.bounded("p_" + i, data[i][ELT], data[i][LLT], solver);

            earliness[i] = VariableFactory.bounded("a_" + i, 0, data[i][TT] - data[i][ELT], solver);
            tardiness[i] = VariableFactory.bounded("t_" + i, 0, data[i][LLT] - data[i][TT], solver);

            obj_ub += Math.max(
                    (data[i][TT] - data[i][ELT]) * data[i][PCBT],
                    (data[i][LLT] - data[i][TT]) * data[i][PCAT]
            );
            earliness[i] = Views.max(ZERO, Views.offset(Views.minus(planes[i]), data[i][TT]));
            tardiness[i] = Views.max(ZERO, Views.offset(planes[i], -data[i][TT]));
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
//        solver.set(StrategyFactory.domwdegMindom(planes, solver));
        solver.set(new StrategiesSequencer(solver.getEnvironment(),
                StrategyFactory.inputOrderMinVal(bVars, solver.getEnvironment()),
                StrategyFactory.inputOrderMinVal(planes, solver.getEnvironment())
        ));

        solver.getSearchLoop().getLimitsBox().setNodeLimit(500000);

        /*IPropagationEngine engine = solver.getEngine();
        // default group
        engine.addGroup(
                Group.buildGroup(
                        Predicates.all(),
                        new Cond(
                                Predicates.member(ranking.keys(new Constraint[ranking.size()])),
                                new Seq(
                                        new MappingC(ranking),
                                        new IncrOrderV(ArrayUtils.append(bVars, planes))),
                                new MappingV(planes, costLAT)
                        ),
                        Policy.FIXPOINT
                ));*/

    }

    @Override
    public void solve() {
//        SearchMonitorFactory.statEveryXXms(solver, 2000);
//        solver.getSearchLoop().getLimitsBox().setSolutionLimit(2);
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

    private int[][] parse(String source) {
        Scanner sc = new Scanner(source);
        int nb = sc.nextInt();
        data = new int[nb][6 + nb];
        sc.nextLine();
        for (int i = 0; i < nb; i++) {
            data[i][0] = sc.nextInt(); // appearance time
            data[i][1] = sc.nextInt(); // earliest landing time
            data[i][2] = sc.nextInt(); // target landing time
            data[i][3] = sc.nextInt(); // latest landing time
            Double tt = Double.parseDouble(sc.next(decimalPattern));
            data[i][4] = (int) Math.ceil(tt); // penalty cost per unit of time for landing before target
            tt = Double.parseDouble(sc.next(decimalPattern));
            data[i][5] = (int) Math.ceil(tt); // penalty cost per unit of time for landing after target
            for (int j = 0; j < nb; j++) {
                data[i][6 + j] = sc.nextInt();
            }
        }
        sc.close();
        return data;
    }

    /////////////////////////////////////////

    static enum Data {
        airland1(" 10 10 \n" +
                " 54 129 155 559 10.00 10.00\n" +
                " 99999 3 15 15 15 15 15 15 \n" +
                " 15 15 \n" +
                " 120 195 258 744 10.00 10.00 \n" +
                " 3 99999 15 15 15 15 15 15 \n" +
                " 15 15 \n" +
                " 14 89 98 510 30.00 30.00 \n" +
                " 15 15 99999 8 8 8 8 8 \n" +
                " 8 8 \n" +
                " 21 96 106 521 30.00 30.00 \n" +
                " 15 15 8 99999 8 8 8 8 \n" +
                " 8 8 \n" +
                " 35 110 123 555 30.00 30.00 \n" +
                " 15 15 8 8 99999 8 8 8 \n" +
                " 8 8 \n" +
                " 45 120 135 576 30.00 30.00 \n" +
                " 15 15 8 8 8 99999 8 8 \n" +
                " 8 8 \n" +
                " 49 124 138 577 30.00 30.00 \n" +
                " 15 15 8 8 8 8 99999 8 \n" +
                " 8 8 \n" +
                " 51 126 140 573 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 99999 \n" +
                " 8 8 \n" +
                " 60 135 150 591 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 99999 8 \n" +
                " 85 160 180 657 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 99999"
        ),
        airland2(" 15 10 \n" +
                " 54 129 155 559 10.00 10.00 \n" +
                " 99999 3 15 15 15 15 15 15 \n" +
                " 15 15 3 3 15 15 3 \n" +
                " 115 190 250 732 10.00 10.00 \n" +
                " 3 99999 15 15 15 15 15 15 \n" +
                " 15 15 3 3 15 15 3 \n" +
                " 9 84 93 501 30.00 30.00 \n" +
                " 15 15 99999 8 8 8 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 14 89 98 509 30.00 30.00 \n" +
                " 15 15 8 99999 8 8 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 25 100 111 536 30.00 30.00 \n" +
                " 15 15 8 8 99999 8 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 32 107 120 552 30.00 30.00 \n" +
                " 15 15 8 8 8 99999 8 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 34 109 121 550 30.00 30.00 \n" +
                " 15 15 8 8 8 8 99999 8 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 34 109 120 544 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 99999 \n" +
                " 8 8 15 15 8 8 15 \n" +
                " 40 115 128 557 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 99999 8 15 15 8 8 15 \n" +
                " 59 134 151 610 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 99999 15 15 8 8 15 \n" +
                " 191 266 341 837 10.00 10.00 \n" +
                " 3 3 15 15 15 15 15 15 \n" +
                " 15 15 99999 3 15 15 3 \n" +
                " 176 251 313 778 10.00 10.00 \n" +
                " 3 3 15 15 15 15 15 15 \n" +
                " 15 15 3 99999 15 15 3 \n" +
                " 85 160 181 674 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 8 15 15 99999 8 15 \n" +
                " 77 152 171 637 30.00 30.00 \n" +
                " 15 15 8 8 8 8 8 8 \n" +
                " 8 8 15 15 8 99999 15 \n" +
                " 201 276 342 815 10.00 10.00 \n" +
                " 3 3 15 15 15 15 15 15 \n" +
                " 15 15 3 3 15 15 99999"),
        airland3(" 20 10\n" +
                " 0 75 82 486 30.00 30.00 \n" +
                " 99999 15 15 8 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 82 157 197 628 10.00 10.00 \n" +
                " 15 99999 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 59 134 160 561 10.00 10.00 \n" +
                " 15 3 99999 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 28 103 117 565 30.00 30.00 \n" +
                " 8 15 15 99999 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 126 201 261 735 10.00 10.00 \n" +
                " 15 3 3 15 99999 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 20 95 106 524 30.00 30.00 \n" +
                " 8 15 15 8 15 99999 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 110 185 229 664 10.00 10.00 \n" +
                " 15 3 3 15 3 15 99999 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 23 98 108 523 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 99999 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 42 117 132 578 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 99999 8 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 42 117 130 569 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 99999 8 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 57 132 149 615 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 99999 8 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 39 114 126 551 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 8 99999 15 15 15 15 \n" +
                " 15 15 8 8 \n" +
                " 186 261 336 834 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 99999 3 3 3 \n" +
                " 3 3 15 15 \n" +
                " 175 250 316 790 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 99999 3 3 \n" +
                " 3 3 15 15 \n" +
                " 139 214 258 688 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 99999 3 \n" +
                " 3 3 15 15 \n" +
                " 235 310 409 967 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 99999 \n" +
                " 3 3 15 15 \n" +
                " 194 269 338 818 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 99999 3 15 15 \n" +
                " 162 237 287 726 10.00 10.00 \n" +
                " 15 3 3 15 3 15 3 15 \n" +
                " 15 15 15 15 3 3 3 3 \n" +
                " 3 99999 15 15 \n" +
                " 69 144 160 607 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 99999 8 \n" +
                " 76 151 169 624 30.00 30.00 \n" +
                " 8 15 15 8 15 8 15 8 \n" +
                " 8 8 8 8 15 15 15 15 \n" +
                " 15 15 8 99999");
        final String source;

        Data(String source) {
            this.source = source;
        }

        String source() {
            return source;
        }
    }
}
