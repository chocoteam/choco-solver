/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package parser.flatzinc.parser.ext;

import gnu.trove.map.hash.THashMap;
import junit.framework.Assert;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincFullExtParser;
import parser.flatzinc.FlatzincFullExtWalker;
import parser.flatzinc.ast.ext.Pair;
import solver.Solver;
import solver.constraints.Arithmetic;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 22/10/12
 */
public class T_grp_instr extends GrammarExtTest {

    Solver mSolver;
    THashMap<String, Object> map;

    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        map = new THashMap<String, Object>();
    }

    public ArrayList grp_instr(FlatzincFullExtParser parser, ArrayList before) throws RecognitionException {
        FlatzincFullExtParser.grp_instr_return r = parser.grp_instr();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
        walker.mSolver = mSolver;
        walker.map = map;
        return walker.grp_instr(before);
    }

    @Test
    public void test1() throws IOException, RecognitionException {
        IntVar x = VariableFactory.bool("x", mSolver);
        IntVar y = VariableFactory.bool("y", mSolver);
        Constraint c = new Arithmetic(x, ">", 0, mSolver);
        mSolver.post(c);
        mSolver.post(new Arithmetic(x, ">", y, mSolver));
        ArrayList<Pair> before = Pair.populate(mSolver);
        FlatzincFullExtParser fp = parser("filter in(c.arity = 1)");
        ArrayList after = grp_instr(fp, before);
        Assert.assertEquals(1, after.size());
        Pair p = (Pair) after.get(0);
        Assert.assertEquals(x, p.var);
        Assert.assertEquals(c, p.prop.getConstraint());

    }

    @Test
    public void test2() throws IOException, RecognitionException {
        IntVar x = VariableFactory.bool("x", mSolver);
        IntVar y = VariableFactory.bool("y", mSolver);
        mSolver.post(new Arithmetic(x, ">", 0, mSolver));
        mSolver.post(new Arithmetic(x, ">", y, mSolver));
        ArrayList<Pair> before = Pair.populate(mSolver);
        FlatzincFullExtParser fp = parser("groupBy v.idx");
        ArrayList after = grp_instr(fp, before);
        Assert.assertEquals(2, after.size());
        ArrayList l1 = (ArrayList) after.get(0);
        ArrayList l2 = (ArrayList) after.get(1);
        Assert.assertEquals(2, l1.size());
        Pair p = (Pair) l1.get(0);
        Assert.assertEquals(x, p.var);
        p = (Pair) l1.get(1);
        Assert.assertEquals(x, p.var);
        p = (Pair) l2.get(0);
        Assert.assertEquals(y, p.var);
    }

    @Test
    public void test3() throws IOException, RecognitionException {
        IntVar x = VariableFactory.bool("x", mSolver);
        IntVar y = VariableFactory.bool("y", mSolver);
        mSolver.post(new Arithmetic(x, ">", 0, mSolver));
        mSolver.post(new Arithmetic(x, ">", y, mSolver));
        ArrayList<Pair> before = Pair.populate(mSolver);
        FlatzincFullExtParser fp = parser("orderBy inc v.cardinality");
        ArrayList after = grp_instr(fp, before);
        Assert.assertEquals(3, after.size());
        Pair p = (Pair) after.get(0);
        Assert.assertEquals(y, p.var);
        p = (Pair) after.get(1);
        Assert.assertEquals(x, p.var);
        p = (Pair) after.get(2);
        Assert.assertEquals(x, p.var);
    }

    @Test
    public void testOrdering() throws IOException, RecognitionException {
        IntVar[] vars = VariableFactory.boundedArray("v", 5, 1, 5, mSolver);
        Constraint[] cstrs = new Constraint[4];
        for (int i = 0; i < 4; i++) {
            cstrs[i] = ConstraintFactory.lt(vars[i], vars[i + 1], mSolver);
            map.put("c_" + i, cstrs[i]);
            map.put(vars[i].getName(), vars[i]);
        }
        map.put(vars[4].getName(), vars[4]);
        mSolver.post(cstrs);
        ArrayList<Pair> before = Pair.populate(mSolver);
        FlatzincFullExtParser fp = parser("" +
                "filter or(in(v_0,c_0),in(v_1,c_1),in(v_2,c_2),in(v_3,c_3)) orderBy inc c.idx");
        ArrayList elts = grp_instr(fp, before);
        Assert.assertEquals(4, elts.size());
    }
}
