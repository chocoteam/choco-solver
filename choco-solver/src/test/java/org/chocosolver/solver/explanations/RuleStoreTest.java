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
package org.chocosolver.solver.explanations;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.ISF;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.strategy.IntStrategy;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VF;
import org.chocosolver.solver.variables.events.IntEventType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by cprudhom on 10/12/14.
 * Project: choco.
 */
public class RuleStoreTest {

    Solver solver;
    IntVar E; // enumerated
    IntVar I; // interval
    BoolVar B; // boolean

    public void setUp() {
        solver = new Solver();
        E = VF.enumerated("E", 0, 6, solver);
        I = VF.bounded("I", 0, 6, solver);
        B = VF.bool("B", solver);
    }

    @Test(groups = "1s")
    public void testEnumFullDom() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));
        // add a rule on all event which has occurred on E
        rs.addFullDomainRule(E);

        int rmask = rs.getMask(E);
        Assert.assertEquals(rmask, RuleStore.DM);
        // simulates the test of an instantiation event
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 4));
        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INCLOW, 1, 0, -1));
        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a value removal
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.REMOVE, 5, -1, -1));

    }

    @Test(groups = "1s")
    public void testEnumLow() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addLowerBoundRule(E);

        int rmask = rs.getMask(E);
        Assert.assertEquals(rmask, RuleStore.LB);

        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INCLOW, 1, 0, -1));
        // simulates the test of a upper bound increasing
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.DECUPP, 4, 6, -1));

        // simulates the test of instantiation events
        // LB hidden
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 4));
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 3));
        // no LB at all
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 3, 4));
        // simulates the test of a value removal
        // a value above the current lb
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.REMOVE, 3, -1, -1));
        // a value above the below lb
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.REMOVE, -1, -1, -1));
    }

    @Test(groups = "1s")
    public void testEnumUpp() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addUpperBoundRule(E);

        int rmask = rs.getMask(E);
        Assert.assertEquals(rmask, RuleStore.UB);
        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a lower bound increasing
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.INCLOW, 1, 0, -1));

        // simulates the test of instantiation events
        // UB hidden
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 4));
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 3, 4));
        // no UB at all
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 3));

        // simulates the test of a value removal
        // a value above the below ub
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.REMOVE, -1, -1, -1));
        // a value above the above ub
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.REMOVE, 7, -1, -1));
    }

    @Test(groups = "1s")
    public void testEnumBound() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addLowerBoundRule(E);
        rs.addUpperBoundRule(E);

        int rmask = rs.getMask(E);
        Assert.assertEquals(rmask, RuleStore.BD);

        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INCLOW, 1, 0, -1));

        // simulates the test of instantiation events
        // UB hidden
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 4));
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 3, 4));
        // no UB at all
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 3));

        // simulates the test of a value removal
        // a value above the current lb
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.REMOVE, -1, -1, -1));
        // a value above the below ub
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.REMOVE, 7, -1, -1));
    }

    @Test(groups = "1s")
    public void testEnumRem() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addRemovalRule(E, 8);
        rs.addRemovalRule(E, -2);

        int rmask = rs.getMask(E);
        Assert.assertEquals(rmask, RuleStore.RM);

        // simulates the test of a value removal
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.REMOVE, 8, -1, -1));
        // a value above the below ub
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.REMOVE, 3, -1, -1));

        // simulates the test of a upper bound increasing
        // not including the value 8
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.DECUPP, 4, 6, -1));
        // including the value 8
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.DECUPP, 6, 8, -1));

        // simulates the test of a lower bound increasing
        // not including the value -2
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.INCLOW, 1, 0, -1));
        // including the value -2
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INCLOW, 0, -3, -1));

        // simulates the test of instantiation events
        // including the value 8
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 9));
        // including the value -2
        Assert.assertTrue(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, -2, 4));
        // including no value
        Assert.assertFalse(rs.matchDomain(rmask, E, IntEventType.INSTANTIATE, 3, 1, 3));

    }

    @Test(groups = "1s")
    public void testBoundFullDom() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));
        // add a rule on all event which has occurred on E
        rs.addFullDomainRule(I);

        int rmask = rs.getMask(I);
        Assert.assertEquals(rmask, RuleStore.DM);

        // simulates the test of an instantiation event
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 4));
        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INCLOW, 1, 0, -1));
        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a value removal
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.REMOVE, 5, -1, -1));

    }

    @Test(groups = "1s")
    public void testBoundLow() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addLowerBoundRule(I);
        int rmask = rs.getMask(I);
        Assert.assertEquals(rmask, RuleStore.LB);

        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INCLOW, 1, 0, -1));
        // simulates the test of a upper bound increasing
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.DECUPP, 4, 6, -1));

        // simulates the test of instantiation events
        // LB hidden
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 4));
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 3));
        // no LB at all
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 3, 4));
        // simulates the test of a value removal
        // a value above the current lb
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.REMOVE, 3, -1, -1));
        // a value above the below lb
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.REMOVE, -1, -1, -1));
    }

    @Test(groups = "1s")
    public void testBoundUpp() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addUpperBoundRule(I);
        int rmask = rs.getMask(I);
        Assert.assertEquals(rmask, RuleStore.UB);

        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a lower bound increasing
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.INCLOW, 1, 0, -1));

        // simulates the test of instantiation events
        // UB hidden
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 4));
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 3, 4));
        // no UB at all
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 3));

        // simulates the test of a value removal
        // a value above the below ub
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.REMOVE, -1, -1, -1));
        // a value above the above ub
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.REMOVE, 7, -1, -1));
    }

    @Test(groups = "1s")
    public void testBoundBound() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        rs.addLowerBoundRule(I);
        rs.addUpperBoundRule(I);
        int rmask = rs.getMask(I);
        Assert.assertEquals(rmask, RuleStore.BD);

        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INCLOW, 1, 0, -1));

        // simulates the test of instantiation events
        // UB hidden
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 4));
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 3, 4));
        // no UB at all
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 3));

        // simulates the test of a value removal
        // a value above the current lb
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.REMOVE, -1, -1, -1));
        // a value above the below ub
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.REMOVE, 7, -1, -1));
    }

    @Test(groups = "1s")
    public void testBoundRem() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));
        rs.addRemovalRule(I, 8);
        rs.addRemovalRule(I, -2);

        int rmask = rs.getMask(I);
        Assert.assertEquals(rmask, RuleStore.BD);

        try {
            rs.addRemovalRule(I, 3);
        } catch (SolverException ignored) {
        }
        // simulates the test of an instantiation event
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INSTANTIATE, 3, 1, 4));
        // simulates the test of a lower bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.INCLOW, 1, 0, -1));
        // simulates the test of a upper bound increasing
        Assert.assertTrue(rs.matchDomain(rmask, I, IntEventType.DECUPP, 4, 6, -1));
        // simulates the test of a value removal
        Assert.assertFalse(rs.matchDomain(rmask, I, IntEventType.REMOVE, 5, -1, -1));

    }

    @Test(groups = "1s", expectedExceptions = SolverException.class)
    public void testBoundRem2() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));
        rs.matchDomain(RuleStore.RM, I, IntEventType.REMOVE, 7, -1, -1);
    }

    @Test(groups = "1s")
    public void testBoolFullDom() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));
        // add a rule on all event which has occurred on E
        rs.addFullDomainRule(B);
        // simulates the test of an instantiation event
        int rmask = rs.getMask(B);
        Assert.assertEquals(rmask, RuleStore.DM);
        // there cannot be other events
    }


    @Test(groups = "1s")
    public void testDecRefutation() {
        setUp();
        RuleStore rs = new RuleStore(solver, true, true);
        rs.init(new Explanation(null, false));

        IntStrategy is = ISF.lexico_LB(E, I, B);
        Decision d1 = null, d2 = null, d3 = null;
        try {
            d1 = is.getDecision();
            d1.setWorldIndex(1);
            d1.buildNext();
            d1.apply();

            d2 = is.getDecision();
            d2.setWorldIndex(2);
            d2.buildNext();
            d2.apply();

            d3 = is.getDecision();
            d3.setWorldIndex(3);
            d3.buildNext();
            d3.apply();
        } catch (ContradictionException cex) {
            Assert.fail();
        }

        Explanation r = new Explanation(null, false);
        r.addDecicion(d1);
        r.addDecicion(d2);

        rs.storeDecisionRefutation(d3, r);

        rs.getDecisionRefutation(d3);
        d3.buildNext();
        Explanation rr = rs.getDecisionRefutation(d3);
        Assert.assertNotNull(rr);
        Assert.assertEquals(rr.nbDecisions(), 2);

    }

}
