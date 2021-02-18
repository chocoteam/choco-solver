/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.examples.nqueen.*;
import org.chocosolver.parser.SetUpException;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30 sept. 2010
 */
public class NQueenTest {

    public final static int[] NB_QUEENS_SOLUTION = {0, 0, 0, 0, 2, 10, 4, 40, 92, 352, 724, 2680, 14200, 73712, 365596};


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

    private void assertIt(Model s) {
        Assert.assertEquals(s.getSolver().getSolutionCount(), NB_QUEENS_SOLUTION[size], "nb sol incorrect");
    }

    protected Model modeler(AbstractNQueen nq, int size) throws SetUpException {
        nq.setUp("-q", Integer.toString(size));
        nq.buildModel();
        nq.configureSearch();
        return nq.getModel();
    }


    @Test(groups = "5m", timeOut = 300000)
    public void testBinary() throws SetUpException {
        Model s = modeler(new NQueenBinary(), size);
        while (s.getSolver().solve()) ;
        assertIt(s);
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testLinBinary() throws SetUpException {
        Model s = modeler(new NQueenLinearBinary(), size);
        while (s.getSolver().solve()) ;
        assertIt(s);
        //s.solve();
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testGlobalBinary() throws SetUpException {
        Model s = modeler(new NQueenBinaryGlobal(), size);
        while (s.getSolver().solve()) ;
        assertIt(s);
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testGlobal() throws ContradictionException, SetUpException {
        Model s = modeler(new NQueenGlobal(), size);
        while (s.getSolver().solve()) ;
        assertIt(s);
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testDualBinary() throws SetUpException {
        Model s = modeler(new NQueenDualBinary(), size);
        while (s.getSolver().solve()) ;
        assertIt(s);
    }


    @Test(groups = "5m", timeOut = 300000)
    public void testDualGlobal() throws SetUpException {
        Model s = modeler(new NQueenDualGlobal(), size);
        while (s.getSolver().solve()) ;
        assertIt(s);
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testAll1() throws SetUpException {
        Model sol;
        for (int j = 4; j < 14; j++) {
            sol = modeler(new NQueenBinary(), j);
            while (sol.getSolver().solve()) ;
            long nbsol = sol.getSolver().getSolutionCount();
            long node = sol.getSolver().getNodeCount();
            sol = modeler(new NQueenBinary(), j);
            while (sol.getSolver().solve()) ;
            assertEquals(sol.getSolver().getSolutionCount(), nbsol);
            assertEquals(sol.getSolver().getNodeCount(), node);
        }
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testAll2() throws SetUpException {
        Model sol;
        for (int j = 4; j < 14; j++) {
            sol = modeler(new NQueenBinary(), j);
            while (sol.getSolver().solve()) ;
            long nbsol = sol.getSolver().getSolutionCount();
            long node = sol.getSolver().getNodeCount();
            sol = modeler(new NQueenBinary(), j);
            // default group
            while (sol.getSolver().solve()) ;
            assertEquals(sol.getSolver().getSolutionCount(), nbsol);
            assertEquals(sol.getSolver().getNodeCount(), node);
        }
    }


    @Test(groups = "1s", timeOut = 60000)
    public void testBug1() throws ContradictionException, SetUpException {
//        "a corriger!!!, ca doit etre du a prop cond des propagators";
        Model model = modeler(new NQueenBinaryGlobal(), 16);
        model.getSolver().propagate();
        int offset = 2;
        Variable[] vars = model.getVars();
        ((IntVar) vars[offset]).instantiateTo(1, Cause.Null);
        ((IntVar) vars[1 + offset]).instantiateTo(3, Cause.Null);
        ((IntVar) vars[2 + offset]).instantiateTo(5, Cause.Null);
        ((IntVar) vars[3 + offset]).instantiateTo(2, Cause.Null);
        ((IntVar) vars[4 + offset]).instantiateTo(12, Cause.Null);
        ((IntVar) vars[5 + offset]).instantiateTo(16, Cause.Null);
        ((IntVar) vars[6 + offset]).instantiateTo(4, Cause.Null);
        model.getSolver().propagate();
//        System.out.printf("%s\n", solver.toString());
        ((IntVar) vars[7 + offset]).instantiateTo(7, Cause.Null);
        try {
            model.getSolver().propagate();
            Assert.fail();
        } catch (ContradictionException ex) {
//            System.out.printf("%s\n", ex.getMessage());
        }
    }
}
