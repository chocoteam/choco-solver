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

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.IntConstraintFactory;
import org.chocosolver.solver.constraints.real.RealConstraint;
import org.chocosolver.solver.search.loop.monitors.IMonitorSolution;
import org.chocosolver.solver.search.strategy.IntStrategyFactory;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/07/12
 */
public class SantaClaude extends AbstractProblem {

    int n_gifts = 20;
    int n_kids = 10;
    int max_price = 25;
    int[] gift_price;

    IntVar[] kid_gift;
    IntVar[] kid_price;
    IntVar total_cost;
    RealVar average;

    @Override
    public void createSolver() {
        solver = new Solver("Santa Claude");
    }

    @Override
    public void buildModel() {
        Random rand = new Random(29091981);
        double precision = 1.e-6;

        kid_gift = solver.intVarArray("g2k", n_kids, 0, n_gifts, false);
        kid_price = solver.intVarArray("p2k", n_kids, 0, max_price, false);
        total_cost = solver.intVar("total cost", 0, max_price * n_kids, true);
        average = solver.realVar("average", 0, max_price * n_kids, precision);


        gift_price = new int[n_gifts];
        for (int i = 0; i < n_gifts; i++) {
            gift_price[i] = rand.nextInt(max_price) + 1;
        }
        solver.post(IntConstraintFactory.alldifferent(kid_gift, "BC"));
        for (int i = 0; i < n_kids; i++) {
            solver.post(IntConstraintFactory.element(kid_price[i], gift_price, kid_gift[i], 0, "detect"));
        }
        solver.post(IntConstraintFactory.sum(kid_price, "=", total_cost));

        StringBuilder funBuilder = new StringBuilder("(");
        for (int i = 0; i < n_kids; i++) {
            funBuilder.append("+{").append(i).append('}');
        }
        funBuilder.append(")/").append(n_kids).append("=").append('{').append(n_kids).append('}');

        RealVar[] all_vars = ArrayUtils.append(solver.realIntViewArray(kid_price, precision), new RealVar[]{average});
        String function = funBuilder.toString();

        solver.post(solver.realIbexGenericConstraint(function, all_vars));
    }

    @Override
    public void configureSearch() {
        solver.set(IntStrategyFactory.random_value(kid_gift, 29091981));
    }

    @Override
    public void solve() {
        solver.plugMonitor((IMonitorSolution) () -> {
            System.out.println("*******************");
            for (int i = 0; i < n_kids; i++) {
                System.out.println(String.format("Kids #%d has received the gift #%d at a cost of %d euros",
                        i, kid_gift[i].getValue(), kid_price[i].getValue()));
            }
            System.out.println(String.format("Total cost: %d euros", total_cost.getValue()));
            System.out.println(String.format("Average: [%.3f,%.3f] euros", average.getLB(), average.getUB()));
        });
        solver.findAllSolutions();
    }

    @Override
    public void prettyOut() {
        solver.getIbex().release();
    }

    public static void main(String[] args) {
        new SantaClaude().execute(args);
    }
}
