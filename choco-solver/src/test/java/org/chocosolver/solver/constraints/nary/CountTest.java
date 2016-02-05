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
package org.chocosolver.solver.constraints.nary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static java.util.Arrays.asList;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random_bound;
import static org.chocosolver.solver.search.strategy.IntStrategyFactory.random_value;
import static org.chocosolver.util.tools.ArrayUtils.append;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
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
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 2);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMS8() {
        Model model = modelit(8);
        model.solveAll();
        Assert.assertEquals(model.getMeasures().getSolutionCount(), 1);
    }

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

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        int n = 2;
        for (int i = 0; i < 200; i++) {
            Model model = new Model();
            IntVar[] vars = model.intVarArray("o", n, 0, n, true);
            int value = 1;
            IntVar occ = model.intVar("oc", 0, n, true);
            IntVar[] allvars = append(vars, new IntVar[]{occ});
            model.set(random_bound(allvars, i));
            model.count(value, vars, occ).post();
//        solver.post(getTableForOccurence(solver, vars, occ, value, n));
//            SearchMonitorFactory.log(solver, true, true);
            model.solveAll();
            assertEquals(model.getMeasures().getSolutionCount(), 9);
        }
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

            if (!enumvar) {
                model.set(random_bound(vars, seed));
            } else {
                model.set(random_value(vars, seed));
            }
            model.solveAll();
            if (nbsol == -1) {
                nbsol = model.getMeasures().getSolutionCount();
            } else {
                assertEquals(model.getMeasures().getSolutionCount(), nbsol);
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
        model.set(IntStrategyFactory.lexico_LB(vars));
        model.solve();
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
        } while (model.solve() == Boolean.TRUE);

        IntVar[] newvs = new IntVar[vs.length + 1];
        System.arraycopy(vs, 0, newvs, 0, vs.length);
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
