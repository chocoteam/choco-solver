/*
@author Arthur Godet <arth.godet@gmail.com>
@since 29/03/2019
*/
package org.chocosolver.solver.constraints.binary;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.chocosolver.solver.search.strategy.Search.inputOrderLBSearch;

public class ModXYTest extends AbstractBinaryTest {
    private static int TEST_VALUE = 3; // TODO How to change this dependency to use AbstractBinaryTest

    @Override
    protected int validTuple(int vx, int vy) {
        return vx%TEST_VALUE == vy ? 1 : 0;
    }

    @Override
    protected Constraint make(IntVar[] vars, Model s) {
        return s.mod(vars[0], TEST_VALUE, vars[1]);
    }

    @Test(groups="1s", timeOut=60000)
    public void testJL() {
        Model s = new Model();
        IntVar dividend = s.intVar("dividend", 2, 3, false);
        int divisor = 1;
        IntVar remainder = s.intVar("remainder", 1, 2, false);
        s.mod(dividend, divisor, remainder).getOpposite().post();
        Solver r = s.getSolver();
        r.setSearch(inputOrderLBSearch(dividend, remainder));
        s.getSolver().solve();
    }

    @Test(groups="1s", timeOut=60000)
    public void testJT1(){
        Model model = new Model("model");
        IntVar a = model.intVar("a", 2,6);
        int b = 2;
        IntVar c = model.intVar("c", 0,1);
        model.mod(a, b, c).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2Var(){
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.intVar("z", 0, 9);
        model.mod(x, 2, z).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 10);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2Var2(){
        Model model = new Model("model");
        IntVar x = model.intVar("x", 0,9);
        IntVar z = model.intVar("z", 0, 9);
        model.mod(x, 2, z).post();
        model.mod(z, 2, 1).post();
        Assert.assertEquals(model.getSolver().findAllSolutions().size(), 5);
    }

    @Test(groups="1s", timeOut=60000)
    public void testMod2VarPropag() throws ContradictionException {
        Model model = new Model("model");
        IntVar x = model.intVar("x", new int[]{0, 2, 3, 5});
        IntVar z = model.intVar("z", 1,3);
        model.mod(x, 3, z).post();
        model.getSolver().propagate();
        Assert.assertTrue(z.isInstantiatedTo(2));
        Assert.assertEquals(x.getDomainSize(), 2);
        Assert.assertEquals(x.getLB(), 2);
        Assert.assertEquals(x.getUB(), 5);
    }

}
