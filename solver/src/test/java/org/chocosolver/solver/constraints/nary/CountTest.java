/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.DefaultSettings;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.lang.Boolean.TRUE;
import static java.lang.System.arraycopy;
import static java.util.Arrays.asList;
import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;
import static org.chocosolver.solver.search.strategy.Search.randomSearch;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 08/06/11
 */
public class CountTest {

    protected static Model modelit(int n) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("var", n, 0, n - 1, true);
        for (int i = 0; i < n; i++) {
            model.count(i, vars, vars[i]).post();
        }
        model.sum(vars, "=", n).post(); // cstr redundant 1
        int[] coeff2 = new int[n - 1];
        IntVar[] vs2 = new IntVar[n - 1];
        for (int i = 1; i < n; i++) {
            coeff2[i - 1] = i;
            vs2[i - 1] = vars[i];
        }
        model.scalar(vs2, coeff2, "=", n).post(); // cstr redundant 1
        return model;
    }


    @Test(groups="1s", timeOut=60000)
    public void testMS4() {
        Model model = modelit(4);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMS8() {
        Model model = modelit(8);
        while (model.getSolver().solve()) ;
        assertEquals(model.getSolver().getSolutionCount(), 1);
    }

    @Test(groups="10s", timeOut=60000)
    public void testRandomProblems() {
        for (int bigseed = 1; bigseed < 11; bigseed++) {
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

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int n = 2;
        for (int i = 0; i < 200; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            int value = 1;
            IntVar occ = model.intVar("oc", 0, n, true);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.count(value, vars, occ).post();

            Solver r = model.getSolver();
            r.setSearch(randomSearch(allvars,i));

//        solver.post(getTableForOccurence(solver, vars, occ, value, n));
//            SearchMonitorFactory.log(solver, true, true);
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2VE() {
        int n = 2;
        for (int i = 0; i < 20; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            IntVar value = model.intVar(new int[]{-5,1,3});
            IntVar occ = model.intVar("oc", 0, n, true);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.count(value, vars, occ).post();
            model.arithm(value,"=",1).post();
            Solver r = model.getSolver();
            r.setSearch(randomSearch(allvars,i));
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test2VB() {
        int n = 2;
        for (int i = 0; i < 20; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            IntVar value = model.intVar(-5,5);
            IntVar occ = model.intVar("oc", 0, n, true);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.count(value, vars, occ).post();
            model.arithm(value,"=",1).post();
            Solver r = model.getSolver();
            r.setSearch(randomSearch(allvars,i));
            while (model.getSolver().solve()) ;
            assertEquals(model.getSolver().getSolutionCount(), 9);
        }
    }

    public long randomOcc(long nbsol, int seed, boolean enumvar, int nbtest, boolean gac) {
        for (int interseed = 0; interseed < nbtest; interseed++) {
            int nbOcc = 2;
            int nbVar = 9;
            int sizeDom = 4;
            int sizeOccurence = 4;

            Model model = new Model(new DefaultSettings().setHybridizationOfPropagationEngine((byte)0b00));
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
                    getTableForOccurence(vs, ivc, val, sizeDom).post();
                } else {
                    model.count(val, vs, ivc).post();
                }
            }
            model.scalar(new IntVar[]{vars[0], vars[3], vars[6]}, new int[]{1, 1, -1}, "=", 0).post();

            //s.setValIntSelector(new RandomIntValSelector(interseed));
            //s.setVarIntSelector(new RandomIntVarSelector(s, interseed + 10));
//            if (!gac) {
//                SearchMonitorFactory.log(solver, true, true);
//            }

            Solver r = model.getSolver();
            r.setSearch(randomSearch(vars, seed));
            while (model.getSolver().solve()) ;
            if (nbsol == -1) {
                nbsol = r.getMeasures().getSolutionCount();
            } else {
                assertEquals(r.getMeasures().getSolutionCount(), nbsol);
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
     * @param ub  upper bound
     * @return Constraint
     */
    public Constraint getTableForOccurence(IntVar[] vs, IntVar occ, int val, int ub) {
        Model model = new Model();
        IntVar[] vars = model.intVarArray("e", vs.length + 1, 0, ub, false);

        Tuples tuples = new Tuples(true);
        model.getSolver().setSearch(inputOrderLBSearch(vars));
        model.getSolver().solve();
        do {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < tuple.length; i++) {
                tuple[i] = vars[i].getValue();
            }
            int checkocc = 0;
            for (int i = 0; i < (tuple.length - 1); i++) {
                if (tuple[i] == val) checkocc++;
            }
            if (checkocc == tuple[tuple.length - 1]) {
                tuples.add(tuple);
            }
        } while (model.getSolver().solve() == TRUE);

        IntVar[] newvs = new IntVar[vs.length + 1];
        arraycopy(vs, 0, newvs, 0, vs.length);
        newvs[vs.length] = occ;

        return model.table(newvs, tuples);
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

}
