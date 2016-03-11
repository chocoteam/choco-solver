package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.strategy.OnDemandIntStrategy;
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

    @Test(groups="1s", timeOut=6000000)
    public void testRemovedValues1(){
        Model model = new Model();
        IntVar o = model.intVar("O", 0, 20, true);
        BoolVar[] bs = model.boolVarArray(3);
        model.arithm(o, "<", 15).reifyWith(bs[0]);
        model.arithm(o, "<", 10).reifyWith(bs[1]);
        model.arithm(o, "=", 5).reifyWith(bs[2]);

        model.setObjective(ResolutionPolicy.MAXIMIZE, o);
        model.getSolver().set(new ExplanationEngine(model, false, false));
        ExplainingObjective eo = new ExplainingObjective(model, 10, 0);
        eo.init();
        OnDemandIntStrategy ods = new OnDemandIntStrategy();
        try {
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            IntDecision d1 = ods.makeIntDecision(bs[0], DecisionOperator.int_eq, 1);
            d1.buildNext();
            d1.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            IntDecision d2 = ods.makeIntDecision(bs[1], DecisionOperator.int_eq, 1);
            d2.buildNext();
            d2.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            IntDecision d3 = ods.makeIntDecision(bs[2], DecisionOperator.int_eq, 1);
            d3.buildNext();
            d3.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            eo.recordSolution();

            Assert.assertEquals(eo.path.size(), 0);
            Assert.assertEquals(eo.related.cardinality(), 3);
            Assert.assertEquals(eo.unrelated.cardinality(), 0);
        } catch (ContradictionException e) {
            Assert.fail();
        }
    }

}