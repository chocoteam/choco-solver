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
import org.chocosolver.util.objects.setDataStructures.nonbacktrackable.SetTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Test the backtracking properties of {@link ISet} under two backtracking environments : <code>COPY</code> and
 * and <code>TRAIL</code>.
 * @author Alexandre LEBRUN
 */
public abstract class BacktrackableSetTest extends SetTest{

    protected Model model;

    @BeforeMethod(alwaysRun = true)
    public void init(){
        model = new Model();
    }

    @DataProvider(name = "params")
    public Object[][] data1D(){
        // indicates whether to use hard coded implementation or not
        List<Object[]> elt = new ArrayList<>();
        elt.add(new Object[]{true});
        elt.add(new Object[]{false});
        return elt.toArray(new Object[elt.size()][1]);
    }

    /**
     * Factory enabling to create an empty backtrackable set
     * @return backtracking implementation of {@link ISet}
     */
    public abstract ISet create();

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testNominal(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();
        IEnvironment environment = model.getEnvironment();
        set.add(1);
        environment.worldPush();

        set.remove(1);
        assertFalse(set.contains(1));
        environment.worldPush();

        environment.worldPop();
        environment.worldPop();
        assertTrue(set.contains(1));
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testTwoPushes(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();
        IEnvironment environment = model.getEnvironment();
        environment.worldPush();
        set.add(1);
        environment.worldPush();


        environment.worldPop();
        environment.worldPop();
        assertTrue(set.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testPopUntilZero(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();
        IEnvironment environment = model.getEnvironment();
        environment.worldPush();

        for (int i = 0; i < 100; i++) {
            set.add(i);
            environment.worldPush();
        }

        environment.worldPopUntil(0);
        assertTrue(set.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testSeveralPushes(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();
        IEnvironment environment = model.getEnvironment();
        environment.worldPush();

        for (int i = 0; i < 10000; i++) {
            set.add(i);
            environment.worldPush();
        }

        environment.worldPop();
        for (int i = 10000 - 1; i >= 0; i--) {
            assertTrue(set.contains(i));
            assertFalse(set.contains(i+1));
            environment.worldPop();
        }

        assertEquals(environment.getWorldIndex(), 0);
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testVoidPushes(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();
        IEnvironment environment = model.getEnvironment();
        environment.worldPush();

        set.add(1);

        for (int i = 0; i < 100; i++) {
            environment.worldPush();
        }
        for (int i = 0; i < 100; i++) {
            environment.worldPop();
        }

        environment.worldPop();
        assertTrue(set.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testTwoSets(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet a = create();
        ISet b = create();
        IEnvironment environment = model.getEnvironment();
        environment.worldPush();

        a.add(1);
        environment.worldPush();

        b.add(2);
        environment.worldPush();
        a.add(3); // not read

        environment.worldPop();
        assertTrue(a.contains(1));
        assertFalse(a.contains(2));
        assertFalse(b.contains(1));
        assertTrue(b.contains(2));
        assertFalse(a.contains(3));

        environment.worldPop();
        assertTrue(a.contains(1));
        assertFalse(b.contains(1));
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testAddRemoveReturnValue(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();
        IEnvironment environment = model.getEnvironment();
        environment.worldPush();
        set.add(1);
        environment.worldPush();
        assertFalse(set.add(1));
        assertTrue(set.remove(1));
        assertTrue(set.add(2));
        environment.worldPop();
        environment.worldPop();
        assertFalse(set.contains(2));
        assertFalse(set.remove(1));
    }

    @Test(groups = "1s", timeOut=60000, dataProvider = "params")
    public void testRemoveInLoop(boolean exp) {
        SetFactory.HARD_CODED = exp;
        ISet set = create();

        set.add(1);
        set.add(2);
        set.add(3);
        set.add(7);
        set.add(6);
        set.add(4);

        int size = 0;
        for (Integer integer : set) {
            assertNotNull(integer);
            size++;
        }
        assertEquals(6, size);

        size = 0;
        for (Integer integer : set) {
            if(set.contains(1)){
                set.remove(1);
            }
            assertNotNull(integer);
            size++;
        }
        assertTrue(5 <= size);
        assertEquals(5, set.size());

        size = 0;
        for (Integer integer : set) {
            if(set.contains(6)){
                set.remove(6);
            }
            assertNotNull(integer);
            size++;
        }
        assertTrue(4 <= size);
        assertEquals(4, set.size());
    }

    @Test(groups="10s", timeOut=60000)
    public void memoryCrashTest() {
        SetFactory.HARD_CODED = false;
        ISet set = create();
        assertTrue(set.add(2));
        IEnvironment environment = model.getEnvironment();
        for(int k=0;k<300;k++) {
            environment.worldPush();
            for (int i = 0; i < 1000; i++) set.add(i);
            environment.worldPop();
            assertTrue(set.size() == 1);
        }
    }
}
