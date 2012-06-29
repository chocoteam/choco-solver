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
import solver.Solver;
import solver.constraints.nary.Sum;
import solver.objective.strategies.Dichotomic_Maximization;
import solver.objective.strategies.TopDown_Maximization;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.PropagationStrategies;
import solver.search.loop.monitors.ABSLNS;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.enumerations.sorters.ActivityBased;
import solver.search.strategy.strategy.StaticStrategiesSequencer;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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

    int[] capacites;
    int[] energies;
    int[] volumes;
    int[] nbOmax;

    Sum c_size, c_energy;

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
            nbOmax[i] = (int) Math.ceil(capacites[1] / volumes[i]);
        }
    }

    @Override
    public void createSolver() {
        solver = new Solver("Knapsack");
    }

    @Override
    public void buildModel() {
        setUp();
        int nos = energies.length;

        objects = new IntVar[nos];
        for (int i = 0; i < nos; i++) {
            objects[i] = VariableFactory.bounded("o_" + (i + 1), 0, nbOmax[i], solver);
        }

        power = VariableFactory.bounded("power", 0, 9999, solver);

        IntVar scalar = VariableFactory.bounded("weight", capacites[0] - 1, capacites[1] + 1, solver);


        c_size = Sum.eq(objects, volumes, scalar, 1, solver);
        c_energy = Sum.eq(objects, energies, power, 1, solver);

        solver.post(c_size);
        solver.post(c_energy);
		solver.post(new solver.constraints.nary.Knapsack(objects,scalar,power,volumes,energies,solver));
    }

    @Override
    public void configureSearch() {
		AbstractStrategy strat = StrategyFactory.domddegMinDom(objects);
		// top-down
//		solver.set(new StaticStrategiesSequencer(new TopDown_Maximization(power),strat));
		// dichotomic
		solver.set(new StaticStrategiesSequencer(new Dichotomic_Maximization(power,solver),strat));
		// bottom-up
//		solver.set(strat);

		// old stuff
        /*AbstractSorter<IntVar> s1 = c_energy.getComparator(Sum.VAR_DECRCOEFFS);
            AbstractSorter<IntVar> s2 = c_size.getComparator(Sum.VAR_DOMOVERCOEFFS);

            AbstractSorter<IntVar> seq = new Seq<IntVar>(s1, s2);

            solver.set(StrategyVarValAssign.dyn(objects,
                    seq,
                    ValidatorFactory.instanciated,
                    solver.getEnvironment()));*/
//        final ActivityBased abs = new ActivityBased(solver, objects, 0.999d, 0.02d, 8, 2.0d, 1, seed);
//        SearchMonitorFactory.log(solver, true, false);
//        solver.set(abs);
//        solver.getSearchLoop().plugSearchMonitor(new ABSLNS(solver, objects, seed, abs, false, objects.length / 2));
    }

    @Override
    public void configureEngine() {
	// not usefull
//        IPropagationEngine pengine = new PropagationEngine(solver.getEnvironment());
//        PropagationStrategies.TWO_QUEUES_WITH_ARCS.make(solver, pengine);
//        solver.set(pengine);
    }

    @Override
    public void solve() {
//      SearchMonitorFactory.log(solver, true, true);
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, power);
    }

    @Override
    public void prettyOut() {
    }

    public static void main(String[] args) {
        new Knapsack().execute(args);
        }


    ////////////////////////////////////////// DATA ////////////////////////////////////////////////////////////////////
    static enum Data {
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
