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

package samples.pert;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.reified.ReifiedConstraint;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class PertReified extends Pert {

    BoolVar[] bvars;
    List<Propagator> reifieds;

    @Override
    public void buildModel() {
        setUp();
        solver = new Solver();

        vars = VariableFactory.boundedArray("task", n, 0, horizon, solver);
        reifieds = new ArrayList<Propagator>();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (graph[i][j] == 1) {
                    solver.post(precedence(vars[i], 1, vars[j], solver));
                }
            }
        }
        List<BoolVar> lbvars = new ArrayList<BoolVar>();
        for (int i = 0; i < alldiffLayers.length; i++) {
            BitSet disjoint = alldiffLayers[i];
            IntVar[] _vars = new IntVar[disjoint.cardinality()];
            int[] _durs = new int[disjoint.cardinality()];
            for (int k = 0, j = disjoint.nextSetBit(0); j >= 0; j = disjoint.nextSetBit(j + 1), k++) {
                _vars[k] = vars[j];
                _durs[k] = 1;
            }
            for (int l = 0; l < _vars.length - 1; l++) {
                for (int m = l + 1; m < _vars.length; m++) {
                    BoolVar bvar = VariableFactory.bool("b" + l + "_" + m, solver);
                    lbvars.add(bvar);
                    Constraint cc = new ReifiedConstraint(bvar,
                            precedence(_vars[l], _durs[l], _vars[m], solver),
                            precedence(_vars[m], _durs[m], _vars[l], solver),
                            solver);
                    solver.post(cc);
                    for (int k = 0; k < cc.propagators.length; k++) {
                        reifieds.add(cc.propagators[k]);
                    }
                }
            }
        }
        bvars = lbvars.toArray(new BoolVar[lbvars.size()]);
    }

    @Override
    public void configureSearch() {
        solver.set(
                new StrategiesSequencer(
                        solver.getEnvironment(),
                        StrategyFactory.inputOrderMinVal(bvars, solver.getEnvironment()),
                        StrategyFactory.inputOrderMinVal(vars, solver.getEnvironment())
                )
        );

        int[] rank = new int[n];
        boolean[] treated = new boolean[n];
        int i = 0;
        Deque<Integer> toTreat = new ArrayDeque<Integer>();
        toTreat.push(i);
        rank[i] = 0;
        while (!toTreat.isEmpty()) {
            i = toTreat.pop();
            treated[i] = true;
            for (int j = 0; j < n; j++) {
                if (graph[i][j] == 1) {
                    rank[j] = Math.max(rank[i] + 1, rank[j]);
                    if (!treated[j] && !toTreat.contains(j)) {
                        toTreat.push(j);
                    }
                }
            }
        }

        /*IPropagationEngine engine = solver.getEngine();
        engine.addGroup(
                Group.buildGroup(
                        Predicates.member(reifieds.toArray(new Propagator[reifieds.size()])),
                        IncrArityP.get(),
                        Policy.ITERATE
                ));
        engine.addGroup(
                Group.buildGroup(
                        Predicates.priority(PropagatorPriority.TERNARY),
                        new Cond(
                                Predicates.lhs(),
                                new MappingV(vars, rank),
                                new Decr(new MappingV(vars, rank))
                        ),
                        Policy.ITERATE
                ));
        // default group
        engine.addGroup(
                Group.buildGroup(
                        Predicates.all(),
                        IncrArityP.get(),
                        Policy.FIXPOINT
                ));*/
    }

    public static void main(String[] args) {
        new PertReified().execute(args);
    }


}
