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

import choco.kernel.common.util.tools.ArrayUtils;
import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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


    public void nasty(int seed, int nbvars, int nbsols) {

        Random r = new Random(seed);
        int[] values = new int[nbvars];
        for(int i = 0; i < values.length; i++){
            values[i] = r.nextInt(nbvars);
        }


        Solver ref = new Solver();
        IntVar[] varsr = new IntVar[nbvars];
        IntVar[] indicesr = new IntVar[nbvars];
        List<Constraint> lcstrsr = new ArrayList<Constraint>(1);

        for (int i = 0; i < varsr.length; i++) {
            varsr[i] = VariableFactory.enumerated("v_" + i, 0, nbvars, ref);
            indicesr[i] = VariableFactory.enumerated("i_"+i, 0, nbvars,ref);
        }
        IntVar[] allvarsr = ArrayUtils.flatten(ArrayUtils.toArray(varsr, indicesr));
        ref.set(StrategyFactory.random(allvarsr, ref.getEnvironment(), seed));

        for (int i = 0; i < varsr.length - 1 ; i++) {
            lcstrsr.add(new Element(varsr[i], values, indicesr[i], 0, ref));
            lcstrsr.add(new EqualXY_C(varsr[i], indicesr[i+1], 2 * nbvars / 3 , ref));
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
