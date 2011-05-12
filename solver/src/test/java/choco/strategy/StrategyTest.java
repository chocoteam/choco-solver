/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package choco.strategy;

import choco.kernel.memory.IEnvironment;
import junit.framework.Assert;
import org.testng.annotations.Test;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.search.strategy.StrategyFactory;
import solver.search.strategy.decision.Decision;
import solver.search.strategy.enumerations.sorters.SorterFactory;
import solver.search.strategy.enumerations.validators.ValidatorFactory;
import solver.search.strategy.enumerations.values.HeuristicValFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.search.strategy.strategy.StrategiesSequencer;
import solver.search.strategy.strategy.StrategyVarValAssign;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18 oct. 2010
 */
public class StrategyTest {

    @Test(groups = "1s")
    public void AssignmentTest() {
        int n = 100;

        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        IntVar[] variables = new IntVar[n];
        for (int i = 0; i < n; i++) {
            variables[i] = VariableFactory.enumerated("V" + i, i, n + i, s);
        }
        AbstractStrategy asg = StrategyFactory.inputOrderMinVal(variables, env);

        s.set(asg);

        env.worldPush();
        Decision decision = asg.getDecision();
        for (int i = 0; i < n; i++) {
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(variables[i].instantiated());
            Assert.assertEquals(variables[i].getValue(), i);
            Decision tmp = decision;
            decision = asg.getDecision();
            if (decision != null) {
                decision.setPrevious(tmp);
            } else {
                decision = tmp;
            }
            env.worldPush();
        }
        env.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            env.worldPop();
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertFalse(variables[i].instantiated());
            Assert.assertFalse(variables[i].contains(i));
            decision = decision.getPrevious();
        }
    }

    @Test(groups = "1s")
    public void AssignmentTest2() {
        int n = 100;

        Solver s = new Solver();
        IEnvironment env = s.getEnvironment();

        AbstractStrategy[] asgs = new AbstractStrategy[n];

        IntVar[] variables = new IntVar[n];
        for (int i = 0; i < n; i++) {
            variables[i] = VariableFactory.enumerated("V" + i, i, n + i, s);
            HeuristicValFactory.indomainMin(variables[i]);
            IntVar[] tmp = new IntVar[]{variables[i]};
            asgs[i] = StrategyVarValAssign.sta(tmp, SorterFactory.inputOrder(tmp),
                    ValidatorFactory.instanciated, env);
        }

        StrategiesSequencer sts = new StrategiesSequencer(env, asgs);

        s.set(sts);

        env.worldPush();
        Decision decision = sts.getDecision();
        for (int i = 0; i < n; i++) {
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertTrue(variables[i].instantiated());
            Assert.assertEquals(variables[i].getValue(), i);
            Decision tmp = decision;
            decision = sts.getDecision();
            if (decision != null) {
                decision.setPrevious(tmp);
            } else {
                decision = tmp;
            }
            env.worldPush();
        }
        env.worldPop();
        for (int i = n - 1; i >= 0; i--) {
            env.worldPop();
            decision.buildNext();
            try {
                decision.apply();
            } catch (ContradictionException e) {
                e.printStackTrace();
            }
            Assert.assertFalse(variables[i].instantiated());
            Assert.assertFalse(variables[i].contains(i));
            decision = decision.getPrevious();
        }


    }


}
