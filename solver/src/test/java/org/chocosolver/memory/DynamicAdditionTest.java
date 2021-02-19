/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
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
                {new EnvironmentBuilder().fromChunk().build()}
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

    @Test(groups="10s", timeOut=300000, dataProvider = "env")
    public void test3(EnvironmentTrailing environment) {
        environment.buildFakeHistoryOn(new Except_0());
        int n = 5000;
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
