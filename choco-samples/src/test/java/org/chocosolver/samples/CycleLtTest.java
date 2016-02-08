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
package org.chocosolver.samples;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;
import org.chocosolver.solver.propagation.PropagationEngineFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.tools.StringUtils;
import org.testng.annotations.Test;

import static java.lang.String.format;
import static org.chocosolver.solver.propagation.PropagationEngineFactory.values;
import static org.chocosolver.util.tools.StatisticUtils.*;
import static org.chocosolver.util.tools.StringUtils.pad;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 30/03/11
 */
public class CycleLtTest {

    public Model modeler(int n) {
        int m = n - 1;
        int min = 1;
        Model s = new Model();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = s.intVar("v_" + i, min, n, false);
        }
        for (int i = 0; i < n - 1; i++) {
            s.arithm(vars[i], "<", vars[i + 1]).post();
        }
        s.arithm(vars[n - 1], "<", vars[0]).post();
        s.getResolver().set(s.getResolver().firstLBSearch(vars));
        return s;
    }


    @Test(groups="1s", timeOut=60000)
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
            st.setLength(0);
            st.append("-- ").append(j).append(" ------------------------------------------------------------------------------------\n");
            for (int i = 0; i < nbIt; i++) {
                Model rand = modeler(n);
                rand.set(nset);
                values()[j].make(rand);
                while (rand.solve()) ;
                st.append(pad(format("%d ", rand.getResolver().getMeasures().getNodeCount()), -7, " "));
                st.append(pad(format("%d ", rand.getResolver().getMeasures().getBackTrackCount()), -7, " "));
                st.setLength(0);
            }
            st.append(StringUtils.pad(String.format("MOYENNE : %fms ", mean(prepare(times))), -15, " "));
            st.append(StringUtils.pad(String.format("DEVIATION : %fms ", standarddeviation(prepare(times))), -15, " "));
            st.setLength(0);
        }
    }

}
