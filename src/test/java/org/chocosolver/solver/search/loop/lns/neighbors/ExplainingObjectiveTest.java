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
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.ResolutionPolicy;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.explanations.ExplanationEngine;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
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

        model.setObjective(ResolutionPolicy.MAXIMIZE, o);
        model.getSolver().setExplainer(new ExplanationEngine(model, false, false));
        ExplainingObjective eo = new ExplainingObjective(model, 10, 0);
        eo.init();
        DecisionPath dp = model.getSolver().getDecisionPath();
        try {
            model.getSolver().propagate();
            dp.apply();
            model.getEnvironment().worldPush();
            dp.pushDecision(dp.makeIntDecision(bs[0], DecisionOperator.int_eq, 1));
            dp.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            dp.pushDecision(dp.makeIntDecision(bs[1], DecisionOperator.int_eq, 1));
            dp.apply();
            model.getSolver().propagate();
            model.getEnvironment().worldPush();
            dp.pushDecision(dp.makeIntDecision(bs[2], DecisionOperator.int_eq, 1));
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