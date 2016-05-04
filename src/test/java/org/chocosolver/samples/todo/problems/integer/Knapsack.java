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
package org.chocosolver.samples.todo.problems.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.IntVar;
import org.kohsuke.args4j.Option;

import static org.chocosolver.solver.ResolutionPolicy.MAXIMIZE;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderUBSearch;

/**
 * <a href="http://en.wikipedia.org/wiki/Knapsack_problem">wikipedia</a>:<br/>
 * "Given a set of items, each with a weight and a value,
 * determine the count of each item to include in a collection so that
 * the total weight is less than or equal to a given limit and the total value is as large as possible.
 * It derives its name from the problem faced by someone who is constrained by a fixed-size knapsack
 * and must fill it with the most useful items."
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class Knapsack extends AbstractProblem {

    @Option(name = "-d", aliases = "--data", usage = "Knapsack data ID.", required = false)
    Data data = Data.k20;

    @Option(name = "-n", usage = "Restricted to n objects.", required = false)
    int n = 13;

    // input data
    int[] capacites;
    int[] energies;
    int[] volumes;
    int[] nbOmax;

    // variables
    public IntVar power;
    public IntVar[] objects;


    public void setUp() {
        // read capacities
        capacites = new int[]{data.data[0], data.data[1]};
        int no = data.data[2];
        if (n > -1) {
            no = n;
        }
        energies = new int[no];
        volumes = new int[no];
        nbOmax = new int[no];
        for (int i = 0, j = 3; i < no; i++) {
            energies[i] = data.data[j++];
            volumes[i] = data.data[j++];
            nbOmax[i] = volumes[i]==0?42:(int) Math.ceil(capacites[1] / volumes[i]);
        }
    }


    @Override
    public void buildModel() {
        model = new Model("Knapsack");
        setUp();
        int nos = energies.length;
        // occurrence of each item
        objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = model.intVar("o_" + (i + 1), 0, nbOmax[i], true);
        }
        // objective variable
        power = model.intVar("power", 0, 9999, true);

        IntVar scalar = model.intVar("weight", capacites[0] - 1, capacites[1] + 1, true);

        model.knapsack(objects, scalar, power, volumes, energies).post();
    }

    @Override
    public void configureSearch() {
        Solver r = model.getSolver();
        // trick : top-down maximization
        r.set(inputOrderUBSearch(power), inputOrderLBSearch(objects));
        model.getSolver().showDecisions();
    }

    @Override
    public void solve() {
        model.setObjective(MAXIMIZE, power);
        while(model.solve()){
            System.out.println(power);
            prettyOut();
        }
    }

    private void prettyOut() {
        StringBuilder st = new StringBuilder(String.format("Knapsack -- %s\n", data.name()));
        st.append("\tItem: Count\n");
        for (int i = 0; i < objects.length; i++) {
            st.append(String.format("\t#%d: %d\n", i, objects[i].getValue()));
        }
        st.append(String.format("\n\tPower: %d", power.getValue()));
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new Knapsack().execute(args);
    }


    ////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////////////
    enum Data {
		k0(new int[]{0, 550, 10,
          100, 0, 49, 25, 54, 0, 12, 41, 78, 94, 30, 75, 65, 40, 31, 59, 90, 95, 50, 99}),
        k10(new int[]{500, 550, 10,
                100, 79, 49, 25, 54, 99, 12, 41, 78, 94, 30, 75, 65, 40, 31, 59, 90, 95, 50, 99}),
        k20(new int[]{1000, 1100, 20,
                54, 38, 12, 57, 47, 69, 33, 90, 30, 79, 65, 89, 56, 28, 57, 70, 91, 38, 88, 71,
                77, 46, 99, 41, 29, 49, 23, 43, 39, 36, 86, 68, 12, 92, 85, 33, 22, 84, 64, 90}),;
        final int[] data;

        Data(int[] data) {
            this.data = data;
        }

        public int get(int i) {
            return data[i];
        }
    }
}
