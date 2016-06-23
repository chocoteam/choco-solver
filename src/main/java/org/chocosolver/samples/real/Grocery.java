/**
 * Copyright (c) 2016, chocoteam
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of samples nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.samples.real;

import org.chocosolver.samples.AbstractProblem;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;

import static org.chocosolver.solver.search.strategy.Search.inputOrderUBSearch;

/**
 * <a href="http://www.mozart-oz.org/documentation/fdt/node21.html">mozart-oz</a>:<br/>
 * "A kid goes into a grocery store and buys four items. The cashier
 * charges $7.11, the kid pays and is about to leave when the cashier
 * calls the kid back, and says ''Hold on, I multiplied the four items
 * instead of adding them; I'll try again; Hah, with adding them the
 * price still comes to $7.11''. What were the prices of the four items?
 * <p/>
 * The model is taken from: Christian Schulte, Gert Smolka, Finite Domain
 * Constraint Programming in Oz. A Tutorial. 2001."
 * <br/>
 * <p/>
 * This problem deals with large domains which result in integer overflows with classical constraints.
 * Thus, this example introduces a dedicated propagator which handles large value products.
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 * @since 08/08/11
 */
public class Grocery extends AbstractProblem {


    IntVar[] itemCost;
    RealVar[] realitemCost;


    @Override
    public void buildModel() {
        model = new Model();
        double epsilon = 0.000001d;
        // 4 integer variables (price in cents)
        itemCost = model.intVarArray("item", 4, 1, 711, true);
        // views as real variables to be used by Ibex
        realitemCost = model.realIntViewArray(itemCost, epsilon);
        model.realIbexGenericConstraint("{0} + {1} + {2} + {3} = 711", realitemCost).post();
        model.realIbexGenericConstraint("{0} * {1}/100 * {2}/100 * {3}/100 = 711", realitemCost).post();
        // symmetry breaking
        model.realIbexGenericConstraint("{0} <= {1};{1} <= {2};{2} <= {3}", realitemCost).post();
    }

    @Override
    public void configureSearch() {
        // choco branching
        model.getSolver().showStatistics();
        model.getSolver().showSolutions();
        model.getSolver().setSearch(inputOrderUBSearch(itemCost));
        // ibex branching
        //		solver.set(new AssignmentInterval(realitemCost, new Cyclic(realitemCost), new RealDomainMiddle()));
    }

    @Override
    public void solve() {
        model.getSolver().solve();
        model.getIbex().release();

        System.out.println("*******");
        int sum = 0;
        long prod = 1;
        for (int i = 0; i < 4; i++) {
            sum += realitemCost[i].getUB();
            prod *= itemCost[i].getValue();
            System.out.println("item " + i + " : " + itemCost[i].getValue());
        }
        prod /= 1000000;
        System.out.println("sum = " + sum + " instead of 711");
        System.out.println("prod = " + prod + " instead of 711");
    }

    public static void main(String[] args) {
        new Grocery().execute(args);
    }
}
