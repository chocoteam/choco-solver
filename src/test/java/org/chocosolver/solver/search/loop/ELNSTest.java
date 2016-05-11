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
package org.chocosolver.solver.search.loop;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.loop.lns.neighbors.ExplainingCut;
import org.chocosolver.solver.search.loop.lns.neighbors.INeighbor;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.lns.neighbors.SequenceNeighborhood;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.search.limits.ICounter.Impl.None;
import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.randomSearch;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class ELNSTest {

    private void small(long seed, int n) {
        Model model = new Model();
        final IntVar[] vars = model.intVarArray("var", 6, 0, 4, true);
        final IntVar obj = model.intVar("obj", 0, 6, true);

        model.sum(vars, "=", obj).post();
        model.arithm(vars[0], "+", vars[1], "<", 2).post();
        model.arithm(vars[4], "+", vars[5], ">", 3).post();

        model.getSolver().setCBJLearning(false, false);

        Solver r = model.getSolver();
        INeighbor neighbor = null;
        switch (n){
            case 0:
                neighbor = new RandomNeighborhood(vars, 200, 123456L);
                break;
            case 1:
                neighbor = new ExplainingCut(model, 200, 123456L);
                break;
            case 2:
                neighbor = new SequenceNeighborhood(
                        new ExplainingCut(model, 200, 123456L),
                        new RandomNeighborhood(vars, 200, 123456L)
                );
                break;
        }

        r.setLNS(neighbor, None);
        r.set(randomSearch(vars, seed));
        r.limitFail(500);
        r.showSolutions();
        model.setObjective(MINIMIZE, obj);
        while(model.getSolver().solve());
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        for(int i = 0; i < 3; i++) {
            System.out.printf("case: %d\n", i);
            small(8, i);
        }
    }


}
