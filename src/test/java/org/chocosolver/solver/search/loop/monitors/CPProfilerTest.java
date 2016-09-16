/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.loop.monitors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.lns.neighbors.RandomNeighborhood;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ProblemMaker;
import org.testng.annotations.Test;

import java.io.IOException;

import static java.lang.System.out;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 13/09/2016.
 */
public class CPProfilerTest {

    @Test(groups = "1s", timeOut = 60000)
    public void test1() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        try (CPProfiler profiler = new CPProfiler(s1, true)) {
            while (s1.getSolver().solve()) ;
            out.println(s1.getSolver().getSolutionCount());
        }
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test2() throws IOException {
        Model s1 = ProblemMaker.makeCostasArrays(7);
        CPProfiler profiler = new CPProfiler(s1, true);
        while (s1.getSolver().solve()) ;
        out.println(s1.getSolver().getSolutionCount());
        profiler.close();
    }

    @Test(groups = "1s", timeOut = 60000)
    public void test3() throws IOException {
        Model s1 = ProblemMaker.makeGolombRuler(11);
        s1.getSolver().setLNS(new RandomNeighborhood((IntVar[]) s1.getHook("ticks"), 10, 0));
        CPProfiler profiler = new CPProfiler(s1, true);
        s1.getSolver().limitSolution(10);
        while (s1.getSolver().solve()) ;
        out.println(s1.getSolver().getSolutionCount());
        profiler.close();
    }

}