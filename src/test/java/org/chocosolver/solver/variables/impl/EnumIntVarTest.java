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
package org.chocosolver.solver.variables.impl;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.ranges.IntIterableBitSet;
import org.chocosolver.solver.variables.ranges.IntIterableRangeSet;
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * @author Alexandre LEBRUN
 */
public class EnumIntVarTest extends IntVarTest {


    @BeforeMethod(alwaysRun = true)
    @Override
    public void setup() {
        Model model = new Model();
        this.var = model.intVar(1, 4, false);
    }


    /******************************************************
     * Specific tests related to the enumerated domain
     *****************************************************/

    //------------------------------------
    //------- Remove interval ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveIntervalInner() throws ContradictionException {
        var.removeInterval(2, 3, Cause.Null);
        enumDomainIn(1, 4);
        enumDomainNotIn(2, 3);
    }

    //------------------------------------
    //-------   Remove value  ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValueInner() throws ContradictionException {
        var.removeValue(2, Cause.Null);
        enumDomainIn(1, 3, 4);
        enumDomainNotIn(2);
    }

    //------------------------------------
    //-------   Remove values ------------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesInner() throws ContradictionException {
        IntIterableRangeSet set = new IntIterableRangeSet(2, 3);
        var.removeValues(set, Cause.Null);
        enumDomainIn(1, 4);
        enumDomainNotIn(2, 3);
    }

    //------------------------------------
    //-----  Remove all values but  ------
    //------------------------------------

    @Test(groups = "1s", timeOut=60000)
    public void testRemoveValuesButInner() throws ContradictionException {
        IntIterableSet set = new IntIterableBitSet();
        set.add(1);
        set.add(4);
        var.removeAllValuesBut(set, Cause.Null);
        enumDomainIn(1, 4);
        enumDomainNotIn(2, 3);
    }

    //------------------------------------
    //----------- Utilities  -------------
    //------------------------------------

    private void enumDomainIn(int... values) {
        for (int value : values) {
            assertTrue(var.contains(value));
        }
    }

    private void enumDomainNotIn(int... values) {
        for (int value : values) {
            assertFalse(var.contains(value));
        }
    }

}
