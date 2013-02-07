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

import common.ESat;
import common.util.iterators.DisposableIntIterator;
import common.util.tools.ArrayUtils;
import org.slf4j.LoggerFactory;
import solver.Solver;
import solver.constraints.IntConstraint;
import solver.constraints.nary.automata.FA.ICostAutomaton;
import solver.constraints.nary.automata.structure.multicostregular.StoredDirectedMultiGraph;
import solver.constraints.propagators.nary.automaton.PropMultiCostRegular;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Julien Menana, Charles Prud'homme
 * @since 18/07/11
 */
public class MultiCostRegular extends IntConstraint<IntVar> {

    public static final String
            MIN_SP = "min_mcr_sol" // minimal solution of the mcr
            ;

    /**
     * The finite automaton which defines the regular language the variable sequence must belong
     */
    protected ICostAutomaton pi;

    /**
     * Layered graph of the unfolded automaton
     */
    protected StoredDirectedMultiGraph graph;

    private final int offset;

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
		this.offset = vars.length;
        this.pi = pi;
        setPropagators(new PropMultiCostRegular(vars, CR, pi));
    }

    @Override
    public ESat isSatisfied(int[] tuple) {
        int word[] = new int[offset];
        System.arraycopy(tuple, 0, word, 0, word.length);
        if (!pi.run(word)) {
            System.err.println("Word is not accepted by the automaton");
            System.err.print("{" + word[0]);
            for (int i = 1; i < word.length; i++)
                System.err.print("," + word[i]);
            System.err.println("}");

            return ESat.FALSE;
        }
        int coffset = vars.length - offset;
        int[] gcost = new int[coffset];
        for (int l = 0; l < graph.layers.length - 2; l++) {
            DisposableIntIterator it = graph.layers[l].getIterator();
            while (it.hasNext()) {
                int orig = it.next();
                DisposableIntIterator arcIter = graph.GNodes.outArcs[orig].getIterator();
                while (arcIter.hasNext()) {
                    int arc = arcIter.next();
                    for (int i = 0; i < coffset; i++)
                        gcost[i] += graph.GArcs.originalCost[arc][i];
                }
                arcIter.dispose();

            }
            it.dispose();
        }
        for (int i = 0; i < gcost.length; i++) {
            if (!vars[coffset + i].instantiated()) {
                LoggerFactory.getLogger("solver").error("z[" + i + "] in MCR should be instantiated : " + vars[coffset + i]);
                return ESat.FALSE;
            } else if (vars[coffset + i].getValue() != gcost[i]) {
                LoggerFactory.getLogger("solver").error("cost: " + gcost[i] + " != z:" + vars[coffset + i].getValue());
                return ESat.FALSE;
            }

        }
        return ESat.TRUE;
    }

    public void setGraph(StoredDirectedMultiGraph graph) {
        this.graph = graph;
    }

}
