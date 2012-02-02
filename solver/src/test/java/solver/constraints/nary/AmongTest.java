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
package solver.constraints.nary;

import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.binary.EqualX_YC;
import solver.constraints.binary.NotEqualX_YC;
import solver.constraints.reified.ReifiedConstraint;
import solver.constraints.unary.Member;
import solver.constraints.unary.NotMember;
import solver.search.strategy.StrategyFactory;
import solver.variables.BoolVar;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import solver.variables.view.Views;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/02/12
 */
public class AmongTest {

    @Test
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

    @Test
    public void testRandomProblems2() {
        for (int bigseed = 0; bigseed < 11; bigseed++) {
            long nbsol, nbsol2;
            //nb solutions of the gac constraint
            long realNbSol = randomOcc2(-1, bigseed, true, 1, true);
            //nb solutions of occurrence + enum
            nbsol = randomOcc2(realNbSol, bigseed, true, 3, false);
            //b solutions of occurrences + bound
            nbsol2 = randomOcc2(realNbSol, bigseed, false, 3, false);
//            Assert.assertEquals(nbsol, nbsol2);
            Assert.assertEquals(nbsol2, realNbSol);
        }
    }

    @Test
    public void test2() {
        int n = 2;
        for (int i = 0; i < 500; i++) {
            Solver solver = new Solver();
            IntVar[] vars = VariableFactory.boundedArray("o", n, 0, n, solver);
            int value = 1;
            IntVar occ = VariableFactory.bounded("oc", 0, n, solver);
            IntVar[] allvars = ArrayUtils.append(vars, new IntVar[]{occ});
            solver.set(StrategyFactory.random(allvars, solver.getEnvironment(), i));
            solver.post(new Among(value, vars, occ, solver));
//            SearchMonitorFactory.log(solver, true, true);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
        }
    }

    @Test
    public void test3() {
        int n = 2;
        for (int i = 0; i < 500; i++) {
            Solver solver = new Solver();
            IntVar[] vars = VariableFactory.boundedArray("o", n, 0, n, solver);
            int[] values = {1, 2, 0};
            IntVar occ = VariableFactory.bounded("oc", 0, n, solver);
            IntVar[] allvars = ArrayUtils.append(vars, new IntVar[]{occ});
            solver.set(StrategyFactory.random(allvars, solver.getEnvironment(), i));
            solver.post(new Among(values, vars, occ, solver));
//            solver.post(getDecomposition(solver, vars, occ, values));
//            SearchMonitorFactory.log(solver, true, true);
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 9);
        }
    }

    public long randomOcc(long nbsol, int seed, boolean enumvar, int nbtest, boolean gac) {
        for (int interseed = 0; interseed < nbtest; interseed++) {
            int nbOcc = 2;
            int nbVar = 9;
            int sizeDom = 4;
            int sizeOccurence = 4;

            Solver solver = new Solver();
            IntVar[] vars;
            if (enumvar) {
                vars = VariableFactory.enumeratedArray("e", nbVar, 0, sizeDom, solver);
            } else {
                vars = VariableFactory.boundedArray("e", nbVar, 0, sizeDom, solver);
            }

            List<IntVar> lvs = new LinkedList<IntVar>();
            lvs.addAll(Arrays.asList(vars));

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
                    solver.post(getDecomposition(solver, vs, ivc, val
                    ));
                } else {
                    solver.post(new Among(val, vs, ivc, solver));
                }
            }
            solver.post(Sum.eq(new IntVar[]{vars[0], vars[3], vars[6]}, new int[]{1, 1, -1}, 0, solver));

            solver.set(StrategyFactory.random(vars, solver.getEnvironment(), seed));
            solver.findAllSolutions();
            if (nbsol == -1) {
                nbsol = solver.getMeasures().getSolutionCount();
            } else {
                Assert.assertEquals(solver.getMeasures().getSolutionCount(), nbsol);
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

            Solver solver = new Solver();
            IntVar[] vars;
            if (enumvar) {
                vars = VariableFactory.enumeratedArray("e", nbVar, 0, sizeDom, solver);
            } else {
                vars = VariableFactory.boundedArray("e", nbVar, 0, sizeDom, solver);
            }

            List<IntVar> lvs = new LinkedList<IntVar>();
            lvs.addAll(Arrays.asList(vars));

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
                    solver.post(getDecomposition(solver, vs, ivc, values
                    ));
                } else {
                    solver.post(new Among(values, vs, ivc, solver));
                }
            }
//            solver.post(Sum.eq(new IntVar[]{vars[0], vars[3], vars[6]}, new int[]{1, 1, -1}, 0, solver));

            solver.set(StrategyFactory.random(vars, solver.getEnvironment(), seed));
            solver.findAllSolutions();
            if (nbsol == -1) {
                nbsol = solver.getMeasures().getSolutionCount();
            } else {
                Assert.assertEquals(solver.getMeasures().getSolutionCount(), nbsol);
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
    public Constraint getDecomposition(Solver solver, IntVar[] vs, IntVar occ, int val) {
        BoolVar[] bs = VariableFactory.boolArray("b", vs.length, solver);
        IntVar vval = Views.fixed(val, solver);
        for (int i = 0; i < vs.length; i++) {
            solver.post(new ReifiedConstraint(bs[i], new EqualX_YC(vs[i], vval, 0, solver),
                    new NotEqualX_YC(vs[i], vval, 0, solver), solver));
        }
        return Sum.eq(bs, occ, solver);
    }

    public Constraint getDecomposition(Solver solver, IntVar[] vs, IntVar occ, int[] values) {
        BoolVar[] bs = VariableFactory.boolArray("b", vs.length, solver);
        for (int i = 0; i < vs.length; i++) {
            solver.post(new ReifiedConstraint(bs[i], new Member(vs[i], values, solver),
                    new NotMember(vs[i], values, solver), solver));
        }
        return Sum.eq(bs, occ, solver);
    }

}
