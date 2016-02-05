/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

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
        model = new Model();
        positions = model.intVarArray("pos", data.people(), 0, data.people() - 1, true);
        violations = model.intVar("viol", 0, data.preferences().length, true);

        viols = model.boolVarArray("b", data.prefPerPeople());
        dist = new IntVar[data.prefPerPeople()];
        for (int i = 0; i < data.prefPerPeople(); i++) {
            int pa = data.preferences()[(2 * i)];
            int pb = data.preferences()[2 * i + 1];


            IntVar k = model.intVar(-20000, 20000);
            model.sum(new IntVar[]{positions[pb], k}, "=", positions[pa]).post();
            dist[i] = model.intAbsView(k);

            model.ifThenElse(viols[i],
                    model.arithm(dist[i], ">", 1),
                    model.arithm(dist[i], "<=", 1));
        }
        model.sum(viols, "=", violations).post();
        model.allDifferent(positions, "BC").post();
        model.arithm(positions[1], ">", positions[0]).post();
    }

    @Override
    public void configureSearch() {
        model.set(IntStrategyFactory.minDom_LB(positions));
    }

    @Override
    public void solve() {
        model.findOptimalSolution(ResolutionPolicy.MINIMIZE, violations);
//        model.findAllSolutions();
    }

    @Override
    public void prettyOut() {
        System.out.println(String.format("Photo -- %s", data.name()));
        StringBuilder st = new StringBuilder();
        st.append("\tPositions: ");
        for (int i = 0; i < data.people(); i++) {
            st.append(String.format("%d ", positions[i].getValue()));
        }
        st.append(String.format("\n\tViolations: %d", violations.getValue()));
        System.out.println(st.toString());
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
