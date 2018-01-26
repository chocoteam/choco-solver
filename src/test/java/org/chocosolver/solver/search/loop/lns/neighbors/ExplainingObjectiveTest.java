/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperatorFactory;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 11/03/2016.
 */
public class ExplainingObjectiveTest {

    @Test(groups="1s", timeOut=600000)
    public void testRemovedValues1(){
        Model model = new Model();
        IntVar o = model.intVar("O", 0, 20, true);
        BoolVar[] bs = model.boolVarArray(3);
        model.arithm(o, "<", 15).reifyWith(bs[0]);
        model.arithm(o, "<", 10).reifyWith(bs[1]);
        model.arithm(o, "=", 5).reifyWith(bs[2]);

        model.setObjective(Model.MAXIMIZE, o);
        model.getSolver().setExplainer(new ExplanationEngine(model, false, false));
        ExplainingObjective eo = new ExplainingObjective(model, 10, 0);
        eo.init();
        DecisionPath dp = model.getSolver().getDecisionPath();
        try {
            model.getSolver().propagate();
            dp.apply();
            model.getEnvironment().worldPush();
            dp.pushDecision(dp.makeIntDecision(bs[0], DecisionOperatorFactory.makeIntEq(), 1));
            dp.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            dp.pushDecision(dp.makeIntDecision(bs[1], DecisionOperatorFactory.makeIntEq(), 1));
            dp.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            dp.pushDecision(dp.makeIntDecision(bs[2], DecisionOperatorFactory.makeIntEq(), 1));
            dp.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            eo.recordSolution();

            Assert.assertEquals(eo.mDecisionPath.size(), 4);
            Assert.assertNull(eo.mDecisionPath.get(0));
            Assert.assertEquals(eo.related.cardinality(), 3);
            Assert.assertEquals(eo.unrelated.cardinality(), 0);
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

}