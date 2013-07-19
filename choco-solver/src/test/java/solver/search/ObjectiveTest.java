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
package solver.search;


import org.testng.Assert;
import org.testng.annotations.Test;
import solver.ResolutionPolicy;
import solver.Solver;
import solver.search.loop.monitors.SMF;
import solver.variables.IntVar;
import solver.variables.VF;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/07/13
 */
public class ObjectiveTest {

    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();

        IntVar iv = VF.enumerated("iv", 0, 10, solver);

        SMF.log(solver, true, true);
        solver.findOptimalSolution(ResolutionPolicy.MAXIMIZE, iv);
        System.out.println("Maximum: " + iv);
        Assert.assertEquals(solver.getMeasures().getBestSolutionValue(), 10);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 21);

        // solver.reset();
        solver.getSearchLoop().reset();
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, iv);
        System.out.println("Minimum: " + iv);
        Assert.assertEquals(solver.getMeasures().getBestSolutionValue(), 0);
        Assert.assertEquals(solver.getMeasures().getNodeCount(), 2);
    }
}
