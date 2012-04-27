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

package choco.propagation.thread;

import org.testng.Assert;
import org.testng.annotations.Test;
import samples.nqueen.NQueenBinary;
import solver.Solver;
import solver.constraints.ConstraintFactory;
import solver.thread.ThreadSolver;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 6 oct. 2010
 */
public class ThreadSolverTest {

    protected Solver modeler(int n) {
        NQueenBinary pb = new NQueenBinary();
        pb.readArgs("-q", Integer.toString(n));
        pb.buildModel();
        pb.configureSearch();

        return pb.getSolver();
    }


    @Test(groups = "1m")
    public void test1() throws InterruptedException {
        int n = 12;

        Solver sref = modeler(n);
        sref.findAllSolutions();
        float tref = sref.getMeasures().getTimeCount();

        int n1 = n / 2;

        Solver sm1 = modeler(n);
        sm1.post(ConstraintFactory.leq((IntVar) sm1.getVars()[0], n1, sm1));

        Solver sm2 = modeler(n);
        sm2.post(ConstraintFactory.geq((IntVar) sm2.getVars()[0], n1 + 1, sm2));

        ThreadSolver ts1 = new ThreadSolver(sm1);
        ThreadSolver ts2 = new ThreadSolver(sm2);
//

        ts1.findAllSolutions();
        ts2.findAllSolutions();
        float tsms = ts1.getSolver().getMeasures().getTimeCount() + ts2.getSolver().getMeasures().getTimeCount();

        ts1.join();
        ts2.join();

        int nbSol = (int) sref.getMeasures().getSolutionCount();
        Assert.assertEquals(ts1.solver.getMeasures().getSolutionCount()
                + ts2.solver.getMeasures().getSolutionCount(), nbSol);
    }

    @Test(groups = "1m")
    public void test2() throws InterruptedException {
        int n = 10;

        ThreadSolver[] solvers = new ThreadSolver[n];
        for (int i = 0; i < n; i++) {
            solvers[i] = new ThreadSolver(modeler(n));
        }
        for (int i = 0; i < n; i++) {
            solvers[i].findAllSolutions();
        }
        for (int i = 0; i < n; i++) {
            solvers[i].join();
        }
        for (int i = 1; i < n; i++) {
            Assert.assertEquals(solvers[i].solver.getMeasures().getSolutionCount(),solvers[0].solver.getMeasures().getSolutionCount());
            Assert.assertEquals(solvers[i].solver.getMeasures().getNodeCount(),solvers[0].solver.getMeasures().getNodeCount());
        }
    }

}
