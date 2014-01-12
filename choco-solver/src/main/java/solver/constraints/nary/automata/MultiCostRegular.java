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
package solver.constraints.nary.automata;

import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.structure.multicostregular.StoredDirectedMultiGraph;
import solver.variables.IntVar;
import util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Julien Menana, Charles Prud'homme
 * @since 18/07/11
 */
public class MultiCostRegular extends Constraint<IntVar> {

    /**
     * The finite automaton which defines the regular language the variable sequence must belong
     */
    protected ICostAutomaton pi;

    /**
     * Layered graph of the unfolded automaton
     */
    protected StoredDirectedMultiGraph graph;

    /**
     * Constructs a multi-cost-regular constraint propagator
     *
     * @param vars   decision variables
     * @param CR     cost variables
     * @param pi     finite automaton
     * @param solver solver
     */
    public MultiCostRegular(final IntVar[] vars, final IntVar[] CR, final ICostAutomaton pi, final Solver solver) {
        super(ArrayUtils.append(vars, CR), solver);
        this.pi = pi;
        setPropagators(new PropMultiCostRegular(vars, CR, pi));
    }

    public void setGraph(StoredDirectedMultiGraph graph) {
        this.graph = graph;
    }
}
