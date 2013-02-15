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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:25
 */

package samples.set;

import samples.AbstractProblem;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.set.SetConstraintsFactory;
import solver.search.strategy.SetStrategyFactory;
import solver.variables.IntVar;
import solver.variables.SetVar;
import solver.variables.SetVarImpl;
import solver.variables.VariableFactory;

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
        x = new SetVarImpl("x", solver);
        y = new SetVarImpl("y", solver);
        z = new SetVarImpl("z", solver);
        universe = new SetVarImpl("universe", solver);
        sum = VariableFactory.bounded("sum of universe", 12, 19, solver);
        // x initial domain
        x.getEnvelope().add(1);
        x.getKernel().add(1);
        x.getEnvelope().add(2);
        x.getEnvelope().add(3);
        x.getEnvelope().add(8);
        // y initial domain
        y.getEnvelope().add(2);
        y.getEnvelope().add(6);
        y.getEnvelope().add(7);
        // z initial domain
        z.getEnvelope().add(1);
        z.getEnvelope().add(2);
        z.getKernel().add(2);
        z.getEnvelope().add(3);
        z.getEnvelope().add(5);
        z.getEnvelope().add(7);
        z.getEnvelope().add(12);
        // universe initial domain (note that the universe is a variable)
        universe.getEnvelope().add(1);
        universe.getEnvelope().add(2);
        universe.getEnvelope().add(3);
        universe.getEnvelope().add(5);
        universe.getEnvelope().add(7);
        universe.getEnvelope().add(8);
        universe.getEnvelope().add(42);
        // partition constraint
        solver.post(SetConstraintsFactory.partition(new SetVar[]{x, y, z}, universe));
        if (noEmptySet) {
            // forbid empty sets
            solver.post(SetConstraintsFactory.nbEmpty(new SetVar[]{x, y, z, universe}, VariableFactory.fixed(0, solver)));
        }
        // restricts the sum of elements in universe
        solver.post(SetConstraintsFactory.sum(universe, sum));
    }

    @Override
    public void configureSearch() {
        solver.set(SetStrategyFactory.setLex(new SetVar[]{x, y, z, universe}));
    }

    @Override
    public void solve() {
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, sum);
    }

    @Override
    public void prettyOut() {
        System.out.println("best solution found");
        System.out.println("x : {" + x.getEnvelope() + "}");
        System.out.println("y : {" + y.getEnvelope() + "}");
        System.out.println("z : {" + z.getEnvelope() + "}");
        System.out.println("universe : {" + universe.getEnvelope() + "} " + sum);
    }
}
