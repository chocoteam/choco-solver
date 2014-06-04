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

package solver.constraints.gary;

import org.testng.annotations.Test;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.Propagator;
import solver.constraints.gary.arborescences.PropArborescence;
import solver.constraints.gary.arborescences.PropArborescence_NaiveForm;
import solver.constraints.gary.degree.PropNodeDegree_AtLeast_Coarse;
import solver.constraints.gary.degree.PropNodeDegree_AtMost_Incr;
import solver.search.loop.monitors.SearchMonitorFactory;
import solver.search.strategy.GraphStrategyFactory;
import solver.search.strategy.strategy.AbstractStrategy;
import solver.variables.graph.DirectedGraphVar;
import util.objects.graphs.Orientation;
import util.objects.setDataStructures.SetType;
import util.tools.ArrayUtils;

import static org.testng.Assert.assertEquals;

public class ArborescenceTest {

    private static SetType graphTypeEnv = SetType.BOOL_ARRAY;
    private static SetType graphTypeKer = SetType.BOOL_ARRAY;

    public static Solver model(int n, int seed, boolean naive, boolean simple, long nbMaxSols) {
        Solver s = new Solver();
        DirectedGraphVar g = new DirectedGraphVar("G", s, n, graphTypeEnv, graphTypeKer, false);
        for (int i = 0; i < n; i++) {
            for (int j = 1; j < n; j++) {
                g.getEnvelopGraph().addArc(i, j);
            }
        }
        int[] preds = new int[n];
        for (int i = 0; i < n; i++) {
            preds[i] = 1;
        }
        preds[0] = 0;
		Propagator[] props = new Propagator[]{
				new PropNodeDegree_AtLeast_Coarse(g, Orientation.PREDECESSORS, preds),
				new PropNodeDegree_AtMost_Incr(g, Orientation.PREDECESSORS, preds)
		};
        if (naive) {
			props = ArrayUtils.append(props,new Propagator[]{new PropArborescence_NaiveForm(g, 0)});
        } else {
			props = ArrayUtils.append(props,new Propagator[]{new PropArborescence(g, 0, simple)});
        }
        AbstractStrategy strategy = GraphStrategyFactory.graphRandom(g, seed);
        s.post(new Constraint("GTest",props));
        s.set(strategy);
        if (nbMaxSols > 0) {
            SearchMonitorFactory.limitSolution(s, nbMaxSols);
        }
        s.findAllSolutions();
        return s;
    }

    @Test(groups = "10s")
    public static void smallTrees() {
        for (int s = 0; s < 3; s++) {
            for (int n = 3; n < 8; n++) {
                System.out.println("Test n=" + n + ", with seed=" + s);
                Solver naive = model(n, s, true, false, -1);
                Solver efficientA = model(n, s, false, true, -1);
                Solver efficientN = model(n, s, false, false, -1);
                System.out.println(naive.getMeasures().getSolutionCount() + " sols");
                assertEquals(naive.getMeasures().getFailCount(), 0);
                assertEquals(naive.getMeasures().getSolutionCount(), efficientA.getMeasures().getSolutionCount());
                assertEquals(naive.getMeasures().getFailCount(), efficientA.getMeasures().getFailCount());
                assertEquals(naive.getMeasures().getSolutionCount(), efficientN.getMeasures().getSolutionCount());
                assertEquals(naive.getMeasures().getFailCount(), efficientN.getMeasures().getFailCount());
            }
        }
    }

    @Test(groups = "10s")
    public static void bigTrees() {
        for (int s = 0; s < 3; s++) {
            int n = 60;
            System.out.println("Test n=" + n + ", with seed=" + s);
            Solver naive = model(n, s, true, false, 10);
            Solver efficientA = model(n, s, false, true, 10);
            Solver efficientN = model(n, s, false, false, 10);
            System.out.println(naive.getMeasures().getSolutionCount() + " sols");
            assertEquals(naive.getMeasures().getFailCount(), 0);
            assertEquals(naive.getMeasures().getSolutionCount(), efficientA.getMeasures().getSolutionCount());
            assertEquals(naive.getMeasures().getFailCount(), efficientA.getMeasures().getFailCount());
            assertEquals(naive.getMeasures().getSolutionCount(), efficientN.getMeasures().getSolutionCount());
            assertEquals(naive.getMeasures().getFailCount(), efficientN.getMeasures().getFailCount());
        }
    }

    @Test(groups = "30s")
    public static void testAllDataStructure() {
        for (SetType ge : SetType.values()) {
            graphTypeEnv = ge;
            graphTypeKer = ge;
            System.out.println("env:" + ge + " ker :" + ge);
            smallTrees();
        }
    }
}
