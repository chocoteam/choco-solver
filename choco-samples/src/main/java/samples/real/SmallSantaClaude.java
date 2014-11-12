/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package samples.real;

import solver.ResolutionPolicy;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.constraints.real.RealConstraint;
import solver.messages.Chatterbox;
import solver.search.loop.monitors.IMonitorSolution;
import solver.search.strategy.IntStrategyFactory;
import solver.variables.IntVar;
import solver.variables.RealVar;
import solver.variables.VariableFactory;
import util.tools.ArrayUtils;

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
        Solver solver = new Solver("Santa Claus");

        // input data
        final int n_kids = 3;
        final int n_gifts = 5;
        final int[] gift_price = new int[]{11, 24, 5, 23, 16};
        final int min_price = 5;
        final int max_price = 24;

        // FD variables
        final IntVar[] kid_gift = VariableFactory.enumeratedArray("g2k", n_kids, 0, n_gifts, solver);
        final IntVar[] kid_price = VariableFactory.boundedArray("p2k", n_kids, min_price, max_price, solver);
        final IntVar total_cost = VariableFactory.bounded("total cost", min_price * n_kids, max_price * n_kids, solver);

        // CD variable
        double precision = 1.e-4;
        final RealVar average = VariableFactory.real("average", min_price, max_price, precision, solver);
        final RealVar average_deviation = VariableFactory.real("average_deviation", 0, max_price, precision, solver);

        // continuous views of FD variables
        RealVar[] realViews = VariableFactory.real(kid_price, precision);

        // kids must have different gifts
        solver.post(IntConstraintFactory.alldifferent(kid_gift, "AC"));
        // associate each kid with his gift cost
        for (int i = 0; i < n_kids; i++) {
            solver.post(IntConstraintFactory.element(kid_price[i], gift_price, kid_gift[i]));
        }
        // compute total cost
        solver.post(IntConstraintFactory.sum(kid_price, total_cost));

        // compute average cost (i.e. average gift cost per kid)
        RealVar[] allRV = ArrayUtils.append(realViews, new RealVar[]{average, average_deviation});
        solver.post(new RealConstraint(
                        "Avg/AvgDev",
                        "({0}+{1}+{2})/3={3};(abs({0}-{3})+abs({1}-{3})+abs({2}-{3}))/3={4}",
                        allRV)
        );

        // set search strategy (ABS)
        solver.set(IntStrategyFactory.minDom_LB(kid_gift));
        // displays resolution statistics
        Chatterbox.showStatistics(solver);
        Chatterbox.showSolutions(solver);
        // print each solution
        solver.plugMonitor(new IMonitorSolution() {
            @Override
            public void onSolution() {
                System.out.println("*******************");
                for (int i = 0; i < n_kids; i++) {
                    System.out.println(String.format("Kids #%d has received the gift #%d at a cost of %d euros",
                            i, kid_gift[i].getValue(), kid_price[i].getValue()));
                }
                System.out.println(String.format("Total cost: %d euros", total_cost.getValue()));
                System.out.println(String.format("Average: %.3f euros per kid", average.getLB()));
                System.out.println(String.format("Average deviation: %.3f ", average_deviation.getLB()));
            }
        });
        // find optimal solution (Santa Claus is stingy)
        solver.findOptimalSolution(ResolutionPolicy.MINIMIZE, average_deviation, precision);
        // free IBEX structures from memory
        solver.getIbex().release();
    }
}
