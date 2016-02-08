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
package org.chocosolver.samples.real;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import static java.lang.String.format;
import static java.lang.System.out;
import static org.chocosolver.solver.ResolutionPolicy.MINIMIZE;
import static org.chocosolver.solver.trace.Chatterbox.showSolutions;
import static org.chocosolver.solver.trace.Chatterbox.showStatistics;
import static org.chocosolver.util.tools.ArrayUtils.append;

/**
 * <br/>
 * <p/>
 * DYLD_LIBRARY_PATH = /Users/gillou/ibex/lib
 *
 * @author Jean-Guillaume Fages, Charles Prud'homme
 * @since 20/07/12
 */
public class SmallSantaClaude {


    public static void main(String[] args) {

        // solver
        Model model = new Model("Santa Claus");

        // input data
        final int n_kids = 3;
        final int n_gifts = 5;
        final int[] gift_price = new int[]{11, 24, 5, 23, 16};
        final int min_price = 5;
        final int max_price = 24;

        // FD variables
        final IntVar[] kid_gift = model.intVarArray("g2k", n_kids, 0, n_gifts, false);
        final IntVar[] kid_price = model.intVarArray("p2k", n_kids, min_price, max_price, true);
        final IntVar total_cost = model.intVar("total cost", min_price * n_kids, max_price * n_kids, true);

        // CD variable
        double precision = 1.e-4;
        final RealVar average = model.realVar("average", min_price, max_price, precision);
        final RealVar average_deviation = model.realVar("average_deviation", 0, max_price, precision);

        // continuous views of FD variables
        RealVar[] realViews = model.realIntViewArray(kid_price, precision);

        // kids must have different gifts
        model.allDifferent(kid_gift, "AC").post();
        // associate each kid with his gift cost
        for (int i = 0; i < n_kids; i++) {
            model.element(kid_price[i], gift_price, kid_gift[i]).post();
        }
        // compute total cost
        model.sum(kid_price, "=", total_cost).post();

        // compute average cost (i.e. average gift cost per kid)
        RealVar[] allRV = append(realViews, new RealVar[]{average, average_deviation});
        model.realIbexGenericConstraint("({0}+{1}+{2})/3={3};(abs({0}-{3})+abs({1}-{3})+abs({2}-{3}))/3={4}", allRV).post();

        // set search strategy (ABS)
        model.getResolver().set(model.getResolver().minDomLBSearch(kid_gift));
        // displays resolution statistics
        showStatistics(model);
        showSolutions(model);
        // print each solution
        model.getResolver().plugMonitor((IMonitorSolution) () -> {
            out.println("*******************");
            for (int i = 0; i < n_kids; i++) {
                out.println(format("Kids #%d has received the gift #%d at a cost of %d euros",
                        i, kid_gift[i].getValue(), kid_price[i].getValue()));
            }
            out.println(format("Total cost: %d euros", total_cost.getValue()));
            out.println(format("Average: %.3f euros per kid", average.getLB()));
            out.println(format("Average deviation: %.3f ", average_deviation.getLB()));
        });
        // find optimal solution (Santa Claus is stingy)
        model.setObjectives(MINIMIZE, average_deviation);
        model.setPrecision(precision);
        model.solve();
        // free IBEX structures from memory
        model.getIbex().release();
    }
}
