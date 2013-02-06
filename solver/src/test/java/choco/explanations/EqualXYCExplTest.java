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

package choco.explanations;

import choco.kernel.common.util.tools.ArrayUtils;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 30/10/11
 * Time: 19:10
 */
public class EqualXYCExplTest {

    public void model(int seed, int nbvars) {

        Random r = new Random(seed);
        int[] values = new int[nbvars];
        for (int i = 0; i < values.length; i++) {
            values[i] = r.nextInt(nbvars);
        }

        Solver ref = new Solver();
        Solver sol = new Solver();
        sol.set(ExplanationFactory.engineFactory(sol, true, false));

        IntVar[] varsr = new IntVar[nbvars];
        IntVar[] indicesr = new IntVar[nbvars];
        List<Constraint> lcstrsr = new ArrayList<Constraint>(1);
        IntVar[] varss = new IntVar[nbvars];
        IntVar[] indicess = new IntVar[nbvars];
        List<Constraint> lcstrss = new ArrayList<Constraint>(1);

        for (int i = 0; i < varsr.length; i++) {
            varsr[i] = VariableFactory.enumerated("v_" + i, 0, nbvars, ref);
            indicesr[i] = VariableFactory.enumerated("i_" + i, 0, nbvars, ref);
            varss[i] = VariableFactory.enumerated("v_" + i, 0, nbvars, sol);
            indicess[i] = VariableFactory.enumerated("i_" + i, 0, nbvars, sol);
        }
        IntVar[] allvarsr = ArrayUtils.flatten(ArrayUtils.toArray(varsr, indicesr));
        ref.set(IntStrategyFactory.force_InputOrder_InDomainMin(allvarsr));

        IntVar[] allvarss = ArrayUtils.flatten(ArrayUtils.toArray(varss, indicess));
        sol.set(IntStrategyFactory.force_InputOrder_InDomainMin(allvarss));


        for (int i = 0; i < varsr.length - 1; i++) {
            lcstrsr.add(IntConstraintFactory.element(varsr[i], values, indicesr[i], 0));
            lcstrsr.add(IntConstraintFactory.arithm(varsr[i], "+", indicesr[i + 1], "=", 2 * nbvars / 3));
            lcstrss.add(IntConstraintFactory.element(varss[i], values, indicess[i], 0));
            lcstrss.add(IntConstraintFactory.arithm(varss[i], "+", indicess[i + 1], "=", 2 * nbvars / 3));
        }

        Constraint[] cstrsr = lcstrsr.toArray(new Constraint[lcstrsr.size()]);
        ref.post(cstrsr);

        Constraint[] cstrss = lcstrss.toArray(new Constraint[lcstrss.size()]);
        sol.post(cstrss);

        ref.findAllSolutions();
        sol.findAllSolutions();


        Assert.assertEquals(sol.getMeasures().getSolutionCount(), ref.getMeasures().getSolutionCount());
        Assert.assertTrue(sol.getMeasures().getBackTrackCount() <= ref.getMeasures().getBackTrackCount());
    }

    @Test(groups = "10s")
    public void test1() {
        if (Configuration.PLUG_EXPLANATION) {
            model(125, 4);
            model(125, 10);
            model(153, 15);
            model(1234, 12);
        }
    }
}
