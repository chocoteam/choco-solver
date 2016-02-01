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
package org.chocosolver.memory; /**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
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

import org.chocosolver.memory.copy.EnvironmentCopying;
import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/04/2014
 */
public class BasicIndexBipartiteSetTest {


    @Test(groups="1s", timeOut=60000)
    public void testBasicIndexedBipartiteSet(){
        try{
            testBasicIndexedBipartiteSet(Environments.TRAIL.make());
            testBasicIndexedBipartiteSet(Environments.COPY.make());
            testBasicIndexedBipartiteSet(Environments.DEFAULT.make());
        }catch (Exception e){// hack for surefire maven pluggin, which does not support all exceptions
            Assert.assertTrue(false);
        }
    }

    public void testBasicIndexedBipartiteSet(IEnvironment env) {
        BasicIndexedBipartiteSet set = new BasicIndexedBipartiteSet(env, 2);

        int b1 = set.add();
        int b2 = set.add();
        int b3 = set.add();
        int b4 = set.add();

        set.swap(b2);

        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertTrue(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));

        env.worldPush();
        set.swap(b3); // b3 is now fixed
        int b5 = set.add();

        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertFalse(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));

        env.worldPush();

        set.swap(b1); // b1 is now fixed

        int b6 = set.add();
        set.swap(b6);

        Assert.assertFalse(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertFalse(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertFalse(set.bundle(b6));

        // go back
        env.worldPop();
        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertFalse(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertTrue(set.bundle(b6));

        env.worldPop();
        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertTrue(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertTrue(set.bundle(b6));

        env.worldPop();
        Assert.assertTrue(set.bundle(b1));
        Assert.assertFalse(set.bundle(b2));
        Assert.assertTrue(set.bundle(b3));
        Assert.assertTrue(set.bundle(b4));
        Assert.assertTrue(set.bundle(b5));
        Assert.assertTrue(set.bundle(b6));
    }
}
