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
package parser.flatzinc.parser;

import gnu.trove.map.hash.THashMap;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.CommonTreeNodeStream;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import parser.flatzinc.FlatzincParser;
import parser.flatzinc.FlatzincWalker;
import solver.Solver;
import solver.variables.VariableFactory;

import java.io.IOException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/10/12
 */
public class T_solve_goal extends GrammarTest {

    Solver mSolver;
    THashMap<String, Object> map;

    @BeforeMethod
    public void before() {
        mSolver = new Solver();
        map = new THashMap<String, Object>();
    }

    public void solve_goal(FlatzincParser parser) throws RecognitionException {
        FlatzincParser.solve_goal_return r = parser.solve_goal();
        CommonTree t = (CommonTree) r.getTree();
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        FlatzincWalker walker = new FlatzincWalker(nodes);
        walker.mSolver = mSolver;
        walker.map = map;
        walker.solve_goal();
    }

    @Test
    public void testSatisfy() throws IOException {
        FlatzincParser fp = parser("solve satisfy;");
        try {
            solve_goal(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

    @Test
    public void testMaximize() throws IOException {
        map.put("a", VariableFactory.bounded("a", 0, 10, mSolver));
        FlatzincParser fp = parser("solve maximize a;");
        try {
            solve_goal(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

    @Test
    public void testMinimize() throws IOException {
        map.put("a", VariableFactory.bounded("a", 0, 10, mSolver));
        FlatzincParser fp = parser("solve minimize a;");
        try {
            solve_goal(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

    @Test
    public void testSatisfy2() throws IOException {
        map.put("a", VariableFactory.bounded("a", 0, 10, mSolver));
        FlatzincParser fp = parser("solve ::int_search([a],input_order,indomain_min, complete) satisfy;");
        try {
            solve_goal(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }


    @Test
    public void testSatisfy3() throws IOException {
        map.put("r", VariableFactory.boundedArray("r", 5, 0, 10, mSolver));
        map.put("s", VariableFactory.boundedArray("s", 5, 0, 10, mSolver));
        map.put("o", VariableFactory.bounded("o", 0, 10, mSolver));
        FlatzincParser fp = parser(
                "solve\n" +
                        "  ::seq_search(\n" +
                        "    [ int_search(r, input_order, indomain_min, complete),\n" +
                        "      int_search(s, input_order, indomain_min, complete) ])\n" +
                        "  minimize o;"
        );
        try {
            solve_goal(fp);
        } catch (RecognitionException e) {
            Assert.fail();
        }
    }

}
