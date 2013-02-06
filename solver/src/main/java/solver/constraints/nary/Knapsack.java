/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.constraints.nary;

import common.util.tools.ArrayUtils;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.nary.PropKnapsack;
import solver.variables.IntVar;

/**
 * A knapsack constraint
 * <a href="http://en.wikipedia.org/wiki/Knapsack_problem">wikipedia</a>:<br/>
 * "Given a set of items, each with a weight and an energy value,
 * determine the count of each item to include in a collection so that
 * the total weight is less than or equal to a given limit and the total value is as large as possible.
 * It derives its name from the problem faced by someone who is constrained by a fixed-size knapsack
 * and must fill it with the most useful items."
 * <br/>
 *
 * @author Jean-Guillaume Fages
 * @since 21/06/12
 */
public class Knapsack extends Constraint<IntVar, Propagator<IntVar>> {

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Knapsack(IntVar[] itemOccurence, IntVar capacity, IntVar power,
                    int[] weight, int[] energy, Solver solver) {
        super(ArrayUtils.append(itemOccurence, new IntVar[]{capacity, power}), solver);
        setPropagators(new PropKnapsack(itemOccurence, capacity, power, weight, energy, this, solver));
    }
}
