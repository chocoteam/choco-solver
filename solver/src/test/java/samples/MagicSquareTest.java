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

import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.propagation.engines.comparators.EngineStrategies;
import solver.variables.IntVar;
import solver.variables.Variable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/03/11
 */
public class MagicSquareTest {


    protected Solver modeler(int n) {
        MagicSquare pb = new MagicSquare();
        pb.readArgs("-n", Integer.toString(n));
        pb.buildModel();
        pb.configureSolver();
        return pb.getSolver();
    }

    @Test(groups = ">30m")
    public void testAll() {
        Solver sol;
        for (int j = 3; j < 7; j++) {
            sol = modeler(j);
            sol.findAllSolutions();
            long nbsol = sol.getMeasures().getSolutionCount();
            long node = sol.getMeasures().getNodeCount();
            for (int t = 0; t < EngineStrategies.values().length; t++) {
                sol = modeler(j);
                EngineStrategies.values()[t].defineIn(sol);
                sol.findAllSolutions();
                Assert.assertEquals(sol.getMeasures().getSolutionCount(), nbsol);
                Assert.assertEquals(sol.getMeasures().getNodeCount(), node);
            }

        }
    }


    @Test(groups = "1s")
    public void testBug1() throws ContradictionException {
        // square0,0=3 square0,1=6 square0,2={12,13} square0,3={12,13}
        // square1,0={1,2,5,7,8,9...,15} square1,1=16 square1,2={1,2} square1,3={2,5,7,8,9,10...,15}
        // square2,0={1,2,5,7,8,9...,15} square2,1={5,7} square2,2=11 square2,3={2,5,7,8,9,10...,15}
        // square3,0={14,15} square3,1={5,7} square3,2={8,9,10} square3,3=4
        //== >square0,2  ==  12 (0)
        Solver solver = modeler(4);
        Variable[] vars = solver.getVars();
        solver.getSearchLoop().propEngine.init();
        solver.getSearchLoop().propEngine.fixPoint();
        ((IntVar) vars[0]).instantiateTo(3, Cause.Null, false);
        ((IntVar) vars[15]).instantiateTo(4, Cause.Null, false);
        ((IntVar) vars[5]).removeInterval(11, 15, Cause.Null, false);
        ((IntVar) vars[1]).removeValue(2, Cause.Null, false);
        ((IntVar) vars[9]).removeInterval(1, 2, Cause.Null, false);
        ((IntVar) vars[13]).removeInterval(1, 2, Cause.Null, false);
        ((IntVar) vars[1]).instantiateTo(6, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();
        LoggerFactory.getLogger("test").error("************************");
        ((IntVar) vars[2]).instantiateTo(12, Cause.Null, false);
        try {
            solver.getSearchLoop().propEngine.fixPoint();
            LoggerFactory.getLogger("test").error("************************");
            Assert.fail("should fail");
        } catch (ContradictionException e) {
        }
        LoggerFactory.getLogger("test").error("************************");
    }

    @Test(groups = "1s")
    public void testBug2() throws ContradictionException {
        //square0,0=2 square0,1=13 square0,2=16 square0,3=3
        // square1,0={4,5,6,7,8,9...,14} square1,1={7,8,9,10,11,12...,14} square1,2={4,5,6,7,8,9...,10} square1,3={1,4,5,6,7,8...,15}
        // square2,0={4,5,6,7,8,9...,14} square2,1={6,7,8,9,10,11...,12} square2,2={4,5,6,7,8,9...,10} square2,3={1,4,5,6,7,8...,15}
        // square3,0={14,15} square3,1={1,4,5,6,7,8...,8} square3,2={4,5,6,7,8,9...,10} square3,3={8,9,10,11,12,14...,15}
        //[R]!square3,0  ==  14 (1)
        Solver solver = modeler(4);
        solver.getSearchLoop().propEngine.init();
        solver.getSearchLoop().propEngine.fixPoint();
        Variable[] vars = solver.getVars();
        ((IntVar) vars[0]).instantiateTo(2, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();
        ((IntVar) vars[3]).instantiateTo(3, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();
        ((IntVar) vars[1]).instantiateTo(13, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();

        ((IntVar) vars[6]).removeValue(1, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();
        ((IntVar) vars[14]).removeValue(1, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();
        ((IntVar) vars[12]).removeInterval(9, 14, Cause.Null, false);
        solver.getSearchLoop().propEngine.fixPoint();
        Assert.assertTrue(((IntVar) vars[13]).instantiatedTo(1));

    }
}
