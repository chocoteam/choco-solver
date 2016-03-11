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
package org.chocosolver.util.objects.setDataStructures.backtrackable.trail;

import org.chocosolver.memory.Environments;
import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public abstract class TrailingSetTest {


    protected Model model;
    protected IEnvironment environment;

    @BeforeMethod
    public void setup() {
        model = new Model(Environments.TRAIL.make(), "trail");
        environment = model.getEnvironment();
    }

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
    public void testOrder() { // TODO order ?
        ISet set = create();
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);

        System.out.println(set.getClass());
        set.forEach(System.out::print);
        System.out.println();
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
