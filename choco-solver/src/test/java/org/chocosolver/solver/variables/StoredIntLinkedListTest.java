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
package org.chocosolver.solver.variables;

import org.chocosolver.memory.trailing.EnvironmentTrailing;
import org.chocosolver.util.objects.setDataStructures.linkedlist.Set_Std_LinkedList;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 09/02/11
 */
public class StoredIntLinkedListTest {

    @Test(groups = "1s")
    public void test1() {
        EnvironmentTrailing environment = new EnvironmentTrailing();
        Set_Std_LinkedList llist = new Set_Std_LinkedList(environment);

        Assert.assertFalse(llist.contain(1));
        Assert.assertFalse(llist.contain(2));
        Assert.assertFalse(llist.contain(3));

        environment.worldPush();

        llist.add(1);
        llist.add(2);
        Assert.assertTrue(llist.contain(1));
        Assert.assertTrue(llist.contain(2));
        Assert.assertFalse(llist.contain(3));

        environment.worldPop();

        Assert.assertFalse(llist.contain(1));
        Assert.assertFalse(llist.contain(2));
        Assert.assertFalse(llist.contain(3));
        llist.add(1);
        llist.add(2);
        Assert.assertTrue(llist.contain(1));
        Assert.assertTrue(llist.contain(2));
        Assert.assertFalse(llist.contain(3));

        environment.worldPush();

        Assert.assertTrue(llist.contain(1));
        Assert.assertTrue(llist.contain(2));
        Assert.assertFalse(llist.contain(3));
        llist.remove(2);
        llist.add(3);
        Assert.assertTrue(llist.contain(1));
        Assert.assertFalse(llist.contain(2));
        Assert.assertTrue(llist.contain(3));

        environment.worldPop();

        Assert.assertTrue(llist.contain(1));
        Assert.assertTrue(llist.contain(2));
        Assert.assertFalse(llist.contain(3));

//		// backtracking above root node makes no sense
//        environment.worldPop();
//        Assert.assertFalse(llist.contain(1));
//        Assert.assertFalse(llist.contain(2));
//        Assert.assertFalse(llist.contain(3));

    }

    @Test(groups = "1s")
    public void test2() {
        EnvironmentTrailing environment = new EnvironmentTrailing();
        Set_Std_LinkedList llist = new Set_Std_LinkedList(environment);

        int n = 100;

        for (int i = 0; i < n; i++) {
            Assert.assertFalse(llist.contain(i));
            Assert.assertFalse(llist.contain(i + 1));

            llist.add(i);
            Assert.assertTrue(llist.contain(i));

            environment.worldPush();
            llist.remove(i);
            llist.add(i + 1);

            environment.worldPush();
            Assert.assertFalse(llist.contain(i));
            Assert.assertTrue(llist.contain(i + 1));
            environment.worldPop();
            Assert.assertFalse(llist.contain(i));
            Assert.assertTrue(llist.contain(i + 1));
            environment.worldPop();
            Assert.assertTrue(llist.contain(i));
        }
        for (int i = 0; i < n; i++) {
            Assert.assertTrue(llist.contain(i));
        }

    }

    @Test(groups = "10s")
    public void test3() {
        EnvironmentTrailing environment = new EnvironmentTrailing();
        Set_Std_LinkedList llist = new Set_Std_LinkedList(environment);

        int n = 49999;

        environment.worldPush();
        for (int i = 0; i < n; i++) {
            llist.add(i);
            Assert.assertTrue(llist.contain(i));
            environment.worldPush();
        }
        environment.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            Assert.assertTrue(llist.contain(i));
            environment.worldPop();
            Assert.assertFalse(llist.contain(i));
        }

    }

}
