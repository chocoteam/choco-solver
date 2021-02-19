/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.decision;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.variables.Variable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.*;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 14/03/2016.
 */
public class DecisionMakerTest {

    Model model;
    DecisionMaker dm;

    @BeforeMethod(alwaysRun = true)
    public void setUp(){
        model = new Model();
        dm = new DecisionMaker();
    }


    @Test(groups = "1s", timeOut=60000)
    public void testMakeIntDecision() throws Exception {
        IntDecision d = dm.makeIntDecision(model.boolVar(), DecisionOperatorFactory.makeIntEq(), 0);
        d.free();
        d = dm.makeIntDecision(model.boolVar(), DecisionOperatorFactory.makeIntEq(), 1);
        d.free();
    }

    @Test(groups = "1s", timeOut=60000)
    public void testMakeRealDecision() throws Exception {
        RealDecision d = dm.makeRealDecision(model.realVar(1d, 3d), 1.5, Double.MIN_VALUE, true);
        d.free();
        d = dm.makeRealDecision(model.realVar(2d, 4d), 2.5, Double.MIN_VALUE * 2, true);
        d.free();
    }

    @Test(groups = "1s", timeOut=60000)
    public void testMakeSetDecision() throws Exception {
        SetDecision d = dm.makeSetDecision(model.setVar(new int[]{2,3,4}), DecisionOperatorFactory.makeSetForce(), 3);
        d.free();
        d = dm.makeSetDecision(model.setVar(new int[]{3,4, 5}), DecisionOperatorFactory.makeSetRemove(), 4);
        d.free();
    }


    public final static Object doSerialize(Serializable obj) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos =  new ObjectOutputStream(bos);
        oos.writeObject(obj);
        ByteArrayInputStream bis = new ByteArrayInputStream(bos.toByteArray()); 
        bos.close();
        oos.close();
        ObjectInputStream ois =  new ObjectInputStream(bis);
        return ois.readObject();

    }
    private <V extends Variable> void assertEqualDecisions(Decision<V> d1, Decision<V> d) {
        Assert.assertEquals(d1.getArity(), d.getArity());
        Assert.assertEquals(d1.getPosition(), d.getPosition());
        Assert.assertEquals(d1.getDecisionValue(), d.getDecisionValue());
        Assert.assertEquals(d1.branch, d.branch);
        Assert.assertNull(d1.getDecisionVariable());
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSerializeIntDecision() throws Exception {
        IntDecision d = dm.makeIntDecision(model.boolVar(), DecisionOperatorFactory.makeIntEq(), 0);
        IntDecision d1 = (IntDecision) doSerialize(d);
        assertEqualDecisions(d1, d);
        Assert.assertEquals(d1.getDecOp(), d.getDecOp());              
    }

    @Test(groups = "1s", timeOut=60000)
    public void testSerializeSetDecision() throws Exception {
        SetDecision d = dm.makeSetDecision(model.setVar(0, 1,2,3), DecisionOperatorFactory.makeSetForce(), 0);
        SetDecision d1 = (SetDecision) doSerialize(d);
        assertEqualDecisions(d1, d);
        // Assert.assertEquals(d1.getDecOp(), d.getDecOp());  // method not implemented  
    }
}