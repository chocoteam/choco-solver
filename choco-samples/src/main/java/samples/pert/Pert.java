/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

package org.chocosolver.samples.pert;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.kohsuke.args4j.Option;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;


/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class Pert extends AbstractProblem {

    @Option(name = "-n", usage = "Number of nodes.", required = false)
    int n = 1000;

    @Option(name = "-l", usage = "Number of layers.", required = false)
    int layers = 200;

    @Option(name = "-d", usage = "Number of layers in disjunction.", required = false)
    int disjunctions = 2;

    int horizon;
    IntVar[] vars;
    int[][] graph;
    BitSet[] alldiffLayers = new BitSet[0];

    protected void setUp() {
        GraphGenerator2 generator = new GraphGenerator2(layers, n, disjunctions, seed);
        generator.generate();
        this.graph = generator.getGraph();
        this.horizon = n - 1;
    }

    @Override
    public void createSolver() {
        solver = new Solver("Pert");
    }

    @Override
    public void buildModel() {
        setUp();

        vars = VariableFactory.boundedArray("task", n, 0, horizon, solver);

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (graph[i][j] == 1) {
                    solver.post(precedence(vars[i], 1, vars[j]));
                }
            }
        }

        for (int i = 0; i < alldiffLayers.length; i++) {
            BitSet disjoint = alldiffLayers[i];
            IntVar[] tvars = new IntVar[disjoint.cardinality()];
            for (int k = 0, j = disjoint.nextSetBit(0); j >= 0; j = disjoint.nextSetBit(j + 1), k++) {
                tvars[k] = vars[j];
            }
            solver.post(IntConstraintFactory.alldifferent(tvars, "BC"));
        }
    }

    static Constraint precedence(IntVar x, int duration, IntVar y) {
        return IntConstraintFactory.arithm(x, "<=", y, "-", duration);
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.lexico_LB(vars));

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
    }


    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, vars[n - 1]);
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new Pert().execute(args);
    }


}
