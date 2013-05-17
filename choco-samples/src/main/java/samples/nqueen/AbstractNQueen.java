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

package samples.nqueen;

import org.kohsuke.args4j.Option;
import samples.AbstractProblem;
import solver.Solver;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractNQueen extends AbstractProblem {

    @Option(name = "-q", usage = "Number of queens.", required = false)
    int n = 4;
    IntVar[] vars;

    @Override
    public void createSolver() {
        solver = new Solver("NQueen");
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.firstFail_InDomainMin(vars));
    }


    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        /*StringBuilder st = new StringBuilder();
        String line = "+";
        for (int i = 0; i < n; i++) {
            line += "---+";
        }
        line += "\n";
        st.append(line);
        for (int i = 0; i < n; i++) {
            st.append("|");
            for (int j = 0; j < n; j++) {
                st.append((vars[i].getValue() == j + 1) ? " * |" : "   |");
            }
            st.append(MessageFormat.format("\n{0}", line));
        }
        st.append("\n\n\n");
        LoggerFactory.getLogger("bench").info(st.toString());*/
    }
}
