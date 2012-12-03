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

package samples;

import choco.kernel.common.util.tools.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;
import solver.Configuration;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.ConstraintFactory;
import solver.propagation.PropagationStrategies;
import solver.search.strategy.StrategyFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;

import static choco.kernel.common.util.tools.StatisticUtils.*;

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
        int max = n;
        Solver s = new Solver();
        IntVar[] vars = new IntVar[n];
        for (int i = 0; i < vars.length; i++) {
            vars[i] = VariableFactory.enumerated("v_" + i, min, max, s);
        }
        Constraint[] cstrs = new Constraint[m + 1];
        int i;
        for (i = 0; i < n - 1; i++) {
            cstrs[i] = ConstraintFactory.lt(vars[i], vars[i + 1], s);
        }
        cstrs[i] = ConstraintFactory.lt(vars[n - 1], vars[0], s);

        s.post(cstrs);
        s.set(StrategyFactory.presetI(vars, s.getEnvironment()));
        return s;
    }


    @Test(groups = "1s")
    public void testAll() {
		if(Configuration.PLUG_EXPLANATION){
			Logger log = LoggerFactory.getLogger("bench");
			StringBuilder st = new StringBuilder("\nCycle LT \n");

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
			for (int j = 0; j < PropagationStrategies.values().length; j++) {
				log.info(st.toString());
				st.setLength(0);
				st.append("-- " + j + " ------------------------------------------------------------------------------------\n");
				for (int i = 0; i < nbIt; i++) {
					Solver rand = modeler(n);
					PropagationStrategies.values()[j].make(rand);
					rand.findAllSolutions();
					st.append(StringUtils.pad(String.format("%.3f ", rand.getMeasures().getInitialPropagationTimeCount()), -7, " "));
					times[i] = rand.getMeasures().getInitialPropagationTimeCount();
					st.append(StringUtils.pad(String.format("%d ", rand.getMeasures().getNodeCount()), -7, " "));
					st.append(StringUtils.pad(String.format("%d ", rand.getMeasures().getBackTrackCount()), -7, " "));
					log.info(st.toString());
					st.setLength(0);
				}
				st.append(StringUtils.pad(String.format("MOYENNE : %fms ", mean(prepare(times))), -15, " "));
				st.append(StringUtils.pad(String.format("DEVIATION : %fms ", standarddeviation(prepare(times))), -15, " "));
				log.info(st.toString());
				st.setLength(0);
			}
		}
    }

}
