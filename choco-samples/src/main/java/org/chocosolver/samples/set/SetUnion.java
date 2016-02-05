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
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 14/01/13
 * Time: 18:25
 */

package org.chocosolver.samples.set;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.SetStrategyFactory;
import org.chocosolver.solver.variables.SetVar;

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
        x = solver.setVar("x", new int[]{1}, new int[]{1, -2, 3});
        // y initial domain
        y = solver.setVar("y", new int[]{}, new int[]{-6, -2, 7});
        // z initial domain
        z = solver.setVar("z", new int[]{}, new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7});
        // set-union constraint
        solver.union(new SetVar[]{x, y}, z).post();
        if (noEmptySet) {
            solver.nbEmpty(new SetVar[]{x, y, z}, solver.intVar(0)).post();
        }
    }

    @Override
    public void configureSearch() {
        solver.set(SetStrategyFactory.remove_first(new SetVar[]{x, y, z}));
    }

    @Override
    public void solve() {
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
    }
}
