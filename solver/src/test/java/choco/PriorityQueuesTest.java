/**
*  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

package choco;

import org.testng.Assert;
import org.testng.annotations.Test;
import solver.propagation.IQueable;
import solver.propagation.engines.queues.PriorityQueues;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 17 sept. 2010
 */
public class PriorityQueuesTest {

    private static class Prio implements IQueable {
        public int id;
        boolean queue = false;

        private Prio(int id) {
            this.id = id;
        }

        @Override
        public boolean enqueued() {
            return queue;
        }

        @Override
        public void enqueue() {
            queue = true;
        }

        @Override
        public void deque() {
            queue = false;
        }
    }

    @Test(groups = "1s")
    public void test1() throws Exception {
        PriorityQueues<Prio> p = new PriorityQueues<Prio>(new int[]{1, 2, 2, 0,0,0,0,0,0});
        p.add(new Prio(1), 0);
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 1);
        p.add(new Prio(2), 1);
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 2);
        p.add(new Prio(3), 1);
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 3);
        p.add(new Prio(4), 2);
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 4);
        p.add(new Prio(5), 2);
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 5);

        Prio pp = p.pop();
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 4);
        Assert.assertEquals(pp.id, 4);
        pp = p.pop();
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 3);
        Assert.assertEquals(pp.id, 5);
        pp = p.pop();
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 2);
        Assert.assertEquals(pp.id, 2);
        pp = p.pop();
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 1);
        Assert.assertEquals(pp.id, 3);

        p.add(new Prio(6), 2);
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 2);

        pp = p.pop();
        Assert.assertFalse(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 1);
        Assert.assertEquals(pp.id, 6);

        pp = p.pop();
        Assert.assertTrue(p.isEmpty(), "empty");
        Assert.assertEquals(p.size(), 0);
        Assert.assertEquals(pp.id, 1);

    }

}
