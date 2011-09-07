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
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.nary.AllDifferent;
import solver.constraints.unary.Relation;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.Cond;
import solver.propagation.engines.comparators.IncrArityV;
import solver.propagation.engines.comparators.IncrOrderV;
import solver.propagation.engines.comparators.predicate.And;
import solver.propagation.engines.comparators.predicate.MemberC;
import solver.propagation.engines.comparators.predicate.VarNotNull;
import solver.propagation.engines.group.Group;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

/**
 * CSPLib prob007:<br/>
 * "Given n in N, find a vector s = (s_1, ..., s_n), such that
 * <ul>
 * <li>s is a permutation of Z_n = {0,1,...,n-1};</li>
 * <li>the interval vector v = (|s_2-s_1|, |s_3-s_2|, ... |s_n-s_{n-1}|) is a permutation of Z_n-{0} = {1,2,...,n-1}.</li>
 * </ul>
 * <br/>
 * A vector v satisfying these conditions is called an all-interval series of size n;
 * the problem of finding such a series is the all-interval series problem of size n."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class AllIntervalSeries extends AbstractProblem {
    @Option(name = "-o", usage = "All interval series size.", required = false)
    private int m = 500;

    IntVar[] vars;
    IntVar[] dist;

    Constraint[] ALLDIFF;
    Constraint[] OTHERS;

    @Override
    public void buildModel() {

        solver = new Solver();
        vars = VariableFactory.enumeratedArray("v", m, 0, m - 1, solver);
        dist = new IntVar[m - 1];


        /*if (false) {
            dist = VariableFactory.enumeratedArray("dist", m - 1, 1, m - 1, solver);
            IntVar[] tmp = VariableFactory.enumeratedArray("tmp", m - 1, -(m - 1), m - 1, solver);
            for (int i = 0; i < m - 1; i++) {
                solver.post(Sum.eq(new IntVar[]{vars[i + 1], Views.minus(vars[i]), Views.minus(tmp[i])}, 0, solver));
                solver.post(new Absolute(dist[i], tmp[i], solver));
            }
        } else*/
        {
            for (int i = 0; i < m - 1; i++) {
                dist[i] = Views.abs(Views.sum(vars[i + 1], Views.minus(vars[i])));
                solver.post(new Relation(dist[i], Relation.R.GT, 0, solver));
                solver.post(new Relation(dist[i], Relation.R.LT, m, solver));
            }
        }

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

        IntVar[] all = new IntVar[vars.length + dist.length];
        all[0] = vars[0];
        for (int i = 1, k = 1; i < vars.length - 1; i++, k++) {
            all[k++] = vars[i];
            all[k] = dist[i - 1];
        }

        // BEWARE:
        // tout se joue sur le nombre d'appel ˆ la mŽthode filter des contraitne AllDiff BC
        // OLDEST n'appelle que m fois le filtrage lourd de AllDiff, les autres l'appellent 2 * m-1
        // Or, c'est cet algo qui coute.
        // Il se dŽclenche lorsque la derniere requete du propagateur est propagee,
        // il faut donc que celle-ci soit propagee le plus tard possible
        IPropagationEngine peng = solver.getEngine();
        peng.setDeal(IPropagationEngine.Deal.SEQUENCE);
        peng.addGroup(Group.buildGroup(
                new And(new MemberC(ALLDIFF[0], new Constraint[]{}), new VarNotNull()),
                new IncrOrderV(vars),
                Policy.ITERATE
        ));
        peng.addGroup(Group.buildGroup(
                new MemberC(ALLDIFF[1], new Constraint[]{}),
                new Cond(new VarNotNull(), new IncrOrderV(vars), IncrArityV.get()),
                Policy.ONE
        ));
        // + default one
    }

    @Override
    public void solve() {
        //SearchMonitorFactory.log(solver, true, true);
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
