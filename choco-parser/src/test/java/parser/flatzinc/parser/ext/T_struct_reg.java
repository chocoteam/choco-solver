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
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.IntConstraintFactory;
import solver.propagation.DSLEngine;
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
public class T_struct_reg extends GrammarExtTest {


    Solver mSolver;
    THashMap<String, Object> map;
    THashMap<String, ArrayList> groups;
    DSLEngine pe;

    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        map = new THashMap<String, Object>();
        groups = new THashMap<String, ArrayList>();
        IntVar[] vars = VariableFactory.boundedArray("v", 5, 1, 5, mSolver);
        Constraint[] cstrs = new Constraint[4];
        for (int i = 0; i < 4; i++) {
            cstrs[i] = IntConstraintFactory.arithm(vars[i], "<", vars[i + 1]);
            map.put("c_" + i, cstrs[i]);
            map.put(vars[i].getName(), vars[i]);
        }
        map.put(vars[4].getName(), vars[4]);
        mSolver.post(cstrs);

        pe = new DSLEngine(mSolver);

        ArrayList<Arc> arcs = Arc.populate(mSolver);
        groups.put("G1", arcs);

    }

    public PropagationStrategy struct_reg(FlatzincFullExtParser parser) throws RecognitionException {
        FlatzincFullExtParser.struct_reg_return r = parser.struct_reg();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
        walker.mSolver = mSolver;
        walker.map = map;
        walker.groups = groups;
        return walker.struct_reg(pe);
    }


    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        // Simple constraint oriented engine
        FlatzincFullExtParser fp = parser("G1 as queue(wone) of {each cstr as list(wfor)}");
        PropagationStrategy scheds = struct_reg(fp);
        Assert.assertNotNull(scheds);
        Assert.assertTrue(scheds instanceof Queue);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        // Simple variable oriented engine
        FlatzincFullExtParser fp = parser("G1 as queue(wone) of {each var as list(wfor)}");
        PropagationStrategy scheds = struct_reg(fp);
        Assert.assertNotNull(scheds);
        Assert.assertTrue(scheds instanceof Queue);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        // A Gecode-like propagation engine
        FlatzincFullExtParser fp = parser("G1 as list(wone) of {each prop.prioDyn as queue(one)}");
        PropagationStrategy scheds = struct_reg(fp);
        Assert.assertNotNull(scheds);
        Assert.assertTrue(scheds instanceof Sort);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        // A heap-based variable oriented propagation engine
        FlatzincFullExtParser fp = parser("G1 as heap(wone) of {each var as queue(one) key any.var.cardinality}");
        PropagationStrategy scheds = struct_reg(fp);
        Assert.assertNotNull(scheds);
        Assert.assertTrue(scheds instanceof SortDyn);
    }

    @Test(groups = "1s")
    public void test5() throws IOException, RecognitionException {
        // A heap-based variable oriented propagation engine
        FlatzincFullExtParser fp = parser("G1 as max heap(wone) of {each var as queue(one) key any.var.cardinality}");
        PropagationStrategy scheds = struct_reg(fp);
        Assert.assertNotNull(scheds);
        Assert.assertTrue(scheds instanceof SortDyn);
    }

}
