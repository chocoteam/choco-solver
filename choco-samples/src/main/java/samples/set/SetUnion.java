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
import solver.Solver;
import solver.constraints.set.SetConstraintsFactory;
import solver.search.strategy.SetStrategyFactory;
import solver.variables.SetVar;
import solver.variables.VariableFactory;

/**
 * Small problem to illustrate how to use set variables
 * enumerates sets such that z = union(x,y)
 *
 * @author Jean-Guillaume Fages
 */
public class SetUnion extends AbstractProblem {

    private SetVar x, y, z;
    private boolean noEmptySet = true;

    public static void main(String[] args) {
        new SetUnion().execute(args);
    }

    @Override
    public void createSolver() {
        solver = new Solver("set union sample");
    }

    @Override
    public void buildModel() {
        // x initial domain
		x = VariableFactory.set("x",new int[]{1,-2,3},new int[]{1},solver);
        // y initial domain
		y = VariableFactory.set("y",new int[]{-6,-2,7},solver);
        // z initial domain
		z = VariableFactory.set("z",-2,7,solver);
        // set-union constraint
		solver.post(SetConstraintsFactory.union(new SetVar[]{x, y}, z));
        if (noEmptySet) {
            solver.post(SetConstraintsFactory.nbEmpty(new SetVar[]{x, y, z}, VariableFactory.fixed(0, solver)));
        }
    }

    @Override
    public void configureSearch() {
        solver.set(SetStrategyFactory.setLex(new SetVar[]{x, y, z}));
    }

    @Override
    public void solve() {
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
    }
}
