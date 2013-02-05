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

package solver.search.strategy.enumerations;

import choco.checker.DomainBuilder;
import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.constraints.nary.Sum;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.sorters.Incr;
import solver.search.strategy.enumerations.sorters.Seq;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.sorters.metrics.IMetric;
import solver.search.strategy.enumerations.sorters.metrics.Map;
import solver.search.strategy.enumerations.sorters.metrics.Middle;
import solver.search.strategy.enumerations.sorters.metrics.operators.GetI;
import solver.search.strategy.enumerations.sorters.metrics.operators.Remap;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.enumerations.values.comparators.Distance;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.nary.Join;
import solver.search.strategy.enumerations.values.heuristics.nary.SeqN;
import solver.search.strategy.enumerations.values.heuristics.unary.DropN;
import solver.search.strategy.enumerations.values.heuristics.unary.Filter;
import solver.search.strategy.enumerations.values.heuristics.unary.FirstN;
import solver.search.strategy.enumerations.values.heuristics.zeroary.DomainUnion;
import solver.search.strategy.enumerations.values.heuristics.zeroary.InstantiatedValues;
import solver.search.strategy.enumerations.values.metrics.Const;
import solver.search.strategy.enumerations.values.metrics.Metric;
import solver.search.strategy.enumerations.values.metrics.NumberOfInstantiatedVariables;
import solver.search.strategy.enumerations.values.predicates.Member;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/12/10
 */
public class VarValTest {

    private final static int SIZE = 11;
    public final static int NB_SOL[] = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596};

//    @Test(groups = {"1s"})
//    public void test1() throws ContradictionException {
//        Random r = new Random(0);
//        Solver s = new Solver();
//        IEnvironment env = s.getEnvironment();
//        int n = 50;
//
//        IntVar[] vars = new IntVar[n];
//        int[][] values = DomainBuilder.buildFullDomains(n, -n, n, r, 0.6, false);
//        for (int i = 0; i < n; i++) {
//            vars[i] = VariableFactory.enumerated("v" + i, values[i], s);
//            System.out.printf("%s\n", vars[i].toString());
//        }
//
//        System.out.printf("=============\n");
//
//        LinkedList<Comparator<IntVar>> varComp = new LinkedList<Comparator<IntVar>>();
//        varComp.add(ComparatorFactory.smallest);
//        varComp.add(ComparatorFactory.first_fail);
//
//        LinkedList<Comparator<IntVal>> valComp = new LinkedList<Comparator<IntVal>>();
//        valComp.add(ComparatorFactory.indomain_max);
//
//        VarVal_DynDyn heuristic = new VarVal_DynDyn(vars, varComp, valComp, ValidatorFactory.instanciated, env);
//
//        Decision dec = heuristic.getDecision();
//        while (dec != null) {
//            System.out.printf("%s\n", dec.toString());
//            dec.buildNext();
//            dec.apply();
//            dec = heuristic.getDecision();
//        }
//    }


    private static Object[] build(int n) {
        Solver solver = new Solver();

        int[][] domains = DomainBuilder.buildFullDomains(n, 1, n);

        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], solver);

        }

        List<Constraint> lcstrs = new ArrayList<Constraint>(10);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                lcstrs.add(IntConstraintFactory.arithm(vars[i], "!=", vars[j]));
                lcstrs.add(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", -k));
                lcstrs.add(IntConstraintFactory.arithm(vars[i], "!=", vars[j], "+", k));
            }
        }
        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);
        solver.post(cstrs);
        return new Object[]{solver, vars};
    }


    private static void feed(AbstractSorter<IntVar> varComp, int type, Solver solver, IntVar[] vars) {
        AbstractStrategy strategy;
        switch (type) {
            default:
            case 1:
                strategy = StrategyVarValAssign.dyn(vars, varComp,
                        ValidatorFactory.instanciated, solver.getEnvironment());
                break;
        }
        solver.set(strategy);
    }


    /**
     * cf. "A Language for Expressing Heuristics", 4.1 Random & increasing
     */
    @Test(groups = "1s")
    public void test401() {

        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // Heuristic val
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var)
            );
        }

        // Heuristic var
        feed(SorterFactory.random(), 1, solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.2 FirstFail & increasing
     */
    @Test(groups = "1s")
    public void test402() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // Heuristic val
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var)
            );
        }

        feed(new Seq<IntVar>(SorterFactory.minDomain(), SorterFactory.inputOrder(vars)), 1, solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.3 First fail, random & increasing
     */
    @Test(groups = "1s")
    public void test403() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // Heuristic val
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var)
            );
        }

        feed(new Seq<IntVar>(SorterFactory.minDomain(), SorterFactory.random()), 1, solver, vars);
        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.4 first fail, Most constrained, input order & increasing
     */
    @Test(groups = "1s")
    public void test404() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var)
            );
        }

        feed(new Seq<IntVar>(SorterFactory.minDomain(),
                new Seq<IntVar>(SorterFactory.mostConstrained(), SorterFactory.inputOrder(vars))), 1,
                solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.5 first fail, Most constrained, random & increasing
     */
    @Test(groups = "1s")
    public void test405() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var)
            );
        }
        feed(new Seq<IntVar>(SorterFactory.minDomain(),
                new Seq<IntVar>(SorterFactory.mostConstrained(), SorterFactory.random())), 1,
                solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.6 Middle, first fail, random & "queen"
     */
    @Test(groups = "1s")
    public void test406() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {
            //TODO: EnumVal with Metric as parameter (bounds and delta)
            Metric middle = new Const((var.getLB() + var.getUB()) / 2);
            var.setHeuristicVal(
                    new Join(new Distance(middle),
                            HeuristicValFactory.enumVal(var, middle.getValue() + 1, 1, var.getUB()),
                            HeuristicValFactory.enumVal(var, middle.getValue(), -1, var.getLB())
                    ));
        }

        feed(new Seq<IntVar>(new Incr<IntVar>(Middle.<IntVar>build(vars)),
                new Seq<IntVar>(SorterFactory.minDomain(), SorterFactory.random())), 1,
                solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.7 input order & already used value, smallest not yet used value
     * BEWARE: incomplete search
     */
    @Test(expectedExceptions = AssertionError.class, groups = "1s")
    public void test407() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(new Member(var), new InstantiatedValues(vars)),
                            new FirstN(HeuristicValFactory.enumVal(var), new Const(1), Action.first_selection)
                    )
            );
        }

        feed(SorterFactory.inputOrder(vars), 1, solver, vars);

        solver.findAllSolutions();
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4081() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = HeuristicValFactory.unsafeEnum(var, Action.first_selection);
            HeuristicVal hval2 = HeuristicValFactory.unsafeEnum(var, Action.first_selection);

            var.setHeuristicVal(
                    new SeqN(
                            new DropN(hval1, metric, Action.first_selection),
                            new FirstN(hval2, metric, Action.first_selection))
            );
        }

        feed(SorterFactory.inputOrder(vars), 1, solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4082() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = HeuristicValFactory.enumVal(var);
            HeuristicVal hval2 = HeuristicValFactory.enumVal(var);
            var.setHeuristicVal(
                    new SeqN(
                            new DropN(hval1, metric, Action.first_selection),
                            new FirstN(hval2, metric, Action.first_selection))
            );
        }

        feed(SorterFactory.inputOrder(vars), 1, solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4083() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = HeuristicValFactory.enumVal(var);

            var.setHeuristicVal(
                    new Filter(new Member(var),
                            HeuristicValFactory.rotateLeft(
                                    hval1,
                                    metric,
                                    Action.first_selection)));
        }

        feed(SorterFactory.inputOrder(vars), 1, solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4084() {
        Object[] o = build(SIZE);
        Solver solver = (Solver) o[0];
        IntVar[] vars = (IntVar[]) o[1];

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = new DomainUnion(vars);

            var.setHeuristicVal(
                    new Filter(new Member(var),
                            HeuristicValFactory.rotateLeft(
                                    hval1,
                                    metric,
                                    Action.first_selection)));
        }

        feed(SorterFactory.inputOrder(vars), 1, solver, vars);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    @Test(groups = "1s")
    public void testSDEM() throws ContradictionException {
        Random random = new Random(0);
        Solver solver = new Solver();
        int n = 10;
        int l = 3, r = 5; // lines, rows

        IntVar[][] vars = new IntVar[l][r];
        int[][] coeffs = new int[l][r];
        int[][] lines = new int[l][r];
        int[][] rows = new int[l][r];
        int[] idx = new int[r];
        IMetric<IntVar>[] metrics = new IMetric[l];
        int k = 1;
        for (int i = 0; i < l; i++) {
            for (int j = 0; j < r; j++, k++) {
                vars[i][j] = VariableFactory.bounded("V_" + k, -2 * n, 2 * n, solver);
                coeffs[i][j] = -n / 2 + random.nextInt(n);
                lines[i][j] = i;
                rows[i][j] = j;
                idx[j] = j;
            }
            Constraint cstr = IntConstraintFactory.scalar(vars[i], coeffs[i], "=", 0);
            solver.post(cstr);
            metrics[i] = cstr.getMetric(Sum.METRIC_COEFFS);
        }
        IntVar[][] tvars = ArrayUtils.transpose(vars);
        for (int j = 0; j < r; j++) {
            solver.post(IntConstraintFactory.alldifferent(tvars[j], "BC"));
        }

//        solver.set(StrategyFactory.presetI(ArrayUtils.flatten(vars), solver.getEnvironment()));
        IMetric<IntVar> maprow = new Map<IntVar>(ArrayUtils.flatten(vars), ArrayUtils.flatten(rows));
        int[] _idx = idx.clone();
        ArrayUtils.reverse(idx);
        IMetric<IntVar> remap = new Remap<IntVar>(maprow, _idx, idx);

        IMetric<IntVar> maplin = new Map<IntVar>(ArrayUtils.flatten(vars), ArrayUtils.flatten(lines));
        IMetric<IntVar> nth = new GetI<IntVar>(maplin, metrics);

        AbstractSorter<IntVar> seq = new Seq<IntVar>(new Incr<IntVar>(remap), new Incr<IntVar>(nth));
        HeuristicValFactory.indomainMin(ArrayUtils.flatten(vars));
        AbstractStrategy strategy = StrategyVarValAssign.dyn(ArrayUtils.flatten(vars), seq, ValidatorFactory.instanciated, solver.getEnvironment());
        solver.set(strategy);

        // initial propagation
        solver.propagate();

        Decision[] ds = new Decision[r * l];
        k = 0;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_10  ==  -20 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_5  ==  -19 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_15  ==  -18 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_14  ==  -20 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_4  ==  -19 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_9  ==  -18 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_8  ==  -20 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_13  ==  -10 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        k++;
        ds[k] = strategy.getDecision();
        Assert.assertEquals(ds[k].toString(), "V_3  ==  -19 (0)");
        ds[k].buildNext();
        ds[k].apply();
        solver.propagate();
        //SearchMonitorFactory.log(solver,  false, false);
        solver.findSolution();


    }
}
