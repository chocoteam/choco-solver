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
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.binary.GreaterOrEqualX_YC;
import solver.constraints.nary.AllDifferent;
import solver.constraints.nary.Sum;
import solver.constraints.reified.ReifiedConstraint;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

/**
 * <a href="http://www.gecode.org">gecode</a>:<br/>
 * "A group of people wants to take a group photo. Each person can give
 * preferences next to whom he or she wants to be placed on the
 * photo. The problem to be solved is to find a placement that
 * violates as few preferences as possible."
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/08/11
 */
public class Photo extends AbstractProblem {

    @Option(name = "-d", aliases = "--data", usage = "Photo preferences .", required = false)
    Data data = Data.small;

    IntVar[] positions;
    IntVar[] dist;
    BoolVar[] viols;
    IntVar violations;

    @Override
    public void buildModel() {
        solver = new Solver();
        positions = VariableFactory.boundedArray("pos", data.people(), 0, data.people() - 1, solver);
        violations = VariableFactory.bounded("viol", 0, data.preferences().length, solver);

        viols = VariableFactory.boolArray("b", data.prefPerPeople(), solver);
        dist = new IntVar[data.prefPerPeople()];
        for (int i = 0; i < data.prefPerPeople(); i++) {
            int pa = data.preferences()[(2 * i)];
            int pb = data.preferences()[2 * i + 1];
            dist[i] = Views.abs(Views.sum(positions[pa], Views.minus(positions[pb])));
            solver.post(new ReifiedConstraint(
                    viols[i],
                    Sum.geq(new IntVar[]{dist[i]}, 2, solver),
                    Sum.leq(new IntVar[]{dist[i]}, 1, solver),
                    solver));
        }
        solver.post(Sum.eq(viols, violations, solver));
        solver.post(new AllDifferent(positions, solver));
        solver.post(new GreaterOrEqualX_YC(positions[1], positions[0], 1, solver));
    }

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.minDomMinVal(positions, solver.getEnvironment()));
        /*IPropagationEngine engine = solver.getEngine();
//        engine.addGroup(Group.buildGroup(
            engine.addGroup(Group.buildQueue(
                Predicates.member(viols),
//                new IncrOrderV(viols),
                Policy.FIXPOINT
        ));
        engine.addGroup(Group.buildQueue(
                Predicates.member(positions),
                Policy.FIXPOINT
        ));
        engine.addGroup(Group.buildQueue(
                Predicates.all(),
                Policy.ONE
        ));*/

    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, violations);
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Photo -- {}", data.name());
        StringBuilder st = new StringBuilder();
        st.append("\tPositions: ");
        for (int i = 0; i < data.people(); i++) {
            st.append(String.format("%d ", positions[i].getValue()));
        }
        st.append(String.format("\n\tViolations: %d", violations.getValue()));
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Photo().execute(args);
    }

    /////////////////////////////////// DATA //////////////////////////////////////////////////
    static enum Data {
        small {
            @Override
            int[] preferences() {
                return new int[]{
                        0, 2,
                        1, 4,
                        2, 3,
                        2, 4,
                        3, 0,
                        4, 3,
                        4, 0,
                        4, 1
                };
            }

            @Override
            int people() {
                return 5;
            }

            @Override
            int prefPerPeople() {
                return 8;
            }
        },
        large {
            @Override
            int[] preferences() {
                return new int[]{
                        0, 2, 0, 4, 0, 7, 1, 4, 1, 8, 2, 3, 2, 4, 3, 0, 3, 4,
                        4, 5, 4, 0, 5, 0, 5, 8, 6, 2, 6, 7, 7, 8, 7, 6
                };
            }

            @Override
            int people() {
                return 9;
            }

            @Override
            int prefPerPeople() {
                return 17;
            }
        };

        abstract int[] preferences();

        abstract int people();

        abstract int prefPerPeople();
    }

}
