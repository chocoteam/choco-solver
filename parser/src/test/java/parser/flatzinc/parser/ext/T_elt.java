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
import solver.propagation.ISchedulable;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.Arc;
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
public class T_elt extends GrammarExtTest {

    Solver mSolver;
    THashMap<String, Object> map;
    THashMap<String, ArrayList> groups;
    PropagationEngine pe;

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

        pe = new PropagationEngine(mSolver);

        ArrayList<Arc> pairs = Arc.populate(mSolver);
        groups.put("G1", pairs);
    }

    public ISchedulable[] elt(FlatzincFullExtParser parser) throws RecognitionException {
        FlatzincFullExtParser.elt_return r = parser.elt();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
        walker.mSolver = mSolver;
        walker.map = map;
        walker.groups = groups;
        return walker.elt(pe);
    }

    @Test(groups = "1s")
    public void test1() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("G1");
        ISchedulable[] scheds = elt(fp);
        Assert.assertNotNull(scheds);
    }

    @Test(groups = "1s")
    public void test2() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("G1 key var.name");
        ISchedulable[] scheds = elt(fp);
        Assert.assertNotNull(scheds);
    }

    @Test(groups = "1s")
    public void test3() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("queue(one) of {G1}");
        ISchedulable[] scheds = elt(fp);
        Assert.assertNotNull(scheds);
    }

    @Test(groups = "1s")
    public void test4() throws IOException, RecognitionException {
        FlatzincFullExtParser fp = parser("queue(one) of {G1} key any.var.name");
        ISchedulable[] scheds = elt(fp);
        Assert.assertNotNull(scheds);
    }
}
