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
package org.chocosolver.util.objects.setDataStructures;

import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class ConstantSetTest {

    @Test(groups = "1s", timeOut=60000)
    public void testSize() {
        ISet set = create();
        assertEquals(set.getSize(), 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testIterator() {
        ISet set = SetFactory.makeConstantSet(new int[]{0, 5, 14, 8});
        Set<Integer> reached = new HashSet<>();
        for (Integer i : set) {
            reached.add(i);
        }
        assertEquals(reached.size(), set.getSize());
        for(int i=-5;i<=20;i++){
            assertEquals(reached.contains(i),set.contain(i));
        }
        assertEquals(reached.size(), 4);
        assertTrue(reached.contains(0));
        assertTrue(reached.contains(5));
        assertTrue(reached.contains(14));
        assertTrue(reached.contains(8));
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testAdd() {
        ISet set = create();
        set.add(6);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testRemove() {
        ISet set = create();
        set.remove(5);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = UnsupportedOperationException.class)
    public void testClear() {
        ISet set = create();
        set.clear();
    }

    @Test(groups = "1s", timeOut=60000)
    public void testDuplicates() {
        ISet set = SetFactory.makeConstantSet(new int[]{0, 5, 5, 8});
        assertEquals(set.getSize(), 3);
    }

    private ISet create() {
        return SetFactory.makeConstantSet(new int[]{0, 5, 8});
    }
}
