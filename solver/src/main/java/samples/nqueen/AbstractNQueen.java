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

import choco.kernel.common.util.tools.ArrayUtils;
import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.propagation.engines.IPropagationEngine;
import solver.propagation.engines.Policy;
import solver.propagation.engines.comparators.IncrOrderV;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 31/03/11
 */
public abstract class AbstractNQueen extends AbstractProblem {

    @Option(name = "-q", usage = "Number of queens.", required = true)
    int n;
    IntVar[] vars;

    @Override
    public void configureSolver() {
        solver.set(StrategyFactory.firstFailInDomainMin(vars, solver.getEnvironment()));

        IntVar[] orderedVars = orederIt2();
        IPropagationEngine engine = solver.getEngine();
        engine.setDefaultComparator(
                new IncrOrderV(orderedVars)
        );
        engine.setDefaultPolicy(Policy.ITERATE);
        solver.getSearchLoop().getLimitsFactory().setNodeLimit(100000);

    }

    protected IntVar[] orederIt1() {
        List<IntVar> odd = new ArrayList<IntVar>(), even = new ArrayList<IntVar>();
        for (int i = 0; i < n; i += 2) {
            odd.add(vars[i]);
        }
        for (int i = 1; i < n; i += 2) {
            even.add(vars[i]);
        }
        int remainder = n % 6;
        if (remainder == 2) {
            IntVar tmp = odd.remove(1);
            odd.add(0, tmp);
            tmp = odd.remove(2);
            odd.add(tmp);
        }
        if (remainder == 3) {
            IntVar tmp = even.remove(0);
            even.add(tmp);
            tmp = odd.remove(0);
            odd.add(tmp);
            tmp = odd.remove(0);
            odd.add(tmp);
        }
        IntVar[] orderedVars = new IntVar[n];
        int k = 0;
        for (int i = 0; i < even.size(); i++, k++) {
            orderedVars[k] = even.get(i);
        }
        for (int i = 0; i < odd.size(); i++, k++) {
            orderedVars[k] = odd.get(i);
        }
        return orderedVars;
    }

    protected IntVar[] orederIt2() {
        IntVar[] orderedVars = new IntVar[n];
        IntVar[] first = new IntVar[n / 2];
        IntVar[] second = new IntVar[n / 2];
        System.arraycopy(vars, 0, first, 0, n / 2);
        System.arraycopy(vars, n - n / 2, second, 0, n / 2);
        ArrayUtils.reverse(first);
        int k = 0;
        if (n % 2 == 1) {
            orderedVars[k++] = vars[n / 2];
        }
        int i = 0;
        for (; i < n / 2; i++, k += 2) {
            orderedVars[k] = first[i];
            orderedVars[k + 1] = second[i];
        }

        return orderedVars;
    }


    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        StringBuilder st = new StringBuilder();
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
        LoggerFactory.getLogger("bench").info(st.toString());
        st = null;
    }
}
