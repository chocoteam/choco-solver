package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * Test the backtracking properties of {@link ISet} under two backtracking environments : <code>COPY</code> and
 * and <code>TRAIL</code>.
 * @author Alexandre LEBRUN
 */
public abstract class BacktrackableSetTest {

    protected Model model;
    protected IEnvironment environment;

    /**
     * Factory enabling to create an empty backtrackable set
     * @return backtracking implementation of {@link ISet}
     */
    public abstract ISet create();


    @Test(groups = "1s", timeOut=60000)
    public void testNominal() {
        ISet set = create();
        set.add(1);
        environment.worldPush();

        set.remove(1);
        assertFalse(set.contain(1));
        environment.worldPush();

        environment.worldPop();
        environment.worldPop();
        assertTrue(set.contain(1));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testTwoPushes() {
        ISet set = create();
        environment.worldPush();
        set.add(1);
        environment.worldPush();


        environment.worldPop();
        environment.worldPop();
        assertTrue(set.isEmpty());
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = AssertionError.class)
    public void testTwoMuchPop() {
        environment.worldPop();
    }

    @Test(groups = "1s", timeOut=60000)
    public void testPopUntilZero() {
        ISet set = create();
        environment.worldPush();

        for (int i = 0; i < 100; i++) {
            set.add(i);
            environment.worldPush();
        }

        environment.worldPopUntil(0);
        assertTrue(set.isEmpty());
    }

    @Test(groups = "10s", timeOut=60000)
    public void testSeveralPushes() {
        ISet set = create();
        environment.worldPush();

        for (int i = 0; i < 10000; i++) {
            set.add(i);
            environment.worldPush();
        }

        environment.worldPop();
        for (int i = 10000 - 1; i >= 0; i--) {
            assertTrue(set.contain(i));
            assertFalse(set.contain(i+1));
            environment.worldPop();
        }

        assertEquals(environment.getWorldIndex(), 0);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testVoidPushes() {
        ISet set = create();
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

    @Test(groups = "1s", timeOut=60000)
    public void testTwoSets() {
        ISet a = create();
        ISet b = create();
        environment.worldPush();

        a.add(1);
        environment.worldPush();

        b.add(2);
        environment.worldPush();
        a.add(3); // not read

        environment.worldPop();
        assertTrue(a.contain(1));
        assertFalse(a.contain(2));
        assertFalse(b.contain(1));
        assertTrue(b.contain(2));
        assertFalse(a.contain(3));

        environment.worldPop();
        assertTrue(a.contain(1));
        assertFalse(b.contain(1));
    }

    @Test(groups = "1s", timeOut=60000)
    public void testAddRemoveReturnValue() {
        ISet set = create();
        environment.worldPush();
        set.add(1);
        environment.worldPush();
        assertFalse(set.add(1));
        assertTrue(set.remove(1));
        assertTrue(set.add(2));
        environment.worldPop();
        environment.worldPop();
        assertFalse(set.contain(2));
        assertFalse(set.remove(1));
    }
}
