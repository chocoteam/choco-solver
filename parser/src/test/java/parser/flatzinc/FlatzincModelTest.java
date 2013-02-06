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

package parser.flatzinc;

import gnu.trove.map.hash.THashMap;
import org.testng.Assert;
import org.testng.annotations.Test;
import parser.flatzinc.ast.GoalConf;
import solver.Solver;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 13 janv. 2010
* Since : Choco 2.1.1
* 
*/
public class FlatzincModelTest {

    @Test(groups = "1s")
    public void test1() {
        StringBuilder st = new StringBuilder();
        st.append("var 1 .. 2: a::output_var;");
        st.append("constraint int_ne(a, 1);");
        st.append("solve satisfy;");

        InputStream in = new ByteArrayInputStream(st.toString().getBytes());

        ParseAndSolve pas = new ParseAndSolve();
        Solver solver = new Solver();
        pas.buildParser(in, solver, new THashMap<String, Object>(), new GoalConf());

        solver.solve();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test2() {
        StringBuilder st = new StringBuilder();
        st.append("var 1 .. 2: a::output_var;\n" +
                "constraint int_ne(a, 1);\n" +
                "solve satisfy;");
        InputStream in = new ByteArrayInputStream(st.toString().getBytes());

        ParseAndSolve pas = new ParseAndSolve();
        Solver solver = new Solver();
        pas.buildParser(in, solver, new THashMap<String, Object>(), new GoalConf());
        solver.solve();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
    }

    @Test(groups = "1s")
    public void test3() {
        StringBuilder st = new StringBuilder();
        st.append("array[1 .. 2] of var 1 .. \n" +
                "2: q;\n" +
                "constraint int_ne(q[1], q[2]);\n" +
                "solve satisfy;");

        InputStream in = new ByteArrayInputStream(st.toString().getBytes());

        ParseAndSolve pas = new ParseAndSolve();
        Solver solver = new Solver();
        pas.buildParser(in, solver, new THashMap<String, Object>(), new GoalConf());
        solver.solve();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
    }

    ////    @Test( groups = "1s" )
////    public void test50() throws URISyntaxException, ContradictionException {
////        // Best known objective = 1
////        tester("roster_model_chicroster_dataset_1.fzn", true, 1,
////                "objective", 1);
////    }
////
////    @Test( groups = "1s" )
////    public void test51() throws URISyntaxException, ContradictionException {
////        // Best known objective = 0
////        tester("roster_model_chicroster_dataset_2.fzn", true, 1,
////                "objective", 0);
////    }
////
////    @Test( groups = "1s" )
////    public void test52() throws URISyntaxException, ContradictionException {
////        // Best known objective = 0
////        tester("roster_model_chicroster_dataset_3.fzn", true, 1,
////                "objective", 0);
////    }
////
////    @Test( groups = "1s" )
////    public void test6() throws URISyntaxException {
////        tester("talent_scheduling_small.fzn", true, 1, "objective", 0);
////    }
////
////    @Test( groups = "1s" )
////    public void test7() throws URISyntaxException, IOException {
////        tester("talent_scheduling_01_small.fzn", true, 1,
////                "objective", 0);
////    }
////
////    @Test( groups = "1s" )
////    @Ignore
////    public void test8() throws URISyntaxException {
////        tester("black-hole_1.fzn", true, 1,
////                "objective", 0);
////    }
////
////    @Test( groups = "1s" )
////    public void test9() throws URISyntaxException {
////        tester("black-hole_17.fzn", false, 0, "objective", 0);
////    }
////
////    @Test( groups = "1s" )
////    public void test10() throws URISyntaxException {
////        tester("debruijn_binary_02_03.fzn", false, 1, "objective", 0);
////    }
////
    @Test(groups = "1s")
    public void test11() {
        StringBuilder st = new StringBuilder();
        st.append(
                "array[1 .. 3] of int: covers = [1,5,8];\n" +
                        "array[1 .. 3] of int: lbound = [0,1,0];\n" +
                        "array[1 .. 3] of int: ubound = [1,1,1];\n" +
                        "array[1 .. 3] of var 1 .. 10: vars;\n" +
                        "constraint globalCardinalityLowUpChoco(vars, covers, lbound, ubound);\n" +
                        "solve satisfy;");

        InputStream in = new ByteArrayInputStream(st.toString().getBytes());

        ParseAndSolve pas = new ParseAndSolve();
        Solver solver = new Solver();
        pas.buildParser(in, solver, new THashMap<String, Object>(), new GoalConf());
        solver.solve();
        Assert.assertEquals(solver.getMeasures().getSolutionCount(), 1);
    }
//
//    //****************************************************************************************************************//
//    //****************************************************************************************************************//
//    //****************************************************************************************************************//
//
//    private void tester(String filename, boolean opt, int nbSol, String objective, int bestKnownValue) throws URISyntaxException {
//        String f = this.getClass().getResource("/benchmarking").getPath();
//        fzn.loadInstance(new File(f + File.separator + filename));
//        Solver solver = fzn.parse();
//        solver.solve();
//        Assert.assertTrue(solver.getMeasures().getSolutionCount() > 0);
//        Assert.assertEquals(solver.isEntailed(), ESat.TRUE);
//    }

}
