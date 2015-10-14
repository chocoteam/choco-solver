/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.nqueen;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class NQueenTest {

    public final static int NB_QUEENS_SOLUTION[] = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596};


    private int peType; // propagation type default value

    private int piType; // pilot type default value

    private int slType; // search loop type default value

    private int size;

    public NQueenTest() {
        this.peType = 0;
        this.piType = 0;
        this.slType = 0;
        this.size = 12;
    }

    public NQueenTest(int peType, int piType, int slType, int size) {
        this.peType = peType;
        this.piType = piType;
        this.slType = slType;
        this.size = size;
    }

    private String parameters() {
        return "(s:" + size + " pe:" + peType + " pi:" + piType + " sl:" + slType + ")";
    }

    private void assertIt(Solver s) {
        Assert.assertEquals(s.getMeasures().getSolutionCount(), NB_QUEENS_SOLUTION[size], "nb sol incorrect");
    }

    protected Solver modeler(AbstractNQueen nq, int size) {
        nq.readArgs("-q", Integer.toString(size));
        nq.createSolver();
        nq.buildModel();
        nq.configureSearch();
        return nq.getSolver();
    }


    @Test(groups = "1m")
    public void testBinary() {
        Solver s = modeler(new NQueenBinary(), size);
        s.findAllSolutions();
        assertIt(s);
    }

    @Test(groups = "1m")
    public void testLinBinary() {
        Solver s = modeler(new NQueenLinearBinary(), size);
        s.findAllSolutions();
        assertIt(s);
        //s.findSolution();
    }

    @Test(groups = "1m")
    public void testGlobalBinary() {
        Solver s = modeler(new NQueenBinaryGlobal(), size);
        s.findAllSolutions();
        assertIt(s);
    }

    @Test(groups = "1m")
    public void testGlobal() throws ContradictionException {
        Solver s = modeler(new NQueenGlobal(), size);
        s.findAllSolutions();
        assertIt(s);
    }

    @Test(groups = "1m")
    public void testDualBinary() {
        Solver s = modeler(new NQueenDualBinary(), size);
        s.findAllSolutions();
        assertIt(s);
    }


    @Test(groups = "1m")
    public void testDualGlobal() {
        Solver s = modeler(new NQueenDualGlobal(), size);
        s.findAllSolutions();
        assertIt(s);
    }

    @Test(groups = "verylong")
    public void testAll1() {
        Solver sol;
        for (int j = 4; j < 23; j++) {
            sol = modeler(new NQueenBinary(), j);
            sol.findAllSolutions();
            long nbsol = sol.getMeasures().getSolutionCount();
            long node = sol.getMeasures().getNodeCount();
            for (int t = 0; t < PropagationEngineFactory.values().length; t++) {
                sol = modeler(new NQueenBinary(), j);
                PropagationEngineFactory.values()[t].make(sol);
                sol.findAllSolutions();
                Assert.assertEquals(sol.getMeasures().getSolutionCount(), nbsol);
                Assert.assertEquals(sol.getMeasures().getNodeCount(), node);
            }

        }
    }

    @Test(groups = "verylong")
    public void testAll2() {
        Solver sol;
        for (int j = 4; j < 23; j++) {
            sol = modeler(new NQueenBinary(), j);
            sol.findAllSolutions();
            long nbsol = sol.getMeasures().getSolutionCount();
            long node = sol.getMeasures().getNodeCount();
            for (int t = 0; t < PropagationEngineFactory.values().length; t++) {
                sol = modeler(new NQueenBinary(), j);
                // default group
                PropagationEngineFactory.values()[t].make(sol);
                sol.findAllSolutions();
                Assert.assertEquals(sol.getMeasures().getSolutionCount(), nbsol);
                Assert.assertEquals(sol.getMeasures().getNodeCount(), node);
            }

        }
    }


    @Test(groups = "1s")
    public void testBug1() throws ContradictionException {
//        "a corriger!!!, ca doit etre du a prop cond des propagators";
        Solver solver = modeler(new NQueenBinaryGlobal(), 16);
        solver.propagate();
		int offset = 2;
        Variable[] vars = solver.getVars();
        ((IntVar) vars[offset]).instantiateTo(1, Cause.Null);
        ((IntVar) vars[1+offset]).instantiateTo(3, Cause.Null);
        ((IntVar) vars[2+offset]).instantiateTo(5, Cause.Null);
        ((IntVar) vars[3+offset]).instantiateTo(2, Cause.Null);
        ((IntVar) vars[4+offset]).instantiateTo(12, Cause.Null);
        ((IntVar) vars[5+offset]).instantiateTo(16, Cause.Null);
        ((IntVar) vars[6+offset]).instantiateTo(4, Cause.Null);
        solver.propagate();
//        System.out.printf("%s\n", solver.toString());
        ((IntVar) vars[7+offset]).instantiateTo(7, Cause.Null);
        try {
            solver.propagate();
            Assert.fail();
        } catch (ContradictionException ex) {
//            System.out.printf("%s\n", ex.getMessage());
        }
    }
}
