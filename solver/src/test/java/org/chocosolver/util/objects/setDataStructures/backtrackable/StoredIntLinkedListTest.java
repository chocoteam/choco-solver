/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/02/11
 */
public class StoredIntLinkedListTest {

    @Test(groups="1s", timeOut=60000)
    public void test1() {
        Model m = new Model();
        IEnvironment environment = m.getEnvironment();
        ISet llist = SetFactory.makeStoredSet(SetType.LINKED_LIST,0,m);

        Assert.assertFalse(llist.contains(1));
        Assert.assertFalse(llist.contains(2));
        Assert.assertFalse(llist.contains(3));

        environment.worldPush();

        llist.add(1);
        llist.add(2);
        Assert.assertTrue(llist.contains(1));
        Assert.assertTrue(llist.contains(2));
        Assert.assertFalse(llist.contains(3));

        environment.worldPop();

        Assert.assertFalse(llist.contains(1));
        Assert.assertFalse(llist.contains(2));
        Assert.assertFalse(llist.contains(3));
        llist.add(1);
        llist.add(2);
        Assert.assertTrue(llist.contains(1));
        Assert.assertTrue(llist.contains(2));
        Assert.assertFalse(llist.contains(3));

        environment.worldPush();

        Assert.assertTrue(llist.contains(1));
        Assert.assertTrue(llist.contains(2));
        Assert.assertFalse(llist.contains(3));
        llist.remove(2);
        llist.add(3);
        Assert.assertTrue(llist.contains(1));
        Assert.assertFalse(llist.contains(2));
        Assert.assertTrue(llist.contains(3));

        environment.worldPop();

        Assert.assertTrue(llist.contains(1));
        Assert.assertTrue(llist.contains(2));
        Assert.assertFalse(llist.contains(3));

//		// backtracking above root node makes no sense
//        environment.worldPop();
//        Assert.assertFalse(llist.contain(1));
//        Assert.assertFalse(llist.contain(2));
//        Assert.assertFalse(llist.contain(3));

    }

    @Test(groups="1s", timeOut=60000)
    public void test2() {
        Model m = new Model();
        IEnvironment environment = m.getEnvironment();
        ISet llist = SetFactory.makeStoredSet(SetType.LINKED_LIST,0,m);

        int n = 100;

        for (int i = 0; i < n; i++) {
            Assert.assertFalse(llist.contains(i));
            Assert.assertFalse(llist.contains(i + 1));

            llist.add(i);
            Assert.assertTrue(llist.contains(i));

            environment.worldPush();
            llist.remove(i);
            llist.add(i + 1);

            environment.worldPush();
            Assert.assertFalse(llist.contains(i));
            Assert.assertTrue(llist.contains(i + 1));
            environment.worldPop();
            Assert.assertFalse(llist.contains(i));
            Assert.assertTrue(llist.contains(i + 1));
            environment.worldPop();
            Assert.assertTrue(llist.contains(i));
        }
        for (int i = 0; i < n; i++) {
            Assert.assertTrue(llist.contains(i));
        }

    }

    @Test(groups="10s", timeOut=60000)
    public void test3() {
        Model m = new Model();
        IEnvironment environment = m.getEnvironment();
        ISet llist = SetFactory.makeStoredSet(SetType.LINKED_LIST,0,m);

        int n = 49999;

        environment.worldPush();
        for (int i = 0; i < n; i++) {
            llist.add(i);
            Assert.assertTrue(llist.contains(i));
            environment.worldPush();
        }
        environment.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            Assert.assertTrue(llist.contains(i));
            environment.worldPop();
            Assert.assertFalse(llist.contains(i));
        }
    }
}
