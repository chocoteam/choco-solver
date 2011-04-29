/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.search.strategy.enumerations.comparators.ComparatorFactory;
import solver.search.strategy.enumerations.sorters.AbstractSorter;
import solver.search.strategy.enumerations.sorters.InputOrder;
import solver.search.strategy.enumerations.sorters.Middle;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.comparators.Distance;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.HeuristicValFactory;
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
import java.util.LinkedList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/12/10
 */
public class VarValTest {

    private static final int SIZE = 11;
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

    private static Solver solver;
    private static IntVar[] vars;
    private static Constraint[] cstrs;

    private static void build(int n) {
        solver = new Solver();

        int[][] domains = DomainBuilder.buildFullDomains(n, 1, n);

        vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], solver);

        }

        List<Constraint> lcstrs = new ArrayList<Constraint>(10);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], solver));
                lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], -k, solver));
                lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], k, solver));
            }
        }
        cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);
    }


    private static void feed(LinkedList<AbstractSorter<IntVar>> varComp, int type) {

        AbstractStrategy strategy;
        switch (type) {
            default:
            case 1:
                strategy = new StrategyVarValAssign(vars, varComp, ValidatorFactory.instanciated, solver.getEnvironment(), MyCollection.Type.DYN);
                break;
        }

        solver.post(cstrs);
        solver.set(strategy);
    }


    /**
     * cf. "A Language for Expressing Heuristics", 4.1 Random & increasing
     */
    @Test(groups = "1s")
    public void test401() {
        build(SIZE);

        // Heuristic var
        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(ComparatorFactory.random_var);

        // Heuristic val
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var.getDomain())
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.2 FirstFail & increasing
     */
    @Test(groups = "1s")
    public void test402() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(ComparatorFactory.first_fail);
        varComp.add(new InputOrder<IntVar>(vars));

        // Heuristic val
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var.getDomain())
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.3 First fail, random & increasing
     */
    @Test(groups = "1s")
    public void test403() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(ComparatorFactory.first_fail);
        varComp.add(ComparatorFactory.random_var);

        // Heuristic val
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var.getDomain())
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.4 first fail, Most constrained, input order & increasing
     */
    @Test(groups = "1s")
    public void test404() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(ComparatorFactory.first_fail);
        varComp.add(ComparatorFactory.most_constrained);
        varComp.add(new InputOrder<IntVar>(vars));

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var.getDomain())
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.5 first fail, Most constrained, random & increasing
     */
    @Test(groups = "1s")
    public void test405() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(ComparatorFactory.first_fail);
        varComp.add(ComparatorFactory.most_constrained);
        varComp.add(ComparatorFactory.random_var);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var.getDomain())
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.6 Middle, first fail, random & "queen"
     */
    @Test(groups = "1s")
    public void test406() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(new Middle<IntVar>(vars));
        varComp.add(ComparatorFactory.first_fail);
        varComp.add(ComparatorFactory.random_var);

        // HeuristicVal
        for (IntVar var : vars) {
            //TODO: EnumVal with Metric as parameter (bounds and delta)
            Metric middle = new Const((var.getLB() + var.getUB()) / 2);
            var.setHeuristicVal(
                    new Join(new Distance(middle),
                            HeuristicValFactory.enumVal(var.getDomain(), middle.getValue() + 1, 1, var.getUB()),
                            HeuristicValFactory.enumVal(var.getDomain(), middle.getValue(), -1, var.getLB())
                    ));
        }

        feed(varComp, 1);

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
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(new InputOrder<IntVar>(vars));

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(new Member(var.getDomain()), new InstantiatedValues(vars)),
                            new FirstN(HeuristicValFactory.enumVal(var.getDomain()), new Const(1), Action.first_selection)
                    )
            );
        }

        feed(varComp, 1);

        solver.findAllSolutions();
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4081() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(new InputOrder<IntVar>(vars));

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = HeuristicValFactory.unsafeEnum(var.getDomain(), Action.first_selection);
            HeuristicVal hval2 = HeuristicValFactory.unsafeEnum(var.getDomain(), Action.first_selection);

            var.setHeuristicVal(
                    new SeqN(
                            new DropN(hval1, metric, Action.first_selection),
                            new FirstN(hval2, metric, Action.first_selection))
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4082() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(new InputOrder<IntVar>(vars));

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = HeuristicValFactory.enumVal(var.getDomain());
            HeuristicVal hval2 = HeuristicValFactory.enumVal(var.getDomain());
            var.setHeuristicVal(
                    new SeqN(
                            new DropN(hval1, metric, Action.first_selection),
                            new FirstN(hval2, metric, Action.first_selection))
            );
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4083() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(new InputOrder<IntVar>(vars));

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = HeuristicValFactory.enumVal(var.getDomain());

            var.setHeuristicVal(
                    new Filter(new Member(var.getDomain()),
                            HeuristicValFactory.rotateLeft(
                                    hval1,
                                    metric,
                                    Action.first_selection)));
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 4.8 rotate the initial ordering
     */
    @Test(groups = "1s")
    public void test4084() {
        build(SIZE);

        LinkedList<AbstractSorter<IntVar>> varComp = new LinkedList<AbstractSorter<IntVar>>();
        varComp.add(new InputOrder<IntVar>(vars));

        // HeuristicVal
        for (IntVar var : vars) {

            Metric metric = new NumberOfInstantiatedVariables(vars, Action.first_selection);
            HeuristicVal hval1 = new DomainUnion(vars);

            var.setHeuristicVal(
                    new Filter(new Member(var.getDomain()),
                            HeuristicValFactory.rotateLeft(
                                    hval1,
                                    metric,
                                    Action.first_selection)));
        }

        feed(varComp, 1);

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }
}
