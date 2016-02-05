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
package org.chocosolver.samples.integer;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;

/**
 * A verbal arithmetic puzzle:
 * <br/> <br/>
 * Attribute a value to each letter, such that the equations are correct.
 * <p/>
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 02/08/11
 */
public class Alpha extends AbstractProblem {

    IntVar[] letters;

    @Override
    public void createSolver() {
        solver = new Solver("Alpha");
    }

    @Override
    public void buildModel() {
        letters = new IntVar[26];
        for (int i = 0; i < 26; i++) {
            letters[i] = solver.intVar("" + (char) (97 + i), 1, 26, true);
        }
        solver.sum(extract("ballet"), "=", 45).post();
        solver.sum(extract("cello"), "=", 43).post();
        solver.sum(extract("concert"), "=", 74).post();
        solver.sum(extract("flute"), "=", 30).post();
        solver.sum(extract("fugue"), "=", 50).post();
        solver.sum(extract("glee"), "=", 66).post();
        solver.sum(extract("jazz"), "=", 58).post();
        solver.sum(extract("lyre"), "=", 47).post();
        solver.sum(extract("oboe"), "=", 53).post();
        solver.sum(extract("opera"), "=", 65).post();
        solver.sum(extract("polka"), "=", 59).post();
        solver.sum(extract("quartet"), "=", 50).post();
        solver.sum(extract("saxophone"), "=", 134).post();
        solver.sum(extract("scale"), "=", 51).post();
        solver.sum(extract("solo"), "=", 37).post();
        solver.sum(extract("song"), "=", 61).post();
        solver.sum(extract("soprano"), "=", 82).post();
        solver.sum(extract("theme"), "=", 72).post();
        solver.sum(extract("violin"), "=", 100).post();
        solver.sum(extract("waltz"), "=", 34).post();
        solver.allDifferent(letters, "BC").post();
    }

    private IntVar[] extract(String word) {
        IntVar[] ivars = new IntVar[word.length()];
        for (int i = 0; i < word.length(); i++) {
            ivars[i] = letters[word.charAt(i) - 97];
        }
        return ivars;
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.minDom_LB(letters));
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder("Alpha\n");
        st.append("\t");
        for (int i = 0; i < 26; i++) {
            st.append(letters[i].getName()).append("= ").append(letters[i].getValue()).append(" ");
            if (i % 6 == 5) {
                st.append("\n\t");
            }
        }
        st.append("\n");
        System.out.println(st.toString());
    }

    public static void main(String[] args) {
        new Alpha().execute(args);
    }
}
