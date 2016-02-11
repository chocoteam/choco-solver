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

import org.chocosolver.samples.integer.GolombRuler;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.Test;

import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.propagation.PropagationEngineFactory.values;
import static org.testng.Assert.assertEquals;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 15/03/11
 */
public class GolombRulerTest {

    public final static int[][] OPTIMAL_RULER = {
            {5, 11}, {6, 17}, {7, 25}, {8, 34}, {9, 44}, {10, 55}//, {11, 72}
    };

    protected Model modeler(int m) {
        GolombRuler pb = new GolombRuler();
        pb.readArgs("-m", Integer.toString(m));
        pb.buildModel();
        pb.configureSearch();
        return pb.getModel();
    }

    @Test(groups="10s", timeOut=60000)
    public void testAll() {
        Model sol;
        for (int j = 0; j < OPTIMAL_RULER.length; j++) {
            sol = modeler(OPTIMAL_RULER[j][0]);
            sol.setObjectives(MINIMIZE, (IntVar) sol.getVars()[OPTIMAL_RULER[j][0] - 1]);
            int nb=0;
            while(sol.solve()){
                nb++;
            }
            long sols = sol.getResolver().getMeasures().getSolutionCount();
            assertEquals(nb, sols);
            long nodes = sol.getResolver().getMeasures().getNodeCount();
            for (int k = 1; k < values().length; k++) {
                sol = modeler(OPTIMAL_RULER[j][0]);
                values()[k].make(sol);
                sol.setObjectives(MINIMIZE, (IntVar) sol.getVars()[OPTIMAL_RULER[j][0] - 1]);
                while(sol.solve());
                assertEquals(sol.getResolver().getMeasures().getSolutionCount(), sols);
                assertEquals(sol.getResolver().getMeasures().getNodeCount(), nodes);

            }
        }
    }

}
