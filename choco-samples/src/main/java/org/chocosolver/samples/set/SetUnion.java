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
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.variables.SetVar;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.setVarSearch;
import static org.chocosolver.solver.search.strategy.selectors.ValSelectorFactory.firstValSelector;
import static org.chocosolver.solver.search.strategy.selectors.VarSelectorFactory.firstVarSelector;

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
    public void buildModel() {
        model = new Model();
        // x initial domain
        x = model.setVar("x", new int[]{1}, new int[]{1, -2, 3});
        // y initial domain
        y = model.setVar("y", new int[]{}, new int[]{-6, -2, 7});
        // z initial domain
        z = model.setVar("z", new int[]{}, new int[]{-2, -1, 0, 1, 2, 3, 4, 5, 6, 7});
        // set-union constraint
        model.union(new SetVar[]{x, y}, z).post();
        if (noEmptySet) {
            model.nbEmpty(new SetVar[]{x, y, z}, model.intVar(0)).post();
        }
    }

    @Override
    public void configureSearch() {
        Resolver r = model.getResolver();
        r.set(setVarSearch(firstVarSelector(),firstValSelector(),false,x, y, z));
    }

    @Override
    public void solve() {
        while (model.solve()) ;
    }

    @Override
    public void prettyOut() {
    }
}
