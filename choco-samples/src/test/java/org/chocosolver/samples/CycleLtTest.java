/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.VariableFactory;
import org.chocosolver.util.tools.StringUtils;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import static org.chocosolver.util.tools.StatisticUtils.*;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/03/11
 */
public class CycleLtTest {

    public Solver modeler(int n) {
        int m = n - 1;
        int min = 1;
        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, n, s);
        }
        Constraint[] cstrs = new Constraint[m + 1];
        int i;
        for (i = 0; i < n - 1; i++) {
            cstrs[i] = IntConstraintFactory.arithm(vars[i], "<", vars[i + 1]);
        }
        cstrs[i] = IntConstraintFactory.arithm(vars[n - 1], "<", vars[0]);

        s.post(cstrs);
        s.set(IntStrategyFactory.lexico_LB(vars));
        return s;
    }


    @Test(groups = "1s")
    public void testAll() {
        StringBuilder st = new StringBuilder("\nCycle LT \n");

        Settings nset = new Settings() {
            @Override
            public boolean plugExplanationIn() {
                return true;
            }
        };
        int n = 6;
        int nbIt = 4;
        st.append(StringUtils.pad("TIME ", -7, " "));
        st.append(StringUtils.pad("NODES ", -7, " "));
        st.append(StringUtils.pad("BCKT ", -7, " "));
        st.append(StringUtils.pad("FILTER ", -15, " "));
        st.append(StringUtils.pad("EVENTS ", -15, " "));
        st.append(StringUtils.pad("PUSHED ", -15, " "));
        st.append(StringUtils.pad("POPPED ", -15, " "));
        st.append(StringUtils.pad("(DIFF)", -15, " "));
        float[] times = new float[nbIt];
        for (int j = 0; j < PropagationEngineFactory.values().length; j++) {
            LoggerFactory.getLogger("test").info(st.toString());
            st.setLength(0);
            st.append("-- ").append(j).append(" ------------------------------------------------------------------------------------\n");
            for (int i = 0; i < nbIt; i++) {
                Solver rand = modeler(n);
                rand.set(nset);
                PropagationEngineFactory.values()[j].make(rand);
                rand.findAllSolutions();
                st.append(StringUtils.pad(String.format("%.3f ", rand.getMeasures().getInitialPropagationTimeCount()), -7, " "));
                times[i] = rand.getMeasures().getInitialPropagationTimeCount();
                st.append(StringUtils.pad(String.format("%d ", rand.getMeasures().getNodeCount()), -7, " "));
                st.append(StringUtils.pad(String.format("%d ", rand.getMeasures().getBackTrackCount()), -7, " "));
                LoggerFactory.getLogger("test").info(st.toString());
                st.setLength(0);
            }
            st.append(StringUtils.pad(String.format("MOYENNE : %fms ", mean(prepare(times))), -15, " "));
            st.append(StringUtils.pad(String.format("DEVIATION : %fms ", standarddeviation(prepare(times))), -15, " "));
            LoggerFactory.getLogger("test").info(st.toString());
            st.setLength(0);
        }
    }

}
