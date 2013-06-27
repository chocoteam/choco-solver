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
package solver.search.loop;

import org.testng.annotations.Test;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.ICF;
import solver.constraints.nary.nogood.NogoodStoreForRestarts;
import solver.explanations.strategies.ExplainedNeighborhood;
import solver.search.loop.lns.LargeNeighborhoodSearch;
import solver.search.loop.monitors.IMessage;
import solver.search.loop.monitors.SMF;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.Arrays;

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

        solver.post(ICF.sum(vars, obj));
        solver.post(ICF.arithm(vars[0], "+", vars[1], "<", 2));
        solver.post(ICF.arithm(vars[4], "+", vars[5], ">", 3));

        NogoodStoreForRestarts ngs = new NogoodStoreForRestarts(vars, solver);
        solver.getSearchLoop().plugSearchMonitor(new LargeNeighborhoodSearch(solver,
                new ExplainedNeighborhood(solver, vars, 123456L, ngs, 1.05), true));
        solver.post(ngs);
        solver.set(IntStrategyFactory.random(vars, seed));


        SMF.log(solver, true, true, new IMessage() {
            @Override
            public String print() {
                return Arrays.toString(vars) + " o:"+obj;
            }
        });
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, obj);
    }


    @Test(groups = "1s")
    public void test1() {
        small(8);
    }


}
