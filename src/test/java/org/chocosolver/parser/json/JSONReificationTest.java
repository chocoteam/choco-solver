/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.json;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.testng.annotations.Test;

/**
 *
 * <p>
 * Project: choco-json.
 * @author Charles Prud'homme
 * @since 27/09/2017.
 */
public class JSONReificationTest extends JSONConstraintTest{
    
    @Test(groups="1s", timeOut=60000)
    public void testXeqCreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXeqC(x, 2, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXneCreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXneC(x, 2, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXgtCreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXgtC(x, 2, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXltCreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXltC(x, 2, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXeqYreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        IntVar y = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXeqY(x, y, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXneYreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        IntVar y = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXneY(x, y, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXltYreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        IntVar y = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXltY(x, y, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXleYreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        IntVar y = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXleY(x, y, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXltYCreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        IntVar y = model.intVar(0,3);
        BoolVar b = model.boolVar();
        model.reifyXltYC(x, y, 1, b);
        eval(model, false);
    }

    @Test(groups="1s", timeOut=60000)
    public void testXinSreif(){
        Model model = new Model();
        IntVar x = model.intVar(0,3);
        IntVar y = model.intVar(0,3);
        BoolVar b = model.boolVar();
        IntIterableRangeSet set = new IntIterableRangeSet();
        set.addBetween(2,4);
        model.reifyXinS(x, set, b);
        eval(model, false);
    }
}
