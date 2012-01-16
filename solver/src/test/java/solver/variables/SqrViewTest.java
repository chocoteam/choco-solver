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
package solver.variables;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Cause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.view.Views;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class SqrViewTest {


    @Test(groups = "1s")
    public void test1() {
        Solver solver = new Solver();

        IntVar X = VariableFactory.enumerated("X", -4, 12, solver);
        IntVar Z = Views.sqr(X);

        try {
            solver.propagate();
            Assert.assertFalse(Z.instantiated());
            Assert.assertEquals(Z.getLB(), 0);
            Assert.assertEquals(Z.getUB(), 144);
            Assert.assertTrue(Z.contains(9));
            Assert.assertEquals(Z.nextValue(9), 16);
            Assert.assertEquals(Z.nextValue(18), 25);
            Assert.assertEquals(Z.nextValue(143), 144);
            Assert.assertEquals(Z.nextValue(145), Integer.MAX_VALUE);
            Assert.assertEquals(Z.previousValue(145), 144);
            Assert.assertEquals(Z.previousValue(144), 121);
            Assert.assertEquals(Z.previousValue(118), 100);
            Assert.assertEquals(Z.previousValue(-1), Integer.MIN_VALUE);

            Z.updateLowerBound(9, Cause.Null, false);
            Assert.assertEquals(X.getLB(), -4);
            Assert.assertEquals(X.getUB(), 12);

            Z.updateUpperBound(100, Cause.Null, false);
            Assert.assertEquals(X.getUB(), 10);
            Assert.assertEquals(X.getLB(), -4);

            Z.removeValue(16, Cause.Null);
            Assert.assertFalse(X.contains(-4));
            Assert.assertFalse(X.contains(4));

            Z.removeInterval(36, 64, Cause.Null, false);
            Assert.assertFalse(X.contains(6));
            Assert.assertFalse(X.contains(7));
            Assert.assertFalse(X.contains(8));

            Assert.assertEquals(X.getDomainSize(), 5);
            Assert.assertEquals(Z.getDomainSize(), X.getDomainSize());

            Z.instantiateTo(25, Cause.Null, false);
            Assert.assertTrue(X.instantiated());
            Assert.assertEquals(X.getValue(), 5);

        } catch (ContradictionException ex) {
            Assert.fail();
        }
    }
}
