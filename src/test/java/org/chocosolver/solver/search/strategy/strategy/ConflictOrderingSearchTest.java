package org.chocosolver.solver.search.strategy.strategy;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/2016
 */
public class ConflictOrderingSearchTest {

    Model model;
    IntVar[] mvars;
    ConflictOrderingSearch<IntVar> cos;

    @BeforeMethod(alwaysRun = true)
    public void before() {
        model = new Model();
        mvars = model.intVarArray(10, 0, 5);
        cos = new ConflictOrderingSearch(model, Search.inputOrderLBSearch(mvars));
    }


    @Test
    public void testStampIt1() throws Exception {
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[1]);
        Assert.assertEquals(cos.vars.size(), 2);
        Assert.assertEquals(cos.vars.get(1), mvars[1]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), -1);
        Assert.assertEquals(cos.pcft, 1);
        cos.stampIt(mvars[2]);
        Assert.assertEquals(cos.vars.size(), 3);
        Assert.assertEquals(cos.vars.get(2), mvars[2]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), -1);
        Assert.assertEquals(cos.pcft, 2);
        cos.stampIt(mvars[3]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.vars.get(3), mvars[3]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), 1);
        Assert.assertEquals(cos.prev.get(1), 0);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 3);
        Assert.assertEquals(cos.prev.get(3), 2);
        Assert.assertEquals(cos.next.get(3), -1);
        Assert.assertEquals(cos.pcft, 3);
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.prev.get(1), -1);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 3);
        Assert.assertEquals(cos.prev.get(3), 2);
        Assert.assertEquals(cos.next.get(3), 0);
        Assert.assertEquals(cos.prev.get(0), 3);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[3]);
        Assert.assertEquals(cos.vars.size(), 4);
        Assert.assertEquals(cos.prev.get(1), -1);
        Assert.assertEquals(cos.next.get(1), 2);
        Assert.assertEquals(cos.prev.get(2), 1);
        Assert.assertEquals(cos.next.get(2), 0);
        Assert.assertEquals(cos.prev.get(0), 2);
        Assert.assertEquals(cos.next.get(0), 3);
        Assert.assertEquals(cos.prev.get(3), 0);
        Assert.assertEquals(cos.next.get(3), -1);
        Assert.assertEquals(cos.pcft, 3);
    }

    @Test
    public void testStampIt2() throws Exception {
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
        cos.stampIt(mvars[0]);
        Assert.assertEquals(cos.vars.size(), 1);
        Assert.assertEquals(cos.vars.get(0), mvars[0]);
        Assert.assertEquals(cos.prev.get(0), -1);
        Assert.assertEquals(cos.next.get(0), -1);
        Assert.assertEquals(cos.pcft, 0);
    }

    @Test
    public void testfirstNotInst() throws ContradictionException {
        cos.stampIt(mvars[0]);
        cos.stampIt(mvars[1]);
        cos.stampIt(mvars[2]);
        cos.stampIt(mvars[3]);
        cos.stampIt(mvars[4]);
        cos.stampIt(mvars[5]);
        Assert.assertEquals(cos.firstNotInst(), mvars[5]);
        mvars[5].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[4]);
        mvars[4].instantiateTo(0, Cause.Null);
        mvars[3].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[2]);
        mvars[2].instantiateTo(0, Cause.Null);
        Assert.assertEquals(cos.firstNotInst(), mvars[1]);
        mvars[1].instantiateTo(0, Cause.Null);
        mvars[0].instantiateTo(0, Cause.Null);
        Assert.assertNull(cos.firstNotInst());
    }

}