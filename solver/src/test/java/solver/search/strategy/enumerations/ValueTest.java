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
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.enumerations.values.comparators.Distance;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.nary.Join;
import solver.search.strategy.enumerations.values.heuristics.nary.SeqN;
import solver.search.strategy.enumerations.values.heuristics.unary.DropN;
import solver.search.strategy.enumerations.values.heuristics.unary.Filter;
import solver.search.strategy.enumerations.values.heuristics.unary.FirstN;
import solver.search.strategy.enumerations.values.heuristics.unary.Last;
import solver.search.strategy.enumerations.values.heuristics.zeroary.List;
import solver.search.strategy.enumerations.values.metrics.Const;
import solver.search.strategy.enumerations.values.metrics.Median;
import solver.search.strategy.enumerations.values.predicates.Greater;
import solver.search.strategy.enumerations.values.predicates.Member;
import solver.search.strategy.enumerations.values.predicates.Predicate;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 05/01/11
 */
public class ValueTest {

    private static final int SIZE = 8;
    public final static int NB_SOL[] = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596};

    private static Solver solver;
    private static IntVar[] vars;

    private static void build(int n, int type) {
        solver = new Solver();

        int[][] domains = DomainBuilder.buildFullDomains(n, 1, n);

        vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("Q_" + i, domains[i], solver);

        }

        java.util.List<Constraint> lcstrs = new ArrayList<Constraint>(10);
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int k = j - i;
                lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], solver));
                lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], -k, solver));
                lcstrs.add(ConstraintFactory.neq(vars[i], vars[j], k, solver));
            }
        }
        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        AbstractStrategy strategy;
        switch (type) {
            default:
            case 1:
                strategy = StrategyVarValAssign.dyn(vars, SorterFactory.inputOrder(vars),
                        ValidatorFactory.instanciated, solver.getEnvironment());
                break;
        }

        solver.post(cstrs);
        solver.set(strategy);
    }


    /**
     * cf. "A Language for Expressing Heuristics", 5.4 minimum value
     */
    @Test(groups = "1s")
    public void test401() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var)
            );
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }


    /**
     * cf. "A Language for Expressing Heuristics", 5.4 maximum value
     */
    @Test(groups = "1s")
    public void test402() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB())
            );
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 middle value
     */
    @Test(groups = "1s")
    public void test403() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            int middle = (var.getLB() + var.getUB()) / 2;
            var.setHeuristicVal(
                    new Join(new Distance(new Const(middle)),
                            HeuristicValFactory.enumVal(var, middle, -1, var.getLB()),
                            HeuristicValFactory.enumVal(var, middle + 1, 1, var.getUB()))
            );
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 median value
     */
    @Test(groups = "1s")
    public void test404() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new DropN(HeuristicValFactory.enumVal(var), new Median(var), Action.open_node)
            );
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 k^th smallest value, default: maximum value
     */
    @Test(groups = "1s")
    public void test405() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Last(new FirstN(HeuristicValFactory.enumVal(var), new Const(2), Action.open_node)),
                            HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB())
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }


    /**
     * cf. "A Language for Expressing Heuristics", 5.4 k^th largest value, default: minimum value
     */
    @Test(groups = "1s")
    public void test406() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Last(new FirstN(HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB()), new Const(3))),
                            HeuristicValFactory.enumVal(var)
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 smallest even value, default: maximum value
     */
    @Test(groups = "1s")
    public void test407() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(Predicate.even, HeuristicValFactory.enumVal(var)),
                            HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB())
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 first value strictly greater than fixed value k, default: maximum value
     */
    @Test(groups = "1s")
    public void test408() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(new Greater(3), HeuristicValFactory.enumVal(var)),
                            HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB())
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 first value of specified list of values L, default: minimum value
     */
    @Test(groups = "1s")
    public void test409() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(new Member(var), new List(7, 3, 5, 2)),
                            HeuristicValFactory.enumVal(var)
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 first value of specified list of values L, default: minimum value
     */
    @Test(groups = "1s")
    public void test410() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(Predicate.odd, HeuristicValFactory.enumVal(var)),
                            new SeqN(
                                    new Filter(new Greater(6), HeuristicValFactory.enumVal(var)),
                                    HeuristicValFactory.enumVal(var, var.getUB(), -1, var.getLB())
                            )
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }

    /**
     * cf. "A Language for Expressing Heuristics", 5.4 first value of specified list of values L, default: minimum value
     */
    @Test(groups = "1s")
    public void test411() {
        build(SIZE, 1);

        // HeuristicVal
        for (IntVar var : vars) {
            var.setHeuristicVal(
                    new SeqN(
                            new Filter(new Member(var), new List(19, 17, 13, 11, 7, 5, 3, 2)),
                            HeuristicValFactory.enumVal(var)
                    ));
        }

        Boolean result = solver.findAllSolutions();
        Assert.assertEquals(result, Boolean.TRUE);
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), NB_SOL[SIZE]);
    }
}
