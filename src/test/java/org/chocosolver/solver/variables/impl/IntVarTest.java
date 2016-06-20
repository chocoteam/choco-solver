/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableRangeSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author Alexandre LEBRUN
 */
public abstract class IntVarTest {

    protected IntVar var;


    public abstract void setup();

    //------------------------------------
    //------- Remove interval ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalOK() throws ContradictionException {
        assertTrue(var.removeInterval(3, 4, Cause.Null));
        domainIn(1, 2);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalWrongDomain() throws ContradictionException{
        assertFalse(var.removeInterval(5, 6, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveIntervalEmptyDomain() throws ContradictionException {
        var.removeInterval(1, 4, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveIntervalCoverDomain() throws ContradictionException {
        var.removeInterval(0, 5, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalCoverBound() throws ContradictionException {
        assertTrue(var.removeInterval(3, 5, Cause.Null));
        domainIn(1, 2);
    }


    //------------------------------------
    //-------   Remove value  ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValueOK() throws ContradictionException {
        assertTrue(var.removeValue(4, Cause.Null));
        domainIn(1, 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValueWrongDomain() throws ContradictionException {
        assertFalse(var.removeValue(7, Cause.Null));
        assertFalse(var.removeValue(-1, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveValueEmptyDomain() throws ContradictionException {
        assertTrue(var.removeValue(4, Cause.Null));
        assertTrue(var.removeValue(1, Cause.Null));
        assertTrue(var.removeValue(2, Cause.Null));
        var.removeValue(3, Cause.Null);
    }


    //------------------------------------
    //-------   Remove values ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesOK() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(1, 2);
        assertTrue(var.removeValues(set, Cause.Null));
        domainIn(3, 4);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesWrongDomain() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(5, 6);
        assertFalse(var.removeValues(set, Cause.Null));
        set = new IntIterableRangeSet(-1, -1);
        assertFalse(var.removeValues(set, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void testRemoveValuesEmptyDomain() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(1, 4);
        var.removeValues(set, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesTwoSides() throws ContradictionException {
        IntIterableSet set = new IntIterableBitSet();
        set.add(1);
        set.add(4);
        var.removeValues(set, Cause.Null);
        domainIn(2, 3);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesCoverBound() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(3, 5);
        assertTrue(var.removeValues(set, Cause.Null));
        domainIn(1, 2);
        set = new IntIterableRangeSet(0, 1);
        assertTrue(var.removeValues(set, Cause.Null));
        domainIn(2, 2);
    }


    //------------------------------------
    //-----  Remove all values but  ------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesButOK() throws ContradictionException {
        IntIterableRangeSet set = new IntIterableRangeSet(3, 4);
        assertTrue(var.removeAllValuesBut(set, Cause.Null));
        domainIn(3, 4);
    }

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveAllValuesButCoverDomain() throws ContradictionException {
        IntIterableRangeSet set = new IntIterableRangeSet(1, 4);
        assertFalse(var.removeAllValuesBut(set, Cause.Null));
        set = new IntIterableRangeSet(0, 7);
        assertFalse(var.removeAllValuesBut(set, Cause.Null));
        domainIn(1, 4);
    }

    @Test(groups = "1s", timeOut=60000, expectedExceptions = ContradictionException.class)
    public void removeAllValuesButWrongDomain() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(8, 9);
        var.removeAllValuesBut(set, Cause.Null);
    }

    @Test(groups = "1s", timeOut=60000)
    public void removeAllValuesButCoverBound() throws ContradictionException {
        IntIterableSet set = new IntIterableRangeSet(3, 5);
        assertTrue(var.removeAllValuesBut(set, Cause.Null));
        domainIn(3, 4);
    }


    //------------------------------------
    //----------- Utilities  -------------
    //------------------------------------

    protected void domainIn(int lb, int ub) {
        assertEquals(var.getLB(), lb);
        assertEquals(var.getUB(), ub);
        assertEquals(var.getDomainSize(), ub - lb + 1);
    }


}
