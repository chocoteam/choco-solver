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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:25
 */

package org.chocosolver.samples.set;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.set.SetConstraintsFactory;
import org.chocosolver.solver.search.strategy.SetStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.VariableFactory;

/**
 * Small problem to illustrate how to use set variables
 * finds a partition a universe so that the sum of elements in universe
 * (restricted to the arbitrary interval [12,19]) is minimal
 *
 * @author Jean-Guillaume Fages
 */
public class Partition extends AbstractProblem {

    private SetVar x, y, z, universe;
    private IntVar sum;
    private boolean noEmptySet = true;

    public static void main(String[] args) {
        new Partition().execute(args);
    }

    @Override
    public void createSolver() {
		solver = new Solver("set union sample");
    }

    @Override
    public void buildModel() {

		///////////////
		// VARIABLES //
		///////////////

		// x initial domain
		int[] x_envelope = new int[]{1,3,2,8}; // not necessarily ordered
		int[] x_kernel = new int[]{1};
		x = VariableFactory.set("x", x_envelope, x_kernel, solver);
		// y initial domain
		int[] y_envelope = new int[]{2,6,7};
		y = VariableFactory.set("y", y_envelope, solver);
		// z initial domain
		int[] z_envelope = new int[]{2,1,3,5,7,12};
		int[] z_kernel = new int[]{2};
		z = VariableFactory.set("z", z_envelope, z_kernel, solver);
		// universe initial domain (note that the universe is a variable)
		int[] universe_envelope = new int[]{1,2,3,5,7,8,42};
		universe = VariableFactory.set("universe", universe_envelope, solver);
		// sum variable
		sum = VariableFactory.bounded("sum of universe", 12, 19, solver);

		/////////////////
		// CONSTRAINTS //
		/////////////////

		// partition constraint
		solver.post(SetConstraintsFactory.partition(new SetVar[]{x, y, z}, universe));
		if (noEmptySet) {
			// forbid empty sets
			solver.post(SetConstraintsFactory.nbEmpty(new SetVar[]{x, y, z, universe}, VariableFactory.fixed(0, solver)));
		}
		// restricts the sum of elements in universe
		solver.post(SetConstraintsFactory.sum(universe, sum, true));
    }

    @Override
    public void configureSearch() {
		// set a search strategy
		solver.set(SetStrategyFactory.force_first(x, y, z, universe));
    }

    @Override
    public void solve() {
		// find the optimum
		solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, sum);
    }

    @Override
    public void prettyOut() {
        System.out.println("best solution found");
        System.out.println(x);
        System.out.println(y);
        System.out.println(z);
		System.out.println(universe);
        System.out.println(sum);
    }
}
