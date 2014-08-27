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

package solver.constraints.binary;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Configuration;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.explanations.ExplanationFactory;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

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

		if(!(index.hasEnumeratedDomain() && var.hasEnumeratedDomain())){
			s.set(IntStrategyFactory.random_bound(allvars, System.currentTimeMillis()));
		}else{
			s.set(IntStrategyFactory.random_value(allvars, System.currentTimeMillis()));
		}
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
    }


    @Test(groups = "1s")
    public void test1() {
        Solver s = new Solver();
        int[] values = new int[]{1, 2, 0, 4, 3};
        IntVar index = VariableFactory.enumerated("v_0", -3, 10, s);
        IntVar var = VariableFactory.enumerated("v_1", -20, 20, s);
        model(s, index, values, var, 0, 5);
    }

    @Test(groups = "1s")
    public void test2() {
        Solver s = new Solver();
        int[] values = new int[]{1, 2, 0, 4, 3};
        IntVar index = VariableFactory.enumerated("v_0", 2, 10, s);
        IntVar var = VariableFactory.enumerated("v_1", -20, 20, s);
        model(s, index, values, var, 0, 3);
    }

    @Test(groups = "1s")
    public void test3() {
        for (int j = 0; j < 100; j++) {
            Random r = new Random(j);
            Solver s = new Solver();
            IntVar index = VariableFactory.enumerated("v_0", 23, 25, s);
            IntVar val = VariableFactory.bounded("v_1", 0, 1, s);
            int[] values = new int[24];
            for (int i = 0; i < values.length; i++) {
                values[i] = r.nextInt(2);
            }
            model(s, index, values, val, 0, 1);
        }
    }

    @Test(groups = "1s")
    public void test4() {
        Solver s = new Solver();
        int[] values = new int[]{0, 0, 1};
        IntVar index = VariableFactory.enumerated("v_0", 1, 3, s);
        IntVar var = VariableFactory.enumerated("v_1", 0, 1, s);
        model(s, index, values, var, 1, 3);
    }

    @Test(groups = "1s")
    public void test5() {
        if (Configuration.PLUG_EXPLANATION) {
            Solver s = new Solver();
            ExplanationFactory.CBJ.plugin(s, false);

            Random r = new Random(125);
            int[] values = new int[10];
            for (int i = 0; i < values.length; i++) {
                values[i] = r.nextInt(5);
            }

            IntVar[] vars = new IntVar[3];
            IntVar[] indices = new IntVar[3];
            List<Constraint> lcstrs = new ArrayList<Constraint>(1);

            for (int i = 0; i < vars.length; i++) {
                vars[i] = VariableFactory.enumerated("v_" + i, 0, 10, s);
                indices[i] = VariableFactory.enumerated("i_" + i, 0, values.length - 1, s);
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
        List<Constraint> lcstrsr = new ArrayList<Constraint>(1);

        for (int i = 0; i < varsr.length; i++) {
            varsr[i] = VariableFactory.enumerated("v_" + i, 0, nbvars, ref);
            indicesr[i] = VariableFactory.enumerated("i_" + i, 0, nbvars, ref);
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


    @Test(groups = "1s")
    public void testBUG() {
        nasty(153, 15, 192);
    }

}
