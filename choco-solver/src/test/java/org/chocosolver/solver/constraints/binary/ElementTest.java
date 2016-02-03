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
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ICF;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.LCF;
import org.chocosolver.solver.constraints.binary.element.ElementFactory;
import org.chocosolver.solver.explanations.ExplanationFactory;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 31/10/11
 * Time: 14:22
 */
public class ElementTest {

    private static void model(Solver s, IntVar index, int[] values, IntVar var,
                              int offset, int nbSol) {

        s.post(IntConstraintFactory.element(var, values, index, offset, "detect"));

        IntVar[] allvars = ArrayUtils.toArray(index, var);

        if (!(index.hasEnumeratedDomain() && var.hasEnumeratedDomain())) {
            s.set(IntStrategyFactory.random_bound(allvars, System.currentTimeMillis()));
        } else {
            s.set(IntStrategyFactory.random_value(allvars, System.currentTimeMillis()));
        }
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
    }


    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Solver s = new Solver();
        int[] values = new int[]{1, 2, 0, 4, 3};
        IntVar index = s.makeIntVar("v_0", -3, 10, false);
        IntVar var = s.makeIntVar("v_1", -20, 20, false);
        model(s, index, values, var, 0, 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Solver s = new Solver();
        int[] values = new int[]{1, 2, 0, 4, 3};
        IntVar index = s.makeIntVar("v_0", 2, 10, false);
        IntVar var = s.makeIntVar("v_1", -20, 20, false);
        model(s, index, values, var, 0, 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void test3() {
        for (int j = 0; j < 100; j++) {
            Random r = new Random(j);
            Solver s = new Solver();
            IntVar index = s.makeIntVar("v_0", 23, 25, false);
            IntVar val = s.makeIntVar("v_1", 0, 1, true);
            int[] values = new int[24];
            for (int i = 0; i < values.length; i++) {
                values[i] = r.nextInt(2);
            }
            model(s, index, values, val, 0, 1);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void test4() {
        Solver s = new Solver();
        int[] values = new int[]{0, 0, 1};
        IntVar index = s.makeIntVar("v_0", 1, 3, false);
        IntVar var = s.makeIntVar("v_1", 0, 1, false);
        model(s, index, values, var, 1, 3);
    }

    @Test(groups="1s", timeOut=60000)
    public void test5() {
        Solver s = new Solver();
        s.set(new Settings() {
            @Override
            public boolean plugExplanationIn() {
                return true;
            }
        });
        ExplanationFactory.CBJ.plugin(s, false, false);

        Random r = new Random(125);
        int[] values = new int[10];
        for (int i = 0; i < values.length; i++) {
            values[i] = r.nextInt(5);
        }

        IntVar[] vars = new IntVar[3];
        IntVar[] indices = new IntVar[3];
        List<Constraint> lcstrs = new ArrayList<>(1);

        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.makeIntVar("v_" + i, 0, 10, false);
            indices[i] = s.makeIntVar("i_" + i, 0, values.length - 1, false);
            lcstrs.add(IntConstraintFactory.element(vars[i], values, indices[i], 0, "detect"));
        }

        for (int i = 0; i < vars.length - 1; i++) {
            lcstrs.add(IntConstraintFactory.arithm(vars[i], ">", vars[i + 1]));
        }

        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);
        s.post(cstrs);

        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 58, "nb sol");
    }

    public void nasty(int seed, int nbvars, int nbsols) {

        Random r = new Random(seed);
        int[] values = new int[nbvars];
        for (int i = 0; i < values.length; i++) {
            values[i] = r.nextInt(nbvars);
        }


        Solver ref = new Solver();
        IntVar[] varsr = new IntVar[nbvars];
        IntVar[] indicesr = new IntVar[nbvars];
        List<Constraint> lcstrsr = new ArrayList<>(1);

        for (int i = 0; i < varsr.length; i++) {
            varsr[i] = ref.makeIntVar("v_" + i, 0, nbvars, false);
            indicesr[i] = ref.makeIntVar("i_" + i, 0, nbvars, false);
        }
        IntVar[] allvarsr = ArrayUtils.flatten(ArrayUtils.toArray(varsr, indicesr));
        ref.set(IntStrategyFactory.random_value(allvarsr, seed));

        for (int i = 0; i < varsr.length - 1; i++) {
            lcstrsr.add(IntConstraintFactory.element(varsr[i], values, indicesr[i], 0, "detect"));
            lcstrsr.add(IntConstraintFactory.arithm(varsr[i], "+", indicesr[i + 1], "=", 2 * nbvars / 3));
        }

        Constraint[] cstrsr = lcstrsr.toArray(new Constraint[lcstrsr.size()]);
        ref.post(cstrsr);

        ref.findAllSolutions();

        Assert.assertEquals(ref.getMeasures().getSolutionCount(), nbsols);
    }


    @Test(groups="1s", timeOut=60000)
    public void testBUG() {
        nasty(153, 15, 192);
    }


    @Test(groups="1s", timeOut=60000)
    public void testInc1() {
        for (int i = 0; i < 20; i++) {
            Solver solver = new Solver();
            IntVar I = solver.makeIntVar("I", 0, 5, false);
            IntVar R = solver.makeIntVar("R", 0, 10, false);
            solver.post(ICF.element(R, new int[]{0, 2, 4, 6, 7}, I));
            solver.set(ISF.random_value(new IntVar[]{I, R}, i));
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 5);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDec1() {
        for (int i = 0; i < 20; i++) {
            Solver solver = new Solver();
            IntVar I = solver.makeIntVar("I", 0, 5, false);
            IntVar R = solver.makeIntVar("R", 0, 10, false);
            solver.post(ICF.element(R, new int[]{7, 6, 4, 2, 0}, I));
            solver.set(ISF.random_value(new IntVar[]{I, R}, i));
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 5);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testReg1() {
        for (int i = 0; i < 20; i++) {
            Solver solver = new Solver();
            IntVar I = solver.makeIntVar("I", 0, 13, false);
            IntVar R = solver.makeIntVar("R", 0, 21, false);
            solver.post(ICF.element(R, new int[]{1, 6, 20, 4, 15, 13, 9, 3, 19, 12, 17, 7, 17, 5}, I));
            solver.set(ISF.random_value(new IntVar[]{I, R}, i));
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 14);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testTAR1(){
        for (int i = 1; i < 20; i++) {
            Solver solver = new Solver();
            IntVar I = solver.makeIntVar("I", 0, 3, true);
            IntVar R = solver.makeIntVar("R", -1, 0, false);
            solver.post(ICF.element(R, new int[]{-1, -1, -1, 0, -1}, I, -1, "detect"));
            solver.set(ISF.random_bound(new IntVar[]{I, R}, i));
            solver.findAllSolutions();
            Assert.assertEquals(solver.getMeasures().getSolutionCount(), 4);
        }
    }
    @Test
    public void testSolverOrMin() {
        Solver s = new Solver();
        IntVar val = s.makeIntVar("v", 0, 9, true);
        // b=> val={5,6,7,8}[2]
        Constraint el = ElementFactory.detect(val, new int[]{5, 6, 7, 8}, s.makeIntVar(2), 0);
        s.post(LCF.or(el.reif()));
        // s.post(el);// works instead of previous post
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 1L);
    }


    @Test
    public void testSolverOrFull() {
        Solver s = new Solver();
        BoolVar b = s.makeBoolVar("b");
        IntVar val = s.makeIntVar("v", 0, 9, true);
        // b=> val={5,6,7,8}[2]
        Constraint el = ElementFactory.detect(val, new int[]{5, 6, 7, 8}, s.makeIntVar(2), 0);
        s.post(LCF.or(b.not(), el.reif()));
        // !b=> val=2
        Constraint affect = ICF.arithm(val, "=", 2);
        s.post(LCF.or(b, affect.reif()));
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 2L);
    }

}
