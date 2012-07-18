/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package samples;

import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.nary.Sum;
import solver.constraints.nary.alldifferent.AllDifferent;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

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
            letters[i] = VariableFactory.bounded("" + (char) (97 + i), 1, 26, solver);
        }
        solver.post(Sum.eq(extract("ballet"), 45, solver));
        solver.post(Sum.eq(extract("cello"), 43, solver));
        solver.post(Sum.eq(extract("concert"), 74, solver));
        solver.post(Sum.eq(extract("flute"), 30, solver));
        solver.post(Sum.eq(extract("fugue"), 50, solver));
        solver.post(Sum.eq(extract("glee"), 66, solver));
        solver.post(Sum.eq(extract("jazz"), 58, solver));
        solver.post(Sum.eq(extract("lyre"), 47, solver));
        solver.post(Sum.eq(extract("oboe"), 53, solver));
        solver.post(Sum.eq(extract("opera"), 65, solver));
        solver.post(Sum.eq(extract("polka"), 59, solver));
        solver.post(Sum.eq(extract("quartet"), 50, solver));
        solver.post(Sum.eq(extract("saxophone"), 134, solver));
        solver.post(Sum.eq(extract("scale"), 51, solver));
        solver.post(Sum.eq(extract("solo"), 37, solver));
        solver.post(Sum.eq(extract("song"), 61, solver));
        solver.post(Sum.eq(extract("soprano"), 82, solver));
        solver.post(Sum.eq(extract("theme"), 72, solver));
        solver.post(Sum.eq(extract("violin"), 100, solver));
        solver.post(Sum.eq(extract("waltz"), 34, solver));
        solver.post(new AllDifferent(letters, solver));
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
        //TODO: changer la strategie pour une plus efficace
        solver.set(StrategyFactory.minDomMinVal(letters, solver.getEnvironment()));
    }

    @Override
    public void configureEngine() {
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Alpha");
        StringBuilder st = new StringBuilder();
        st.append("\t");
        for (int i = 0; i < 26; i++) {
            st.append(letters[i].getName()).append("= ").append(letters[i].getValue()).append(" ");
            if (i % 6 == 5) {
                st.append("\n\t");
            }
        }
        st.append("\n");
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new Alpha().execute(args);
    }
}
