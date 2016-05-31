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
package org.chocosolver.memory;

import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 25/03/2014
 */
public class DynamicAdditionTest {

    @DataProvider(name = "env")
    public Object[][] getEnvs(){
        return new EnvironmentTrailing[][]{
                {new EnvironmentBuilder().fromFlat().build()},
                {new EnvironmentBuilder().fromChunk().build()},
                {new EnvironmentBuilder().fromUnsafe().build()}
        };
    }


    @Test(groups="1s", timeOut=60000, dataProvider = "env")
    public void test1(EnvironmentTrailing environment) {
        environment.buildFakeHistoryOn(new Except_0());
        IStateInt a = environment.makeInt(10);
        a.set(11);
        environment.worldPush();

        IStateInt b = environment.makeInt(21);
        a.set(12);
        b.set(22);
        environment.worldPush();

        IStateInt c = environment.makeInt(32);
        a.set(13);
        b.set(23);
        c.set(33);
        environment.worldPush();

        IStateInt d = environment.makeInt(43);
        a.set(14);
        b.set(24);
        c.set(34);
        d.set(44);
        environment.worldPush();

        a.set(15);
        b.set(25);
        c.set(35);
        d.set(45);


        Assert.assertEquals(a.get(), 15);
        Assert.assertEquals(b.get(), 25);
        Assert.assertEquals(c.get(), 35);
        Assert.assertEquals(d.get(), 45);

        // then roll back and assert
        environment.worldPop();
        Assert.assertEquals(a.get(), 14);
        Assert.assertEquals(b.get(), 24);
        Assert.assertEquals(c.get(), 34);
        Assert.assertEquals(d.get(), 44);

        environment.worldPop();
        Assert.assertEquals(a.get(), 13);
        Assert.assertEquals(b.get(), 23);
        Assert.assertEquals(c.get(), 33);
        Assert.assertEquals(d.get(), 43);

        environment.worldPop();
        Assert.assertEquals(a.get(), 12);
        Assert.assertEquals(b.get(), 22);
        Assert.assertEquals(c.get(), 32);
        Assert.assertEquals(d.get(), 43);

        environment.worldPop();
        Assert.assertEquals(a.get(), 11);
        Assert.assertEquals(b.get(), 21);
        Assert.assertEquals(c.get(), 32);
        Assert.assertEquals(d.get(), 43);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "env")
    public void test2(EnvironmentTrailing environment){
        environment.buildFakeHistoryOn(new Except_0());
        int n = 100;
        int m = 100;
        int k = 100;
        IStateInt[] si = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            si[i] = environment.makeInt(i);
        }
        for (int w = 0; w < k; w++) {
            environment.worldPush();
            for (int i = 0; i < n; i++) {
                si[i].add(1);
            }
        }
        IStateInt[] si2 = new IStateInt[m];
        for (int i = 0; i < m; i++) {
            si2[i] = environment.makeInt(-i);
            si2[i].set(100);
        }
        for (int w = 0; w < k; w++) {
            environment.worldPop();
        }
        for (int i = 0; i < m; i++) {
            Assert.assertEquals(si2[i].get(), -i);
        }
    }

    @Test(groups="5m", timeOut=300000, dataProvider = "env")
    public void test3(EnvironmentTrailing environment) {
        environment.buildFakeHistoryOn(new Except_0());
        int n = 50000;
        int m = 3000;
        int k = 100;
        IStateInt[] si = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            si[i] = environment.makeInt(i);
        }
        for (int w = 0; w < k; w++) {
            environment.worldPush();
            for (int i = 0; i < n; i++) {
                si[i].add(1);
            }
        }
        IStateInt[] si2 = new IStateInt[m];
        for (int i = 0; i < m; i++) {
            si2[i] = environment.makeInt(-i);
            si2[i].set(100);
        }
        for (int w = 0; w < k; w++) {
            environment.worldPop();
        }
        for (int i = 0; i < m; i++) {
            Assert.assertEquals(si2[i].get(), -i);
        }
    }
}
