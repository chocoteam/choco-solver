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
package org.chocosolver.samples.pert;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import java.util.ArrayDeque;
import java.util.BitSet;
import java.util.Deque;

import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;


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
    public void buildModel() {
        model = new Model();
        setUp();

        vars = model.intVarArray("task", n, 0, horizon, true);

        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                if (graph[i][j] == 1) {
                    precedence(vars[i], 1, vars[j]).post();
                }
            }
        }

        for (int i = 0; i < alldiffLayers.length; i++) {
            BitSet disjoint = alldiffLayers[i];
            IntVar[] tvars = new IntVar[disjoint.cardinality()];
            for (int k = 0, j = disjoint.nextSetBit(0); j >= 0; j = disjoint.nextSetBit(j + 1), k++) {
                tvars[k] = vars[j];
            }
            model.allDifferent(tvars, "BC").post();
        }
    }

    static Constraint precedence(IntVar x, int duration, IntVar y) {
        return x.getModel().arithm(x, "<=", y, "-", duration);
    }

    @Override
    public void configureSearch() {
        model.set(IntStrategyFactory.lexico_LB(vars));

        int[] rank = new int[n];
        boolean[] treated = new boolean[n];
        int i = 0;
        Deque<Integer> toTreat = new ArrayDeque<>();
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
        model.setObjectives(MINIMIZE, vars[n - 1]);
        model.solve();
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new Pert().execute(args);
    }


}
