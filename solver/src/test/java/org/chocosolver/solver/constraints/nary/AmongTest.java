/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.chocosolver.solver.Cause.Null;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/02/12
 */
public class AmongTest {

    @Test(groups="10s", timeOut=60000)
    public void testRandomProblems() {
        for (int bigseed = 0; bigseed < 11; bigseed++) {
            long nbsol, nbsol2;
            //nb solutions of the gac constraint
            long realNbSol = randomOcc(-1, bigseed, true, 1, true);
            //nb solutions of occurrence + enum
            nbsol = randomOcc(realNbSol, bigseed, true, 3, false);
            //b solutions of occurrences + bound
            nbsol2 = randomOcc(realNbSol, bigseed, false, 3, false);
            Assert.assertEquals(nbsol, nbsol2);
            Assert.assertEquals(nbsol2, realNbSol);
        }
    }

    @Test(groups="10s", timeOut=300000)
    public void testRandomProblems2() {
        for (int bigseed = 0; bigseed < 11; bigseed++) {
            long nbsol, nbsol2;
            //nb solutions of the gac constraint
            long realNbSol = randomOcc2(-1, bigseed, true, 1, true);
            //nb solutions of occurrence + enum
            nbsol = randomOcc2(realNbSol, bigseed, true, 3, false);
            //b solutions of occurrences + bound
            nbsol2 = randomOcc2(realNbSol, bigseed, false, 3, false);
            Assert.assertEquals(nbsol, nbsol2);
            Assert.assertEquals(nbsol2, realNbSol);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int n = 2;
        for (int i = 0; i < 500; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            int value = 1;
            IntVar occ = model.intVar("oc", 0, n, true);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.getSolver().setSearch(randomSearch(allvars,i));
            model.among(occ, vars, new int[]{value}).post();
//            SearchMonitorFactory.log(solver, true, true);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        int n = 2;
        for (int i = 0; i < 500; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            int[] values = {1, 2, 0};
            IntVar occ = model.intVar("oc", 0, n, true);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.getSolver().setSearch(randomSearch(allvars,i));
            model.among(occ, vars, values).post();
//            solver.post(getDecomposition(solver, vars, occ, values));
//            SearchMonitorFactory.log(solver, true, true);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("o", 4, new int[]{0, 1, 2, 5});
        int[] values = {1, 2, 0};
        IntVar occ = model.intVar("oc", 0, 4, true);
        model.among(occ, vars, values).post();
        try {
            model.getSolver().propagate();

            vars[0].removeValue(1, Null);
            vars[0].removeValue(2, Null);
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            fail();
        }
        while (model.getSolver().solve()) ;
    }

    public long randomOcc(long nbsol, int seed, boolean enumvar, int nbtest, boolean gac) {
        for (int interseed = 0; interseed < nbtest; interseed++) {
            int nbOcc = 2;
            int nbVar = 9;
            int sizeDom = 4;
            int sizeOccurence = 4;

            Model model = new Model();
            IntVar[] vars;
            if (enumvar) {
                vars = model.intVarArray("e", nbVar, 0, sizeDom, false);
            } else {
                vars = model.intVarArray("e", nbVar, 0, sizeDom, true);
            }

            List<IntVar> lvs = new LinkedList<>();
            lvs.addAll(asList(vars));

            Random rand = new Random(seed);
            for (int i = 0; i < nbOcc; i++) {
                IntVar[] vs = new IntVar[sizeOccurence];
                for (int j = 0; j < sizeOccurence; j++) {
                    IntVar iv = lvs.get(rand.nextInt(lvs.size()));
                    lvs.remove(iv);
                    vs[j] = iv;
                }
                IntVar ivc = lvs.get(rand.nextInt(lvs.size()));
                int val = rand.nextInt(sizeDom);
                if (gac) {
                    getDecomposition(model, vs, ivc, val
                    ).post();
                } else {
                    model.among(ivc, vs, new int[]{val}).post();
                }
            }
            model.scalar(new IntVar[]{vars[0], vars[3]}, new int[]{1, 1}, "=", vars[6]).post();

            model.getSolver().setSearch(randomSearch(vars,seed));
            while (model.getSolver().solve()) ;
            if (nbsol == -1) {
                nbsol = model.getSolver().getSolutionCount();
            } else {
                assertEquals(model.getSolver().getSolutionCount(), nbsol);
            }

        }
        return nbsol;
    }

    public long randomOcc2(long nbsol, int seed, boolean enumvar, int nbtest, boolean gac) {
        for (int interseed = 0; interseed < nbtest; interseed++) {
            int nbOcc = 2;
            int nbVar = 9;
            int sizeDom = 4;
            int sizeOccurence = 2;

            Model model = new Model();
            IntVar[] vars;
            if (enumvar) {
                vars = model.intVarArray("e", nbVar, 0, sizeDom, false);
            } else {
                vars = model.intVarArray("e", nbVar, 0, sizeDom, true);
            }

            List<IntVar> lvs = new LinkedList<>();
            lvs.addAll(asList(vars));

            Random rand = new Random(seed);
            for (int i = 0; i < nbOcc; i++) {
                IntVar[] vs = new IntVar[sizeOccurence];
                for (int j = 0; j < sizeOccurence; j++) {
                    IntVar iv = lvs.get(rand.nextInt(lvs.size()));
                    lvs.remove(iv);
                    vs[j] = iv;
                }
                IntVar ivc = lvs.get(rand.nextInt(lvs.size()));
                int[] values = new int[]{
                        rand.nextInt(sizeDom),
                        rand.nextInt(sizeDom),
                        rand.nextInt(sizeDom)
                };
                if (gac) {
                    getDecomposition(model, vs, ivc, values
                    ).post();
                } else {
                    model.among(ivc, vs, values).post();
                }
            }
//            solver.post(Sum.eq(new IntVar[]{vars[0], vars[3], vars[6]}, new int[]{1, 1, -1}, 0, solver));


            model.getSolver().setSearch(randomSearch(vars,seed));
            while (model.getSolver().solve()) ;
            if (nbsol == -1) {
                nbsol = model.getSolver().getSolutionCount();
            } else {
                assertEquals(model.getSolver().getSolutionCount(), nbsol);
            }

        }
        return nbsol;
    }

    /**
     * generate a table to encode an occurrence constraint.
     *
     * @param vs  array of variables
     * @param occ occurence variable
     * @param val value
     * @return Constraint
     */
    public Constraint getDecomposition(Model model, IntVar[] vs, IntVar occ, int val) {
        BoolVar[] bs = model.boolVarArray("b", vs.length);
        IntVar vval = model.intVar(val);
        for (int i = 0; i < vs.length; i++) {
            model.ifThenElse(bs[i], model.arithm(vs[i], "=", vval), model.arithm(vs[i], "!=", vval));
        }
        return model.sum(bs, "=", occ);
    }

    public Constraint getDecomposition(Model model, IntVar[] vs, IntVar occ, int[] values) {
        BoolVar[] bs = model.boolVarArray("b", vs.length);
        for (int i = 0; i < vs.length; i++) {
            model.ifThenElse(bs[i], model.member(vs[i], values), model.notMember(vs[i], values));
        }
        return model.sum(bs, "=", occ);
    }

}
