package org.chocosolver.parser.json;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.SetVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p> Project: choco-json.
 *
 * @author Charles Prud'homme
 * @since 12/09/2017.
 */
public class JSONVariableTest {

    private void eval(Model model){
        String org = model.toString();
        String str = JSON.toString(model);
        System.out.printf("%s\n", str);
        Model cpy = JSON.fromString(str);
        System.out.printf("%s\n", cpy);
        Assert.assertEquals(cpy.toString(), org);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBoolVar(){
        Model model =new Model("gson");
        BoolVar b0 = model.boolVar();
        BoolVar b1 = model.boolVar(true);
        eval(model);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIntVar(){
        Model model =new Model("gson");
        IntVar v0 = model.intVar(18);
        IntVar v2 = model.intVar(0,40000);
        IntVar v3 = model.intVar(new int[]{1,3,5});
        IntVar v4 = model.intVar(new int[]{1,3,50000});
        IntVar v5 = model.intVar(0,4, true);
        model.setObjective(false, v4);
        eval(model);
    }

    @Test(groups="1s", timeOut=60000)
    public void testBoolView(){
        Model model =new Model("gson");
        BoolVar b0 = model.boolVar();
        BoolVar b1 = b0.not();
        eval(model);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIntView(){
        Model model =new Model("gson");
        IntVar v0 = model.intVar(0,10);
        IntVar v1 = model.intMinusView(v0);
        IntVar v2 = model.intScaleView(v0, 10);
        IntVar v3 = model.intOffsetView(v0, 3);
        eval(model);
    }

    @Test(groups="1s", timeOut=60000)
    public void testRealVar(){
        Model model =new Model("gson");
        IntVar v0 = model.intVar(0,10);
        RealVar v1 = model.realIntView(v0, 0.001);
        RealVar v2 = model.realVar(1.1, 2.6, 0.001);
        RealVar v3 = model.realVar(1.8);
        eval(model);
    }

    @Test(groups="1s", timeOut=60000)
    public void testSetVar(){
        Model model =new Model("gson");
        SetVar s0 = model.setVar(1,3,5);
        SetVar s1 = model.setVar(new int[]{0,4}, new int[]{0,2,4,6});
        SetVar s2 = model.setVar(new int[]{}, new int[]{0});
        eval(model);
    }

    /*@Test(groups="1s", timeOut=60000, expectedExceptions = IllegalArgumentException.class)
    public void testTaskVar(){
        Model model =new Model("gson");
        IntVar s0 = model.intVar(0,10);
        IntVar d0 = model.intVar(0,5);
        Task t0 = model.taskVar(s0, d0);
        IntVar e0 = model.intVar(5,15);
        Task t1 = model.taskVar(s0, d0, e0);
        String org = model.toString();
        String str = JSON.toString(model);
    }*/

}