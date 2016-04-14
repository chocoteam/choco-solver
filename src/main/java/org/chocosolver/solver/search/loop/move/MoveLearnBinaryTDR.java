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
package org.chocosolver.solver.search.loop.move;

import gnu.trove.map.TObjectDoubleMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.search.loop.learn.LearnExplained;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.*;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeMap;

/**
 * A combination of Move and Learn which results in tabu Decision-repair[1] (TDR) with binary decisions.
 * <p>
 * [1]: N. Jussien, O. Lhomme, Local search with constraint propagation and conflict-based heuristics, AI-139 (2002).
 * <p>
 * Created by cprudhom on 04/11/2015.
 * Project: choco.
 */
public class MoveLearnBinaryTDR extends LearnExplained implements Move {

    private static final boolean DEBUG = true;

    /**
     * Internal reference to Move for simulating multiple extension.
     */
    private final Move move;

    /**
     * List of "n" last conflicts.
     */
    private List<List<IntDecision>> gamma;

    /**
     * Limited size of conflicts to store in the tabu list.
     */
    private int s;

    /**
     * The neigbhor of the current decision path.
     */
    private IntDecision[] neighbor;

    /**
     * Current decision in CD.
     */
    private int current;

    /**
     * Ordered list (decreasing weight) of decisions in k.
     */
    private TIntObjectHashMap<TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>>> weights;

    /**
     * An array to maintain ordered the index of decision in the conflict.
     */
    private TreeMap<Integer, Double> L;

    /**
     * Indicates that no neighnor has been found, and the search can then stop.
     */
    private boolean stop = false;

    private MoveLearnBinaryTDR(Model aModel, Move move, int tabuListSize) {
        super(aModel, false, false);
        this.move = move;
        this.s = tabuListSize;
        this.gamma = new ArrayList<>();
        this.weights = new TIntObjectHashMap<>(16, .5f, -1);
        this.neighbor = new IntDecision[0];
        this.current = 0;
        this.L = new TreeMap<>();
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extend;
        // as we observe the number of backtracks, no limit can be reached on extend()
        if (current < neighbor.length) {
            DecisionPath dp = solver.getDecisionPath();
            assert neighbor[current] != null;
            dp.pushDecision(neighbor[current++]);
            solver.getEnvironment().worldPush();
            extend = true;
        } else /*cut will checker with propagation */ {
            // TODO: incomplete, have to deal with gamma when extending
            extend = move.extend(solver);
        }
        return extend;
    }

    @Override
    public boolean repair(Solver solver) {
        boolean repair;
        if (current < neighbor.length) {
            solver.restart();
            // check stop conditionsn for instance, when no compatible decision path can be computed
            repair = !stop;
        } else {
            repair = move.repair(solver);
        }
        return repair;
    }

    /**
     * {@inheritDoc} main reason we implement this Learn.
     */
    @Override
    public void onFailure(Solver solver) {
        super.onFailure(solver);
        neighbor(solver);
    }

    private void neighbor(Solver solver) {
        Explanation expl = getLastExplanation();
        List<IntDecision> k = extractConlict(solver, expl);
        // add k to the list of conflicts
        gamma.add(k);
        // remove the oldest element in gamma if the tabu size is met
        if (gamma.size() > s) {
            List<IntDecision> r = gamma.remove(0);
            // update weight of decisions
            decWeight(r);
        }
        // prepare getting the decision of k in decreasing order wrt to their weights
        if (DEBUG) {
            System.out.printf("G:\n");
            for (List<IntDecision> g : gamma) {
                System.out.printf("\t");
                for (int i = g.size() - 1; i > -1; i--) {
                    IntDecision d = g.get(i);
                    System.out.printf("%s %s %d, ",
                            d.getDecisionVariable().getName(),
                            d.getDecOp(),
                            d.getDecisionValue());
                }
                System.out.printf("\n");
            }
        }
        boolean compatible;
        System.out.printf("CD:\n");
        do {
            int j = L.lastKey();
            IntDecision d = neighbor[j].flip();
            neighbor[j].free();
            neighbor[j] = d;
            if (DEBUG) System.out.printf("\t%s ? \n", Arrays.toString(neighbor));
            compatible = isCDcompatible();
            if (!compatible) {
                d = neighbor[j].flip();
                neighbor[j].free();
                neighbor[j] = d;
            }
            L.remove(j);
        } while (!compatible && !L.isEmpty());
        if (DEBUG) System.out.printf("%s\n", compatible);
        stop = !compatible;
    }

    /**
     * Check if CD is compatible with any conflict of gamma.
     *
     * @return <tt>true</tt> if all conflicts of gamma are compatible with CD, <tt>false</tt> otherwise.
     */
    private boolean isCDcompatible() {
        boolean compatible = true;
        // iteration over all stored conflicts
        // the last conflict added is ignored, since it is trivially compatible with any CD
        for (int i = 0; i < gamma.size() - 1 && compatible; i++) {
            if (isSubsetOfNeighbor(gamma.get(i))) {
                compatible = false;
            }
        }
        return compatible;
    }


    /**
     * Returns <tt>true</tt> if <code>conflict</code> is contained in <code>neighbor</code>, <tt>false</tt> otherwise.
     *
     * @param conflict one of the stored conflict
     * @return <tt>true</tt> if <code>conflict</code> is contained in <code>neighbor</code>, <tt>false</tt> otherwise.
     */
    private boolean isSubsetOfNeighbor(List<IntDecision> conflict) {
        boolean subset = true;
        // for all decisions in the conflict
        for (int j = 0; j < conflict.size() && subset; j++) {
            IntVar var = conflict.get(j).getDecisionVariable();
            int val = conflict.get(j).getDecisionValue();
            DecisionOperator dop = conflict.get(j).getDecOp();
            boolean contains = false;
            // iteration over decisions in the neighbor
            for (int k = 0; k < neighbor.length && !contains; k++) {
                // if the decision is not found, the neighbor is not in conflict
                contains = (dop == neighbor[k].getDecOp()
                        && var == neighbor[k].getDecisionVariable()
                        && val == neighbor[k].getDecisionValue());
            }
            subset = contains;
        }
        return subset;
    }

    private void incWeight(List<IntDecision> k, int i, double w) {
        TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>> _w1 = weights.get(k.get(i).getDecisionVariable().getId());
        if (_w1 == null) {
            _w1 = new TIntObjectHashMap<>(10, .5f, Integer.MAX_VALUE);
            weights.put(k.get(i).getDecisionVariable().getId(), _w1);
        }
        TObjectDoubleMap<DecisionOperator> _w2 = _w1.get(k.get(i).getDecisionValue());
        if (_w2 == null) {
            _w2 = new TObjectDoubleHashMap<>(10, .5f, 0);
            _w1.put(k.get(i).getDecisionValue(), _w2);
        }
        _w2.adjustOrPutValue(k.get(i).getDecOp(), w, w);
    }

    private void decWeight(List<IntDecision> k) {
        int size = k.size();
        double w = -1d / size;
        for (int i = 0; i < size; i++) {
            TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>> _w1 = weights.get(k.get(i).getDecisionVariable().getId());
            TObjectDoubleMap<DecisionOperator> _w2 = _w1.get(k.get(i).getDecisionValue());
            _w2.adjustValue(k.get(i).getDecOp(), w);
        }
    }

    private double getWeight(List<IntDecision> k, int i) {
        TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>> _w1 = weights.get(k.get(i).getDecisionVariable().getId());
        TObjectDoubleMap<DecisionOperator> _w2 = _w1.get(k.get(i).getDecisionValue());
        return _w2.get(k.get(i).getDecOp());
    }

    private List<IntDecision> extractConlict(Solver solver, Explanation lastExplanation) {
        int offset = solver.getSearchWorldIndex();
        int wi = solver.getEnvironment().getWorldIndex() - 1;
        int k = wi - offset;
        int size = lastExplanation.getDecisions().cardinality();
        double w = 1d / size;

        // prepare internal data for record
        current = 0;
        neighbor = new IntDecision[k];
        L.clear();

        // start iteration over decisions
        List<IntDecision> md = new ArrayList<>();
        DecisionPath dp = solver.getDecisionPath();
        int last = dp.size() - 1;
        Decision decision;
        IntDecision id;
        if (DEBUG) System.out.printf("Conflict: ");
        while (last > 0) { // all decisions needs to be explored
            decision = dp.getDecision(last--);
            id = (IntDecision) decision.duplicate();
            if (decision.triesLeft() != id.triesLeft() - 1) {
                id.flip();
            }
            neighbor[--k] = id;
            if (lastExplanation.getDecisions().get(wi)) {
                md.add(id);
                incWeight(md, md.size() - 1, w);
                L.put(k, getWeight(md, md.size() - 1));
                if (DEBUG) System.out.printf("%s, ", neighbor[k]);
            }
            wi--;
        }
        if (DEBUG) System.out.printf("\n");
        assert md.size() == size;
        return md;
    }

    //****************************************************************************************************************//
    //********************************* BASIC IMPLEMENTATIONS ********************************************************//
    //****************************************************************************************************************//

    @Override
    public boolean init() {
        return move.init();
    }

    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        return move.getStrategy();
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        move.setStrategy(aStrategy);
    }

    @Override
    public List<Move> getChildMoves() {
        return move.getChildMoves();
    }

    @Override
    public void setChildMoves(List<Move> someMoves) {
        move.setChildMoves(someMoves);
    }

    @Override
    public void setTopDecisionPosition(int position) {
        move.setTopDecisionPosition(position);
    }
}
