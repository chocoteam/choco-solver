/*
 * This file is part of examples, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.examples.integer;

import org.chocosolver.parser.SetUpException;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.selectors.variables.ImpactBased;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/03/11
 */
public class MagicSquareTest {

    protected Model modeler(int n) throws SetUpException {
        MagicSquare pb = new MagicSquare();
        pb.setUp("-n", Integer.toString(n));
        pb.buildModel();
        pb.configureSearch();
        return pb.getModel();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testImpact() throws SetUpException {
        Model sol;
        int j = 3;
        sol = modeler(j);
        sol.getSolver().setSearch(new ImpactBased((IntVar[]) sol.getSolver().getSearch().getVariables(), 2, 3, 10, 29091981L, false));
        while (sol.getSolver().solve()) ;
        long nbsol = sol.getSolver().getSolutionCount();
        long node = sol.getSolver().getNodeCount();
        sol = modeler(j);
        sol.getSolver().setSearch(new ImpactBased((IntVar[]) sol.getSolver().getSearch().getVariables(), 2, 3, 10, 29091981L, false));
        while (sol.getSolver().solve()) ;
        assertEquals(sol.getSolver().getSolutionCount(), nbsol);
        assertEquals(sol.getSolver().getNodeCount(), node);
    }

    @Test(groups = "5m", timeOut = 300000)
    public void testAll() throws SetUpException {
        Model sol;
        for (int j = 3; j < 5; j++) {
            sol = modeler(j);
            while (sol.getSolver().solve()) ;
            long nbsol = sol.getSolver().getSolutionCount();
            long node = sol.getSolver().getNodeCount();
            sol = modeler(j);
            while (sol.getSolver().solve()) ;
            assertEquals(sol.getSolver().getSolutionCount(), nbsol);
            assertEquals(sol.getSolver().getNodeCount(), node);
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBug1() throws ContradictionException, SetUpException {
        // square0,0=3 square0,1=6 square0,2={12,13} square0,3={12,13}
        // square1,0={1,2,5,7,8,9...,15} square1,1=16 square1,2={1,2} square1,3={2,5,7,8,9,10...,15}
        // square2,0={1,2,5,7,8,9...,15} square2,1={5,7} square2,2=11 square2,3={2,5,7,8,9,10...,15}
        // square3,0={14,15} square3,1={5,7} square3,2={8,9,10} square3,3=4
        //== >square0,2  ==  12 (0)
        Model model = modeler(4);
        Variable[] vars = model.getVars();
        model.getSolver().propagate();
        int offset = 0;
        ((IntVar) vars[offset]).instantiateTo(3, Cause.Null);
        ((IntVar) vars[15 + offset]).instantiateTo(4, Cause.Null);
        ((IntVar) vars[5 + offset]).removeInterval(11, 15, Cause.Null);
        ((IntVar) vars[1 + offset]).removeValue(2, Cause.Null);
        ((IntVar) vars[9 + offset]).removeInterval(1, 2, Cause.Null);
        ((IntVar) vars[13 + offset]).removeInterval(1, 2, Cause.Null);
        ((IntVar) vars[1 + offset]).instantiateTo(6, Cause.Null);
        model.getSolver().propagate();
        ((IntVar) vars[2 + offset]).instantiateTo(12, Cause.Null);
        try {
            model.getSolver().propagate();
            Assert.fail("should fail");
        } catch (ContradictionException ignored) {
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void testBug2() throws ContradictionException, SetUpException {
        //square0,0=2 square0,1=13 square0,2=16 square0,3=3
        // square1,0={4,5,6,7,8,9...,14} square1,1={7,8,9,10,11,12...,14} square1,2={4,5,6,7,8,9...,10} square1,3={1,4,5,6,7,8...,15}
        // square2,0={4,5,6,7,8,9...,14} square2,1={6,7,8,9,10,11...,12} square2,2={4,5,6,7,8,9...,10} square2,3={1,4,5,6,7,8...,15}
        // square3,0={14,15} square3,1={1,4,5,6,7,8...,8} square3,2={4,5,6,7,8,9...,10} square3,3={8,9,10,11,12,14...,15}
        //[R]!square3,0  ==  14 (1)
        Model model = modeler(4);
        model.getSolver().propagate();
        int offset = 0;
        Variable[] vars = model.getVars();
        ((IntVar) vars[offset]).instantiateTo(2, Cause.Null);
        model.getSolver().propagate();
        ((IntVar) vars[3 + offset]).instantiateTo(3, Cause.Null);
        model.getSolver().propagate();
        ((IntVar) vars[1 + offset]).instantiateTo(13, Cause.Null);
        model.getSolver().propagate();

        ((IntVar) vars[6 + offset]).removeValue(1, Cause.Null);
        model.getSolver().propagate();
        ((IntVar) vars[14 + offset]).removeValue(1, Cause.Null);
        model.getSolver().propagate();
        ((IntVar) vars[12 + offset]).removeInterval(9, 14, Cause.Null);
        model.getSolver().propagate();
        Assert.assertTrue(((IntVar) vars[13 + offset]).isInstantiatedTo(1));
    }
}
