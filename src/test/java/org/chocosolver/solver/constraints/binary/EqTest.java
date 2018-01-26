/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.chocosolver.util.ESat.TRUE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 12/06/13
 */
public class EqTest {

    @DataProvider(name = "params")
    public Object[][] data1D(){
        // indicates whether to use explanations or not
        List<Object[]> elt = new ArrayList<>();
        elt.add(new Object[]{true});
        elt.add(new Object[]{false});
        return elt.toArray(new Object[elt.size()][1]);
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void test1(boolean exp) {
        Model s = new Model();
        IntVar two1 = s.intVar(2);
        IntVar two2 = s.intVar(2);
        s.arithm(two1, "=", two2).post();
        if(exp){
            s.getSolver().setCBJLearning(false,false);
        }
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }


    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void test2(boolean exp) {
        Model s = new Model();
        IntVar three = s.intVar(3);
        IntVar two = s.intVar(2);
        s.arithm(three, "-", two, "=", 1).post();
        if(exp){
            s.getSolver().setCBJLearning(false,false);
        }
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }

    @Test(groups="1s", timeOut=60000, dataProvider = "params")
    public void test3(boolean exp) {
        Model s = new Model();
        IntVar three = s.intVar(3);
        IntVar two = s.intVar(2);
        s.arithm(three, "=", two, "+", 1).post();
        if(exp){
            s.getSolver().setCBJLearning(false,false);
        }
        assertTrue(s.getSolver().solve());
        assertEquals(TRUE, s.getSolver().isSatisfied());
    }
}
