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
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.DecisionPath;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.variables.IntVar;

import java.util.BitSet;
import java.util.Random;

/**
 * A Random LNS
 * <p>
 *
 * @author Charles Prud'homme
 * @since 18/04/13
 */
public class RandomNeighborhood implements INeighbor {

    /**
     * Number of variables to consider in this neighbor
     */
    protected final int n;
    /**
     * Variables to consider in this neighbor
     */
    protected final IntVar[] vars;
    /**
     * Last solution found, wrt {@link #vars}
     */
    protected final int[] bestSolution;
    /**
     * For randomness
     */
    private Random rd;
    /**
     * Size of the fragment
     */
    private double nbFixedVariables = 0d;
    /**
     * Number of times this neighbor is called
     */
    protected int nbCall;
    /**
     * Next time the level should be increased
     */
    protected int limit;
    /**
     * Relaxing factor
     */
    protected final int level;
    /**
     * Indicate which variables are selected to be part of the fragment
     */
    protected BitSet fragment;
    /**
     * Reference to the model
     */
    protected Model mModel;

    /**
     * Create a neighbor for LNS which randomly selects variable to be part of a fragment
     * @param vars variables to consider in this
     * @param level relaxing factor
     * @param seed for randomness
     */
    public RandomNeighborhood(IntVar[] vars, int level, long seed) {
        this.mModel = vars[0].getModel();
        this.n = vars.length;
        this.vars = vars.clone();
        this.level = level;

        this.rd = new Random(seed);
        this.bestSolution = new int[n];
        this.fragment = new BitSet(n);
    }

    @Override
    public void init() {}

    @Override
    public boolean isSearchComplete() {
        return false;
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < vars.length; i++) {
            bestSolution[i] = vars[i].getValue();
        }
        nbFixedVariables = 2. * n / 3. + 1;
        nbCall = 0;
        limit = 200; //geo.getNextCutoff(nbCall);
    }

    @Override
    public void fixSomeVariables(DecisionPath decisionPath) {
        nbCall++;
        restrictLess();
        fragment.set(0, n); // all variables are frozen
        for (int i = 0; i < nbFixedVariables - 1 && fragment.cardinality() > 0; i++) {
            int id = selectVariable();
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                impose(id, decisionPath);
            }
            fragment.clear(id);
        }
    }

    /**
     * Impose a decision to be part of the fragment
     * @param id variable id in {@link #vars}
     * @param decisionPath the current decision path
     */
    protected void impose(int id, DecisionPath decisionPath) {
        IntDecision decision = decisionPath.makeIntDecision(vars[id], DecisionOperator.int_eq, bestSolution[id]);
        decision.setRefutable(false);
        decisionPath.pushDecision(decision);
    }

    /**
     * @return a variable id in {@link #vars} to be part of the fragment
     */
    protected int selectVariable() {
        int id;
        int cc = rd.nextInt(fragment.cardinality());
        for (id = fragment.nextSetBit(0); id >= 0 && cc > 0; id = fragment.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }

    @Override
    public void restrictLess() {
        if (nbCall > limit) {
            limit = nbCall + level;
            nbFixedVariables = rd.nextDouble() * n;
        }
    }
}
