/**
 * Copyright (c) 2014, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the {organization} nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.sat;

import gnu.trove.list.array.TIntArrayList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * <p>
 * Project: choco-sat.
 *
 * @author Charles Prud'homme
 * @since 07/03/2016.
 */
public class SatSolverTest {

    SatSolver sat;
    int a, b, c, d;

    @BeforeMethod
    public void setUp() throws Exception {
        sat = new SatSolver();
        a = sat.newVariable();
        b = sat.newVariable();
        c = sat.newVariable();
        d = sat.newVariable();
    }

    @Test(groups = "1s")
    public void testNewVariable() throws Exception {
        Assert.assertEquals(a, 0);
        Assert.assertEquals(b, 1);
        Assert.assertEquals(c, 2);
        Assert.assertEquals(d, 3);
        Assert.assertEquals(SatSolver.makeLiteral(a, true), 1);
        Assert.assertEquals(SatSolver.makeLiteral(a, false), 0);
        Assert.assertEquals(SatSolver.makeLiteral(b, true), 3);
        Assert.assertEquals(SatSolver.makeLiteral(b, false), 2);
        Assert.assertEquals(SatSolver.makeLiteral(c, true), 5);
        Assert.assertEquals(SatSolver.makeLiteral(c, false), 4);
        Assert.assertEquals(SatSolver.makeLiteral(d, true), 7);
        Assert.assertEquals(SatSolver.makeLiteral(d, false), 6);
    }

    @Test(groups = "1s")
    public void testAddClause() throws Exception {
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(d), SatSolver.Boolean.kUndefined);

        Assert.assertTrue(sat.addClause(SatSolver.makeLiteral(a, true)));
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kFalse);
        Assert.assertTrue(sat.addClause(SatSolver.makeLiteral(b, false)));
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kTrue);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.clauses.size(), 0);
        Assert.assertEquals(sat.implies_.size(), 0);
    }

    @Test(groups = "1s")
    public void testLearnClause() throws Exception {

    }

    @Test(groups = "1s")
    public void testAddEmptyClause() throws Exception {
        Assert.assertFalse(sat.addEmptyClause());
    }

    @Test(groups = "1s")
    public void testAddClause1() throws Exception {
        int ap = SatSolver.makeLiteral(a, true);
        int bp = SatSolver.makeLiteral(b, true);
        Assert.assertTrue(sat.addClause(ap,bp));
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.clauses.size(), 0);
        Assert.assertEquals(sat.implies_.size(), 2);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertNull(sat.implies_.get(ap));
        Assert.assertNull(sat.implies_.get(bp));
        Assert.assertEquals(sat.implies_.get(SatSolver.negated(ap)).size(), 1);
        Assert.assertEquals(sat.implies_.get(SatSolver.negated(ap)).get(0), bp);
        Assert.assertEquals(sat.implies_.get(SatSolver.negated(bp)).size(), 1);
        Assert.assertEquals(sat.implies_.get(SatSolver.negated(bp)).get(0), ap);
    }

    @Test(groups = "1s")
    public void testAddClause2() throws Exception {
        int ap = SatSolver.makeLiteral(a, true);
        int bp = SatSolver.makeLiteral(b, true);
        int cp = SatSolver.makeLiteral(c, true);
        Assert.assertTrue(sat.addClause(ap,bp, cp));
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.implies_.size(), 0);
        Assert.assertEquals(sat.clauses.size(), 1);
    }

    @Test(groups = "1s")
    public void testAddClause3() throws Exception {
        int ap = SatSolver.makeLiteral(a, true);
        int bp = SatSolver.makeLiteral(b, true);
        int cp = SatSolver.makeLiteral(c, true);
        int dp = SatSolver.makeLiteral(d, true);
        Assert.assertTrue(sat.addClause(new TIntArrayList(new int[]{ap,bp, cp, dp})));
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.implies_.size(), 0);
        Assert.assertEquals(sat.clauses.size(), 1);
    }

    @Test(groups = "1s")
    public void testAddClause4() throws Exception {
        int a1 = SatSolver.makeLiteral(a, true);
        Assert.assertTrue(sat.addClause(new TIntArrayList(new int[]{a1,a1,a1, a1})));
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 1);
        Assert.assertEquals(sat.implies_.size(), 0);
        Assert.assertEquals(sat.clauses.size(), 0);
    }

    @Test(groups = "1s")
    public void testAddClause5() throws Exception {
        int a1 = SatSolver.makeLiteral(a, true);
        int a2 = SatSolver.makeLiteral(a, false);
        Assert.assertTrue(sat.addClause(new TIntArrayList(new int[]{a1,a2})));
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.assignment_.get(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.implies_.size(), 0);
        Assert.assertEquals(sat.clauses.size(), 0);
    }

    @Test(groups = "1s")
    public void testAddClause6() throws Exception {
        int ap = SatSolver.makeLiteral(a, true);
        sat.uncheckedEnqueue(ap);
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kFalse);
        int an = SatSolver.makeLiteral(a, false);
        Assert.assertFalse(sat.addClause(an));
        sat.propagate();
        Assert.assertEquals(sat.assignment_.get(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.qhead_, 1);
        Assert.assertFalse(sat.ok_);
    }

    @Test(groups = "1s")
    public void testInitPropagator() throws Exception {
        sat.uncheckedEnqueue(SatSolver.makeLiteral(a, false));
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kTrue);
        Assert.assertEquals(sat.touched_variables_.size(), 1);
        Assert.assertFalse(sat.initPropagator());
        Assert.assertEquals(sat.touched_variables_.size(), 0);
    }

    @Test(groups = "1s")
    public void testCancelUntil() throws Exception {
        int ap = SatSolver.makeLiteral(a, true);
        int an = SatSolver.makeLiteral(a, false);
        int bp = SatSolver.makeLiteral(b, true);
        int cp = SatSolver.makeLiteral(c, true);
        int cn = SatSolver.makeLiteral(c, false);
        int dp = SatSolver.makeLiteral(d, true);
        sat.addClause(an, bp);
        sat.addClause(cn, dp);
        sat.propagateOneLiteral(ap);
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(b), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 1);
        Assert.assertEquals(sat.trailMarker(), 1);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        sat.propagateOneLiteral(cp);
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(b), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.qhead_, 4);
        Assert.assertEquals(sat.trail_.size(), 4);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_.get(2), 5);
        Assert.assertEquals(sat.trail_.get(3), 7);
        Assert.assertEquals(sat.trail_markers_.size(), 2);
        Assert.assertEquals(sat.trailMarker(), 2);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        Assert.assertEquals(sat.trail_markers_.get(1), 2);
        sat.cancelUntil(1);
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(b), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 1);
        Assert.assertEquals(sat.trailMarker(), 1);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        sat.cancelUntil(0);
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(b), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 0);
        Assert.assertEquals(sat.trail_.size(), 0);
        Assert.assertEquals(sat.trail_markers_.size(), 0);
        Assert.assertEquals(sat.trailMarker(), 0);
    }

    @Test(groups = "1s")
    public void testValueVar() throws Exception {
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kUndefined);
        sat.propagateOneLiteral(SatSolver.makeLiteral(c, true));
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kFalse);

        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kUndefined);
        sat.propagateOneLiteral(SatSolver.makeLiteral(d, false));
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kTrue);
    }

    @Test(groups = "1s")
    public void testValueLit() throws Exception {
        int cp = SatSolver.makeLiteral(c, true);
        int cn = SatSolver.makeLiteral(c, false);
        int dp = SatSolver.makeLiteral(d, true);
        int dn = SatSolver.makeLiteral(d, false);

        Assert.assertEquals(sat.valueLit(cp), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueLit(cn), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueLit(dp), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueLit(dn), SatSolver.Boolean.kUndefined);

        sat.propagateOneLiteral(SatSolver.makeLiteral(c, true));
        Assert.assertEquals(sat.valueLit(cp), SatSolver.Boolean.kTrue);
        Assert.assertEquals(sat.valueLit(cn), SatSolver.Boolean.kFalse);

        sat.propagateOneLiteral(SatSolver.makeLiteral(d, false));
        Assert.assertEquals(sat.valueLit(dp), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueLit(dn), SatSolver.Boolean.kTrue);
    }

    @Test(groups = "1s")
    public void testNClauses() throws Exception {

    }

    @Test(groups = "1s")
    public void testNLearnt() throws Exception {

    }

    @Test(groups = "1s")
    public void testPropagateOneLiteral() throws Exception {
        int ap = SatSolver.makeLiteral(a, true);
        int an = SatSolver.makeLiteral(a, false);
        int bp = SatSolver.makeLiteral(b, true);
        int bn = SatSolver.makeLiteral(b, false);
        int cp = SatSolver.makeLiteral(c, true);
        int cn = SatSolver.makeLiteral(c, false);
        int dp = SatSolver.makeLiteral(d, true);
        int dn = SatSolver.makeLiteral(d, false);
        sat.addClause(an, bp);
        sat.addClause(cn, dp);
        sat.addClause(cp, dp);

        Assert.assertTrue(sat.propagateOneLiteral(ap));
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(b), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 1);
        Assert.assertEquals(sat.trailMarker(), 1);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);

        Assert.assertFalse(sat.propagateOneLiteral(an));
        Assert.assertTrue(sat.propagateOneLiteral(bp));
        Assert.assertEquals(sat.valueVar(a), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(b), SatSolver.Boolean.kFalse);
        Assert.assertEquals(sat.valueVar(c), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.valueVar(d), SatSolver.Boolean.kUndefined);
        Assert.assertEquals(sat.qhead_, 2);
        Assert.assertEquals(sat.trail_.size(), 2);
        Assert.assertEquals(sat.trail_.get(0), 1);
        Assert.assertEquals(sat.trail_.get(1), 3);
        Assert.assertEquals(sat.trail_markers_.size(), 2);
        Assert.assertEquals(sat.trailMarker(), 2);
        Assert.assertEquals(sat.trail_markers_.get(0), 0);
        Assert.assertEquals(sat.trail_markers_.get(1), 2);
        Assert.assertFalse(sat.propagateOneLiteral(bn));

        sat.uncheckedEnqueue(dn);
        Assert.assertFalse(sat.propagateOneLiteral(cn));

    }

    @Test(groups = "1s")
    public void testPushTrailMarker() throws Exception {

    }

    @Test(groups = "1s")
    public void testUncheckedEnqueue() throws Exception {

    }

    @Test(groups = "1s")
    public void testDynUncheckedEnqueue() throws Exception {

    }

    @Test(groups = "1s")
    public void testEnqueue() throws Exception {

    }

    @Test(groups = "1s")
    public void testAttachClause() throws Exception {

    }

    @Test(groups = "1s")
    public void testDetachLearnt() throws Exception {

    }

    @Test(groups = "1s")
    public void testPropagate() throws Exception {
        sat.addBoolOrArrayEqualTrue(a, b, c, d);
        sat.addBoolOrArrayEqualTrue(b, c, d);
        sat.addBoolOrArrayEqualTrue(a, c, d);
        Assert.assertTrue(sat.propagate());
        Assert.assertTrue(sat.propagateOneLiteral(SatSolver.makeLiteral(a, false)));
        Assert.assertTrue(sat.propagateOneLiteral(SatSolver.makeLiteral(b, false)));
        Assert.assertTrue(sat.propagateOneLiteral(SatSolver.makeLiteral(c, false)));
        Assert.assertFalse(sat.propagateOneLiteral(SatSolver.makeLiteral(d, false)));
        sat.cancelUntil(2);
        Assert.assertTrue(sat.propagateOneLiteral(SatSolver.makeLiteral(d, true)));
    }

    @Test(groups = "1s")
    public void testMakeLiteral() throws Exception {

    }

    @Test(groups = "1s")
    public void testNegated() throws Exception {

    }

    @Test(groups = "1s")
    public void testSign() throws Exception {

    }

    @Test(groups = "1s")
    public void testVar() throws Exception {

    }

    @Test(groups = "1s")
    public void testMakeBoolean() throws Exception {

    }

    @Test(groups = "1s")
    public void testXor() throws Exception {

    }

    @Test(groups = "1s")
    public void testCopyFrom() throws Exception {

    }

    @Test(groups = "1s")
    public void testNbclauses() throws Exception {

    }

    @Test(groups = "1s")
    public void testNumvars() throws Exception {
        Assert.assertEquals(sat.num_vars_, 4);
    }
}