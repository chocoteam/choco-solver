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

import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.search.limits.ICounter;
import org.chocosolver.solver.search.loop.lns.neighbors.ExplainingCut;
import org.chocosolver.solver.search.loop.lns.neighbors.ExplainingObjective;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.search.loop.lns.neighbors.SequenceNeighborhood;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.trace.Chatterbox;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/06/13
 */
public class ELNSTest {

    private void small(long seed) {
        Solver solver = new Solver();
        final IntVar[] vars = VariableFactory.boundedArray("var", 6, 0, 4, solver);
        final IntVar obj = VariableFactory.bounded("obj", 0, 6, solver);

        solver.post(ICF.sum(vars, "=", obj));
        solver.post(ICF.arithm(vars[0], "+", vars[1], "<", 2));
        solver.post(ICF.arithm(vars[4], "+", vars[5], ">", 3));

        ExplanationFactory.CBJ.plugin(solver, false, false);

        SLF.lns(solver,
                new SequenceNeighborhood(
                        new ExplainingObjective(solver, 200, 123456L),
                        new ExplainingCut(solver, 200, 123456L),
                        new RandomNeighborhood(solver, vars, 200, 123456L)
                ),
                ICounter.Impl.None);
        solver.set(IntStrategyFactory.random_bound(vars, seed));


//        SMF.log(solver, true, true, new IMessage() {
//            @Override
//            public String print() {
//                return Arrays.toString(vars) + " o:" + obj;
//            }
//        });
        Chatterbox.showSolutions(solver);
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, obj);
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        small(8);
    }


}
