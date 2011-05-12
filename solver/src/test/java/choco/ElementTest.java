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

package choco;

import choco.kernel.common.util.tools.ArrayUtils;
import choco.kernel.memory.IEnvironment;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.binary.Element;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20 sept. 2010
 */
public class ElementTest {

    private static void model(Solver s, choco.kernel.memory.IEnvironment env, IntVar index, int[] values, IntVar var, int offset, int nbSol, int nbNod) {
        IntVar[] allvars = ArrayUtils.toArray(index, var);


        List<Constraint> lcstrs = new ArrayList<Constraint>(1);
        lcstrs.add(new Element(index, values, var, offset, s));
        Constraint[] cstrs = lcstrs.toArray(new Constraint[lcstrs.size()]);

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(allvars, env);

        s.post(cstrs);
        s.set(strategy);

        s.findAllSolutions();

        Assert.assertEquals(s.getMeasures().getSolutionCount(), nbSol, "nb sol");
        Assert.assertEquals(s.getMeasures().getNodeCount(), nbNod, "nb nod");
    }

    @Test(groups = "1s")
    public void test1() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();
        int[] values = new int[]{1, 2, 0, 4, 3};
        IntVar index = VariableFactory.enumerated("v_0", -3, 10, s);
        IntVar var = VariableFactory.enumerated("v_1", -20, 20, s);
        model(s, env, index, values, var, 0, 5, 9);
    }

    @Test(groups = "1s")
    public void test2() {
        Solver s = new Solver();
        choco.kernel.memory.IEnvironment env = s.getEnvironment();
        int[] values = new int[]{1, 2, 0, 4, 3};
        IntVar index = VariableFactory.enumerated("v_0", 2, 10, s);
        IntVar var = VariableFactory.enumerated("v_1", -20, 20, s);
        model(s, env, index, values, var, 0, 3,5);
    }

    @Test(groups = "1s")
    public void test3() {
        for(int j = 0; j< 100; j++){
            Random r = new Random(j);
            Solver s = new Solver();
            IEnvironment env = s.getEnvironment();
            IntVar index = VariableFactory.enumerated("v_0", 23, 25, s);
            IntVar val = VariableFactory.bounded("v_1", 0, 1, s);
            int[] values = new int[24];
            for(int i = 0; i < values.length; i++){
                values[i] = r.nextInt(2);
            }
            model(s, env, index, values, val, 0, 1, 1);
        }
    }

    @Test(groups = "1s")
    public void test4() {
        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();
        int[] values = new int[]{0,0,1};
        IntVar index = VariableFactory.enumerated("v_0", 1, 3, s);
        IntVar var = VariableFactory.enumerated("v_1", 0, 1, s);
        model(s, env, index, values, var, 1, 3, 5);
    }

}
