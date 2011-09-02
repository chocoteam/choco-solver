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

import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.constraints.nary.Sum;
import solver.constraints.ternary.Times;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.IncrOrderC;
import solver.propagation.engines.comparators.IncrOrderV;
import solver.propagation.engines.comparators.predicate.MemberC;
import solver.propagation.engines.comparators.predicate.MemberV;
import solver.propagation.engines.comparators.predicate.Not;
import solver.propagation.engines.group.Group;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;
import java.util.HashSet;

/**
 * <a href="http://www.mozart-oz.org/documentation/fdt/node21.html">mozart-oz</a>:<br/>
 * "A kid goes into a grocery store and buys four items. The cashier
 * charges $7.11, the kid pays and is about to leave when the cashier
 * calls the kid back, and says ''Hold on, I multiplied the four items
 * instead of adding them; I'll try again; Hah, with adding them the
 * price still comes to $7.11''. What were the prices of the four items?
 * <p/>
 * The model is taken from: Christian Schulte, Gert Smolka, Finite Domain
 * Constraint Programming in Oz. A Tutorial. 2001."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/08/11
 */
public class Grocery extends AbstractProblem {


    IntVar[] vars;

    Constraint[] LEQ, TMP;

    @Override
    public void buildModel() {
        solver = new Solver();
        vars = VariableFactory.enumeratedArray("item", 4, 0, 711, solver);
        solver.post(Sum.eq(vars, 711, solver));

        IntVar[] tmp = VariableFactory.boundedArray("tmp", 2, 1, 711 * 100 * 100, solver);
        IntVar _711 = Views.fixed(711 * 100 * 100 * 100, solver);

        TMP = new Constraint[3];
        TMP[0] = (new Times(vars[0], vars[1], tmp[0], solver));
        TMP[1] = (new Times(vars[2], vars[3], tmp[1], solver));
        TMP[2] = (new Times(tmp[0], tmp[1], _711, solver));
        solver.post(TMP);

        // symetries
        LEQ = new Constraint[3];
        LEQ[0] = (ConstraintFactory.leq(vars[0], vars[1], solver));
        LEQ[1] = (ConstraintFactory.leq(vars[1], vars[2], solver));
        LEQ[2] = (ConstraintFactory.leq(vars[2], vars[3], solver));
        solver.post(LEQ);

    }

    @Override
    public void configureSolver() {
        //AVOID dom/wdeg: it can change the tree search
        //solver.set(StrategyFactory.domwdegMindom(vars, solver));
        HeuristicValFactory.indomainSplitMax(vars);
        solver.set(StrategyVarValAssign.sta(vars,
                SorterFactory.inputOrder(vars),
                ValidatorFactory.instanciated,
                solver.getEnvironment()));
        //FIRST propagators on tmp, natural order
        solver.getEngine().addGroup(
                Group.buildGroup(
                        new Not(new MemberV<IntVar>(new HashSet<IntVar>(Arrays.asList(vars)))),
                        new IncrOrderC(TMP),
                        Policy.FIXPOINT
                ));
        // THEN, LEQ constraints
        solver.getEngine().addGroup(
                Group.buildGroup(
                        new MemberC(new HashSet<Constraint>(Arrays.<Constraint>asList(LEQ))),
                        new IncrOrderV(vars),
                        Policy.ITERATE
                )
        );
        // AND, constraints on VARS, oldest first
        solver.getEngine().addGroup(
                Group.buildQueue(
                        new MemberV<IntVar>(new HashSet<IntVar>(Arrays.<IntVar>asList(vars))), Policy.FIXPOINT
                )
        );
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Grocery");
        StringBuilder st = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            st.append(String.format("\titem %d : %d$\n", (i + 1), vars[i].getValue()));
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Grocery().execute(args);
    }
}
