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
package org.chocosolver.samples.todo.problems.pert;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public class PertReified extends Pert {

    BoolVar[] bvars;


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
        List<BoolVar> lbvars = new ArrayList<>();
        for (int i = 0; i < alldiffLayers.length; i++) {
            BitSet disjoint = alldiffLayers[i];
            IntVar[] _vars = new IntVar[disjoint.cardinality()];
            int[] _durs = new int[disjoint.cardinality()];
            for (int k = 0, j = disjoint.nextSetBit(0); j >= 0; j = disjoint.nextSetBit(j + 1), k++) {
                _vars[k] = vars[j];
                _durs[k] = 1;
            }
            for (int l = 0; l < _vars.length - 1; l++) {
                for (int m = l + 1; m < _vars.length; m++) {
                    BoolVar bvar = model.boolVar("b" + l + "_" + m);
                    lbvars.add(bvar);
                    model.ifThenElse(bvar, precedence(_vars[l], _durs[l], _vars[m]), precedence(_vars[m], _durs[m], _vars[l]));
                }
            }
        }
        bvars = lbvars.toArray(new BoolVar[lbvars.size()]);
    }

    @Override
    public void configureSearch() {
        Solver r = model.getSolver();
        r.set(
                inputOrderLBSearch(bvars),
                inputOrderLBSearch(vars)
        );
    }

    public static void main(String[] args) {
        new PertReified().execute(args);
    }


}
