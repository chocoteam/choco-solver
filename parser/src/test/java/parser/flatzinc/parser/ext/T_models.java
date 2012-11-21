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
import org.testng.annotations.Test;
import parser.flatzinc.ParseAndSolveExt;
import solver.Solver;

import java.io.ByteArrayInputStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/11/12
 */
public class T_models {

    StringBuilder st;

    public void before() {
        st = new StringBuilder();
        st.append("" +
                "var 0..8: INT____00001;\n" +
                "var 0..9: INT____00002;\n" +
                "var 1..9: INT____00003;\n" +
                "array [1..3] of var 0..9: differences = [INT____00001, INT____00002, INT____00003];\n" +
                "array [1..3] of var 0..9: mark :: output_array([1..3]);\n" +
                "constraint alldifferentChoco(differences);\n" +
                "constraint int_eq(mark[1], 0);\n" +
                "constraint int_lin_eq([-1, -1, 1], [INT____00001, mark[1], mark[2]], 0);\n" +
                "constraint int_lin_eq([-1, -1, 1], [INT____00002, mark[1], mark[3]], 0);\n" +
                "constraint int_lin_eq([-1, -1, 1], [INT____00003, mark[2], mark[3]], 0);\n" +
                "constraint int_lt(INT____00001, INT____00003);\n" +
                "constraint int_lt(mark[1], mark[2]);\n" +
                "constraint int_lt(mark[2], mark[3]);\n");
    }

    public void after() {
        st.append("solve  :: int_search(mark, input_order, indomain, complete) minimize mark[3];\n");
    }


    private void execute(String model) {
        ParseAndSolveExt ps = new ParseAndSolveExt();
        Solver solver = new Solver();
        ps.buildParser(new ByteArrayInputStream(model.getBytes()), solver, new THashMap<String, Object>());
        solver.solve();
    }

    @Test
    public void test1() {
        before();
        st.append("All: true;All as queue(wone) of {each var.name as list(wfor)};");
        after();
        execute(st.toString());
    }

    @Test
    public void test2() {
        before();
        st.append("All: true;queue(wone) of {All};");
        after();
        execute(st.toString());
    }
}
