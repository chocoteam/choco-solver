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

package solver.constraints.ternary;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/02/11
 */
public class TimesTest {

    @Test(groups = "1s")
    public void test1() {
        Solver s = new Solver();

        IntVar X, Y, Z;
        X = VariableFactory.enumerated("X", new int[]{1, 2}, s);
        Y = VariableFactory.enumerated("Y", new int[]{2, 4}, s);
        Z = VariableFactory.enumerated("Z", new int[]{2,3,4,5}, s);

        s.post(new Times(X, Y, Z, s));

        AbstractStrategy strategy = StrategyFactory.inputOrderMinVal(new IntVar[]{X,Y,Z}, s.getEnvironment());
        s.set(strategy);
//        SearchMonitorFactory.log(s, true, false);
        s.findAllSolutions();
        Assert.assertEquals(s.getMeasures().getSolutionCount(), 3);
        Assert.assertEquals(s.getMeasures().getNodeCount(), 5);
    }

}
