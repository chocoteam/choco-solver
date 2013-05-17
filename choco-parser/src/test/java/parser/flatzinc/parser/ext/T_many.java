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
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincFullExtParser;
import parser.flatzinc.FlatzincFullExtWalker;
import parser.flatzinc.ast.Datas;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.propagation.DSLEngine;
import solver.propagation.generator.Arc;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.generator.Queue;
import solver.propagation.generator.Sort;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/11/12
 */
public class T_many extends GrammarExtTest {

    Solver mSolver;
    Datas datas;
    THashMap<String, ArrayList> groups;
    DSLEngine pe;
    ArrayList<Arc> arcs;

    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        datas = new Datas();
        groups = new THashMap<String, ArrayList>();
        IntVar[] vars = VariableFactory.boundedArray("v", 5, 1, 5, mSolver);
        Constraint[] cstrs = new Constraint[4];
        for (int i = 0; i < 4; i++) {
            cstrs[i] = IntConstraintFactory.arithm(vars[i], "<", vars[i + 1]);
            datas.register("c_" + i, cstrs[i]);
            datas.register(vars[i].getName(), vars[i]);
        }
        datas.register(vars[4].getName(), vars[4]);
        mSolver.post(cstrs);

        pe = new DSLEngine(mSolver);
        arcs = Arc.populate(mSolver);
    }

    public FlatzincFullExtWalker.many_return many(FlatzincFullExtParser parser,
                                                  ArrayList<Arc> in) throws RecognitionException {
        FlatzincFullExtParser.many_return r = parser.many();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
        walker.mSolver = mSolver;
        walker.datas = datas;
        walker.groups = groups;
        return walker.many(in);
    }

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("each var as queue(wone)");
        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(0, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(5, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Queue);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("each cstr as queue(wone) of { each var as list(wfor)}");

        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(1, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(4, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Queue);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("each cstr as list(for) of { each var as list(wfor) key any.var.name }");
        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(1, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(4, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Sort);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("each prop.prioDyn as queue(wone) of { each prop as list(wfor)}");

        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(1, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(3, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Queue);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("each prop as queue(wone) key any.prop.priority");

        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(0, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(4, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Queue);
    }

    //each var as list(wone) of {" +
//                                    "       each prop as queue(wone) key any.prop.priority" +
//                                    "   } key any.any.var.cardinality

    @Test(groups = "1s")
    public void test6() throws IOException, RecognitionException {
        FlatzincFullExtParser fp =
                parser("each var as list(wone) key any.var.cardinality");

        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(0, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(5, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Sort);
    }

    @Test(groups = "1s")
    public void test7() throws IOException, RecognitionException {
        FlatzincFullExtParser fp =
                parser("each var as list(wone) of {each prop as queue(one) key any.prop.priority} key any.any.var.cardinality");

        FlatzincFullExtWalker.many_return _many = many(fp, arcs);
        Assert.assertEquals(1, _many.depth);
        ArrayList<PropagationStrategy> scheds = _many.pss;
        Assert.assertNotNull(scheds);
        Assert.assertEquals(5, scheds.size());
        Assert.assertTrue(scheds.get(0) instanceof Sort);
    }
}
