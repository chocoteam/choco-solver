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
package org.chocosolver.samples.todo.problems.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.StringUtils;
import org.kohsuke.args4j.Option;

import java.text.MessageFormat;

import static org.chocosolver.solver.search.strategy.SearchStrategyFactory.inputOrderLBSearch;

/**
 * <a href="http://en.wikipedia.org/wiki/Latin_square">wikipedia</a>:<br/>
 * "A Latin square is an n x n array filled with n different Latin letters,
 * each occurring exactly once in each row and exactly once in each column"
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/06/11
 */
public class LatinSquare extends AbstractProblem {

    @Option(name = "-n", usage = "Latin square size.", required = false)
    int m = 20;
    IntVar[] vars;

    @Override
    public void buildModel() {
        model = new Model("Latin square");
        vars = new IntVar[m * m];
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                vars[i * m + j] = model.intVar("C" + i + "_" + j, 0, m - 1, false);
            }
        }
        // Constraints
        for (int i = 0; i < m; i++) {
            IntVar[] row = new IntVar[m];
            IntVar[] col = new IntVar[m];
            for (int x = 0; x < m; x++) {
                row[x] = vars[i * m + x];
                col[x] = vars[x * m + i];
            }
            model.allDifferent(col, "AC").post();
            model.allDifferent(row, "AC").post();
        }
    }

    @Override
    public void configureSearch() {
        model.getSolver().set(inputOrderLBSearch(vars));
    }

    @Override
    public void solve() {
        model.solve();

        StringBuilder st = new StringBuilder();
        String line = "+";
        for (int i = 0; i < m; i++) {
            line += "----+";
        }
        line += "\n";
        st.append(line);
        for (int i = 0; i < m; i++) {
            st.append("|");
            for (int j = 0; j < m; j++) {
                st.append(StringUtils.pad((char) (vars[i * m + j].getValue() + 97) + "", -3, " ")).append(" |");
            }
            st.append(MessageFormat.format("\n{0}", line));
        }
        st.append("\n\n\n");
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new LatinSquare().execute(args);
    }
}
