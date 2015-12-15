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
package org.chocosolver.util.tools;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 15/12/2015.
 */
public class MathUtilsTest {

    @Test(groups = "1s")
    public void testDivFloor() throws Exception {
        Assert.assertEquals(Math.floorDiv(3,5), MathUtils.divFloor(3,5));
        Assert.assertEquals(Math.floorDiv(-3,5), MathUtils.divFloor(-3,5));
        Assert.assertEquals(Math.floorDiv(3,-5), MathUtils.divFloor(3,-5));
        Assert.assertEquals(Math.floorDiv(-3,-5), MathUtils.divFloor(-3,-5));
        Assert.assertEquals(Integer.MAX_VALUE, MathUtils.divFloor(10,0));
    }

    @Test(groups = "1s")
    public void testDivCeil() throws Exception {
        Assert.assertEquals(Math.floorDiv(3,5) + 1, MathUtils.divCeil(3,5));
        Assert.assertEquals(Math.floorDiv(-3,5) +1, MathUtils.divCeil(-3,5));
        Assert.assertEquals(Math.floorDiv(3,-5) +1 , MathUtils.divCeil(3,-5));
        Assert.assertEquals(Math.floorDiv(-3,-5)+1, MathUtils.divCeil(-3,-5));
        Assert.assertEquals(MathUtils.divFloor(10,0)+1, MathUtils.divCeil(10,0));
    }


    @Test(groups="1s")
    public void testSafeAdd() {
        Assert.assertEquals(MathUtils.safeAdd(1, 1), 2);
        Assert.assertEquals(MathUtils.safeAdd(Integer.MAX_VALUE, 1), Integer.MAX_VALUE);
        Assert.assertEquals(MathUtils.safeAdd(Integer.MIN_VALUE, -1), Integer.MIN_VALUE);
    }

    @Test(groups="1s")
    public void testSafeSubstract() {
        Assert.assertEquals(MathUtils.safeSubstract(1, 1), 0);
        Assert.assertEquals(MathUtils.safeSubstract(Integer.MIN_VALUE, 1), Integer.MIN_VALUE);
        Assert.assertEquals(MathUtils.safeSubstract(Integer.MAX_VALUE, -1), Integer.MAX_VALUE);
    }

    @Test(groups="1s")
    public void testSafeMultiply() {
        Assert.assertEquals(MathUtils.safeMultiply(1, 1), 1);
        Assert.assertEquals(MathUtils.safeMultiply(Integer.MAX_VALUE, 10), Integer.MAX_VALUE);
        Assert.assertEquals(MathUtils.safeSubstract(Integer.MIN_VALUE, 10), Integer.MIN_VALUE);
    }

}