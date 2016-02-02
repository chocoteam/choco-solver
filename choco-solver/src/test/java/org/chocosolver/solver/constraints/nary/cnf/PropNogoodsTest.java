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
package org.chocosolver.solver.constraints.nary.cnf;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.explanations.RuleStore;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.util.ESat;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Test class for PropNogoods
 * Created by cprudhom on 25/11/2015.
 * Project: choco.
 */
public class PropNogoodsTest {

    IntVar[] vars;
    PropNogoods PNG;
    int[] lits;

    @BeforeMethod(alwaysRun = true)
    public void setUp() throws Exception {
        Solver solver = new Solver("nogoods");
        vars = VF.enumeratedArray("X", 4, -1, 1, solver);
        PNG = solver.getNogoodStore().getPropNogoods();
        lits = new int[6];
        lits[0] = PNG.Literal(vars[0], 0, true);
        lits[1] = PNG.Literal(vars[0], 0, false);
        lits[2] = PNG.Literal(vars[1], 0, true);
        lits[3] = PNG.Literal(vars[1], 0, false);
        lits[4] = PNG.Literal(vars[2], 0, true);
        lits[5] = PNG.Literal(vars[2], 0, false);
        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[1]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[3]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[4]));
        list.add(lits[5]);
        PNG.addNogood(list);
        PNG.propagate(2);
    }

    @AfterMethod
    public void tearDown() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testPropagate() throws Exception {
        try {
            PNG.propagate(2);
        }catch (ContradictionException c){
            Assert.fail();
        }
        Assert.assertEquals(vars[0].getDomainSize(), 3);
        Assert.assertEquals(vars[1].getDomainSize(), 3);
        Assert.assertEquals(vars[2].getDomainSize(), 3);

        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[2]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[4]);
        PNG.addNogood(list);
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.propagate(2);
        }catch (ContradictionException c){
            Assert.fail();
        }
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
        Assert.assertTrue(vars[1].isInstantiatedTo(0));
        Assert.assertTrue(vars[2].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testPropagate1() throws Exception {
        PNG.propagate(2);
        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[2]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[4]);
        PNG.addNogood(list);
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.propagate(0, 15);
        }catch (ContradictionException c){
            Assert.fail();
        }
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
        Assert.assertTrue(vars[1].isInstantiatedTo(0));
        Assert.assertTrue(vars[2].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testIsEntailed1() throws Exception {
        vars[0].instantiateTo(0, Cause.Null);
        vars[1].instantiateTo(1, Cause.Null);
        vars[2].instantiateTo(-1, Cause.Null);
        Assert.assertEquals(PNG.isEntailed(), ESat.TRUE);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIsEntailed2() throws Exception {
        Assert.assertEquals(PNG.isEntailed(), ESat.UNDEFINED);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLiteral1() throws Exception {
        Assert.assertTrue(lits[0] == 1);
        Assert.assertTrue(lits[1] == 3);
        Assert.assertTrue(lits[2] == 5);
        Assert.assertTrue(lits[3] == 7);
        Assert.assertTrue(lits[4] == 9);
        Assert.assertTrue(lits[5] == 11);
    }

    @Test(groups="1s", timeOut=60000)
    public void testLiteral2() throws Exception {
        BoolVar[] b = VF.boolArray("B", 100, vars[0].getSolver());
        for(int i = 0 ; i < 100; i++){
            PNG.Literal(b[i], 0, true);
            PNG.Literal(b[i], 0, false);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound1(){
        try {
            vars[0].instantiateTo(0, Cause.Null);
            PNG.VariableBound(0, true);
            PNG.VariableBound(1, false);


            vars[1].instantiateTo(1, Cause.Null);
            PNG.VariableBound(3, true);
            PNG.VariableBound(2, false);

            vars[2].instantiateTo(-1, Cause.Null);
            PNG.VariableBound(5, false);
            PNG.VariableBound(4, false);

        }catch (ContradictionException cew){
            Assert.fail();
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound2() throws Exception{
        vars[0].instantiateTo(0, Cause.Null);
        try {
            PNG.VariableBound(1, false);
            Assert.fail();
        }catch (ContradictionException ce){

        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound3() throws Exception{
        vars[0].instantiateTo(1, Cause.Null);
        try {
            PNG.VariableBound(0, true);
            Assert.fail();
        }catch (ContradictionException cew){
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testVariableBound4(){
        try {
            vars[0].instantiateTo(1, Cause.Null);
            PNG.VariableBound(0, true);
            Assert.fail();
        }catch (ContradictionException cew){
        }
    }


    @Test(groups="1s", timeOut=60000)
    public void testApplyEarlyDeductions() throws Exception {

    }

    @Test(groups="1s", timeOut=60000)
    public void testWhy() throws Exception {
        ExplanationEngine ee = new ExplanationEngine(vars[0].getSolver(), true, false);

        PNG.propagate(2);
        TIntList list = new TIntArrayList();
        list.add(SatSolver.negated(lits[0]));
        list.add(lits[2]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(lits[4]);
        PNG.addNogood(list);
        list.clear();
        list.add(SatSolver.negated(lits[2]));
        list.add(SatSolver.negated(lits[4]));
        PNG.addNogood(list);
        vars[0].instantiateTo(0, new ICause() {
            @Override
            public boolean why(RuleStore ruleStore, IntVar var, IEventType evt, int value) {
                return false;
            }
        });
        try {
            PNG.propagate(0, 15);
            Assert.fail();
        }catch (ContradictionException c){
            ee.explain(c);
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce1() throws Exception {
        PNG.doReduce(1);
        Assert.assertTrue(vars[0].isInstantiatedTo(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce2() throws Exception {
        PNG.doReduce(0);
        Assert.assertFalse(vars[0].contains(0));
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce3() throws Exception {
        PNG.doReduce(3);
        Assert.assertEquals(vars[0].getUB(),0);
    }

    @Test(groups="1s", timeOut=60000)
    public void testDoReduce4() throws Exception {
        PNG.doReduce(2);
        Assert.assertEquals(vars[0].getLB(),1);
    }

    @Test(groups="1s", timeOut=60000)
    public void testIvalue(){
        int[] values = {0, 1, -1, 10, -10, 181, -181, 210, -210};
        for(int value: values) {
            long eqvalue = value;
            long ltvalue = PropNogoods.leq(value);
            Assert.assertEquals(PropNogoods.ivalue(eqvalue), value, "ivalue eq: " + value + ", " + eqvalue + "");
            Assert.assertEquals(PropNogoods.ivalue(ltvalue), value, "ivalue leq: " + value + ", " + eqvalue + "");
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testDeclareDomainNogood(){
        IntVar var = VF.enumerated("X4", -1, 1, vars[0].getSolver());
        PNG.declareDomainNogood(var);
        try{
            PNG.doReduce(13);
            Assert.assertTrue(var.isInstantiatedTo(-1));
        }catch (ContradictionException c){
            Assert.fail();
        }
    }
}