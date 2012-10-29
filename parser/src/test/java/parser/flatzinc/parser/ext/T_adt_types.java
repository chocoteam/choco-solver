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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincFullExtParser;
import parser.flatzinc.ast.ext.Pair;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.ternary.Times;
import solver.propagation.IPropagationEngine;
import solver.propagation.PropagationEngine;
import solver.propagation.generator.PropagationStrategy;
import solver.propagation.generator.Queue;
import solver.propagation.generator.Sort;
import solver.propagation.generator.SortDyn;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/10/12
 */
public class T_adt_types extends GrammarExtTest {

    Solver mSolver;
    THashMap<String, Object> map;
    THashMap<String, ArrayList> groups;
    IntVar[] vars;
    Propagator prop;
    IPropagationEngine pe;


    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        map = new THashMap<String, Object>();
        groups = new THashMap<String, ArrayList>();
        vars = VariableFactory.boundedArray("v", 3, 1, 10, mSolver);
        Constraint cstr = new Times(vars[0], vars[1], vars[2], mSolver);
        mSolver.post(cstr);
        prop = cstr.propagators[0];
        pe = new PropagationEngine(mSolver.getEnvironment());
        pe.prepareWM(mSolver);
    }

    public ArrayList<PropagationStrategy> adt_types(FlatzincFullExtParser parser, ArrayList in) throws RecognitionException {
//        FlatzincFullExtParser.adt_types_return r = parser.adt_types();
//        CommonTree t = (CommonTree) r.getTree();
//        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
//        FlatzincFullExtWalker walker = new FlatzincFullExtWalker(nodes);
//        walker.mSolver = mSolver;
//        walker.map = map;
//        walker.groups = groups;
//        return walker.adt_types(pe, in);
        return null;
    }

    @Test
    public void test1() throws RecognitionException, IOException {
        ArrayList before = new ArrayList() {{
            add(new Pair(vars[0], prop, 0));
            add(new Pair(vars[1], prop, 1));
            add(new Pair(vars[2], prop, 2));
        }};
        FlatzincFullExtParser fp = parser("queue(one)");
        ArrayList<PropagationStrategy> out = adt_types(fp, before);
        Assert.assertEquals(1, out.size());
        Assert.assertTrue(out.get(0) instanceof Queue);
    }

    @Test
    public void test2() throws RecognitionException, IOException {
        ArrayList before = new ArrayList() {{
            add(new Pair(vars[0], prop, 0));
            add(new Pair(vars[1], prop, 1));
            add(new Pair(vars[2], prop, 2));
        }};
        FlatzincFullExtParser fp = parser("heap(wone)");
        ArrayList<PropagationStrategy> out = adt_types(fp, before);
        Assert.assertEquals(1, out.size());
        Assert.assertTrue(out.get(0) instanceof SortDyn);
    }

    @Test
    public void test3() throws RecognitionException, IOException {
        ArrayList before = new ArrayList() {{
            add(new Pair(vars[0], prop, 0));
            add(new Pair(vars[1], prop, 1));
            add(new Pair(vars[2], prop, 2));
        }};
        FlatzincFullExtParser fp = parser("list(wfor)");
        ArrayList<PropagationStrategy> out = adt_types(fp, before);
        Assert.assertEquals(1, out.size());
        Assert.assertTrue(out.get(0) instanceof Sort);
    }

    @Test
    public void test4() throws RecognitionException, IOException {
        ArrayList before = new ArrayList() {{
            add(new ArrayList<Pair>() {{
                add(new Pair(vars[0], prop, 0));
                add(new Pair(vars[1], prop, 1));
            }});
            add(new ArrayList<Pair>() {{
                add(new Pair(vars[2], prop, 2));
            }});
        }};
        FlatzincFullExtParser fp = parser("many list(wfor)");
        ArrayList<PropagationStrategy> out = adt_types(fp, before);
        Assert.assertEquals(2, out.size());
        Assert.assertTrue(out.get(0) instanceof Sort);
        Assert.assertTrue(out.get(1) instanceof Sort);
    }

    @Test
    public void test5() throws RecognitionException, IOException {
        ArrayList before = new ArrayList() {{
            add(new ArrayList() {{
                add(new ArrayList<Pair>() {{
                    add(new Pair(vars[0], prop, 0));
                    add(new Pair(vars[1], prop, 1));
                }});
                add(new ArrayList<Pair>() {{
                    add(new Pair(vars[2], prop, 2));
                }});
            }});
        }};
        FlatzincFullExtParser fp = parser("queue(wone, many list(wfor))");
        ArrayList<PropagationStrategy> out = adt_types(fp, before);
        Assert.assertEquals(1, out.size());
        Assert.assertTrue(out.get(0) instanceof Queue);
    }


}
