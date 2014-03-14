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
package solver.explanations;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.explanations.antidom.AntiDomBitset;
import solver.explanations.antidom.AntiDomInterval;
import solver.explanations.antidom.AntiDomain;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.iterators.DisposableValueIterator;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/01/13
 */
public class AntiDomainTest {

    @Test(groups = "1s")
    public void test01() {
        Solver solver = new Solver();
        IntVar v = VariableFactory.enumerated("A", 1, 5, solver);
        AntiDomain ad = new AntiDomBitset(v);
        Assert.assertFalse(ad.get(1));
        Assert.assertFalse(ad.get(2));
        Assert.assertFalse(ad.get(3));
        Assert.assertFalse(ad.get(4));
        Assert.assertFalse(ad.get(5));
        solver.getEnvironment().worldPush();
        ad.add(1);
        ad.add(5);
        Assert.assertTrue(ad.get(1));
        Assert.assertFalse(ad.get(2));
        Assert.assertFalse(ad.get(3));
        Assert.assertFalse(ad.get(4));
        Assert.assertTrue(ad.get(5));
        solver.getEnvironment().worldPush();
        ad.add(3);
        DisposableValueIterator values = ad.getValueIterator();
        while (values.hasNext()) {
            Assert.assertTrue(ad.get(values.next()));
        }
        Assert.assertTrue(ad.get(1));
        Assert.assertFalse(ad.get(2));
        Assert.assertTrue(ad.get(3));
        Assert.assertFalse(ad.get(4));
        Assert.assertTrue(ad.get(5));
        solver.getEnvironment().worldPop();
        Assert.assertTrue(ad.get(1));
        Assert.assertFalse(ad.get(2));
        Assert.assertFalse(ad.get(3));
        Assert.assertFalse(ad.get(4));
        Assert.assertTrue(ad.get(5));
        solver.getEnvironment().worldPop();
        Assert.assertFalse(ad.get(1));
        Assert.assertFalse(ad.get(2));
        Assert.assertFalse(ad.get(3));
        Assert.assertFalse(ad.get(4));
        Assert.assertFalse(ad.get(5));
    }

    @Test(groups = "1s")
    public void test02() {
        Solver solver = new Solver();
        IntVar v = VariableFactory.enumerated("A", 1, 10, solver);
        AntiDomain ad = new AntiDomInterval(v);
        for (int i = 1; i < 11; i++) {
            Assert.assertFalse(ad.get(i));
        }
        solver.getEnvironment().worldPush();
        ad.updateLowerBound(1, 4);
        ad.updateLowerBound(4, 5);
        ad.updateUpperBound(10, 7);
        Assert.assertTrue(ad.get(1));
        Assert.assertTrue(ad.get(2));
        Assert.assertTrue(ad.get(3));
        Assert.assertTrue(ad.get(4));
        Assert.assertFalse(ad.get(5));
        Assert.assertFalse(ad.get(6));
        Assert.assertFalse(ad.get(7));
        Assert.assertTrue(ad.get(8));
        Assert.assertTrue(ad.get(9));
        Assert.assertTrue(ad.get(10));
        solver.getEnvironment().worldPush();
        DisposableValueIterator values = ad.getValueIterator();
        while (values.hasNext()) {
            Assert.assertTrue(ad.get(values.next()));
        }
        solver.getEnvironment().worldPop();
        Assert.assertTrue(ad.get(1));
        Assert.assertTrue(ad.get(2));
        Assert.assertTrue(ad.get(3));
        Assert.assertTrue(ad.get(4));
        Assert.assertFalse(ad.get(5));
        Assert.assertFalse(ad.get(6));
        Assert.assertFalse(ad.get(7));
        Assert.assertTrue(ad.get(8));
        Assert.assertTrue(ad.get(9));
        Assert.assertTrue(ad.get(10));
        solver.getEnvironment().worldPop();
        for (int i = 1; i < 11; i++) {
            Assert.assertFalse(ad.get(i));
        }
    }
}
