/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.cutoffseq;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 13/05/11
 */
public class RestartTest {

    public final static int[] LUBY_2 = {1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 16};

    public final static int[] GEOMETRIC_1_1_3 = {1, 2, 2, 3, 3, 4, 5, 7, 9, 11, 14, 18, 24, 31, 40};

    public final static int[] GEOMETRIC_4_1_3 = {4, 6, 7, 9, 12, 15, 20, 26, 33, 43, 56, 72, 94, 122, 158, 205, 267, 347, 450, 585, 761, 989, 1285, 1671, 2172, 2823, 3670, 4771, 6202, 8062, 10480};
  
    @Test(timeOut=60000)
    public void testLubyRestarts() {
        testCutoffs(new LubyCutoffStrategy(1), LUBY_2, 1);
        testCutoffs(new LubyCutoffStrategy(4), LUBY_2, 4);
    }

    @Test(timeOut=60000)
    public void testGeomRestarts() {
        testCutoffs(new GeometricalCutoffStrategy(1, 1.3), GEOMETRIC_1_1_3, 1);
        testCutoffs(new GeometricalCutoffStrategy(4, 1.3), GEOMETRIC_4_1_3, 1);
    }

    private static void testCutoffs(ICutoffStrategy strat, int[] expected, int scale) {
        for (int i = 0; i < expected.length; i++) {
            Assert.assertEquals(scale * expected[i], strat.getNextCutoff());
        }
    }
}
