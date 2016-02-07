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
import org.chocosolver.solver.Resolver;
import org.chocosolver.solver.explanations.Explanation;
import org.chocosolver.solver.search.loop.learn.LearnExplained;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.search.strategy.decision.IntDecision;
import org.chocosolver.solver.search.strategy.decision.IntMetaDecision;
import org.chocosolver.solver.search.strategy.decision.RootDecision;
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
    final Move move;

    /**
     * List of "n" last conflicts.
     */
    List<IntMetaDecision> gamma;

    /**
     * Limited size of conflicts to store in the tabu list.
     */
    int s;

    /**
     * The neigbhor of the current decision path.
     */
    IntDecision[] neighbor;

    /**
     * Current decision in CD.
     */
    int current;

    /**
     * Ordered list (decreasing weight) of decisions in k.
     */
    TIntObjectHashMap<TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>>> weights;

    /**
     * An array to maintain ordered the index of decision in the conflict.
     */
    TreeMap<Integer, Double> L;

    /**
     * Indicates that no neighnor has been found, and the search can then stop.
     */
    boolean stop = false;

    /**
     * @param aModel
     * @param move
     * @param tabuListSize
     */
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
    public boolean extend(Resolver resolver) {
        boolean extend;
        // as we observe the number of backtracks, no limit can be reached on extend()
        if (current < neighbor.length) {
            Decision tmp = resolver.getLastDecision();
            resolver.setLastDecision(neighbor[current++]);
            assert resolver.getLastDecision() != null;
            resolver.getLastDecision().setWorldIndex(resolver.getModel().getEnvironment().getWorldIndex());
            resolver.getLastDecision().setPrevious(tmp);
            resolver.getModel().getEnvironment().worldPush();
            extend = true;
        } else /*cut will checker with propagation */ {
            // TODO: incomplete, have to deal with gamma when extending
            extend = move.extend(resolver);
        }
        return extend;
    }

    @Override
    public boolean repair(Resolver resolver) {
        boolean repair;
        if (current < neighbor.length) {
            resolver.restart();
            // check stop conditionsn for instance, when no compatible decision path can be computed
            repair = !stop;
        } else {
            repair = move.repair(resolver);
        }
        return repair;
    }

    /**
     * {@inheritDoc} main reason we implement this Learn.
     */
    @Override
    public void onFailure(Resolver resolver) {
        super.onFailure(resolver);
        neighbor(resolver);
    }

    private void neighbor(Resolver resolver) {
        Explanation expl = getLastExplanation();
        IntMetaDecision k = extractConlict(resolver, expl);
        // add k to the list of conflicts
        gamma.add(k);
        // remove the oldest element in gamma if the tabu size is met
        if (gamma.size() > s) {
            IntMetaDecision r = gamma.remove(0);
            // update weight of decisions
            decWeight(r);
            r.free();
        }
        // prepare getting the decision of k in decreasing order wrt to their weights
        if (DEBUG) {
            System.out.printf("G:\n");
            for (IntMetaDecision g : gamma) {
                System.out.printf("\t");
                for (int i = g.size() - 1; i > -1; i--) {
                    System.out.printf("%s %s %d, ", g.getVar(i).getName(), g.getDop(i), g.getVal(i));
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
    private boolean isSubsetOfNeighbor(IntMetaDecision conflict) {
        boolean subset = true;
        // for all decisions in the conflict
        for (int j = 0; j < conflict.size() && subset; j++) {
            IntVar var = conflict.getVar(j);
            int val = conflict.getVal(j);
            DecisionOperator dop = conflict.getDop(j);
            boolean contains = false;
            // iteration over decisions in the neighbor
            for (int k = 0; k < neighbor.length && !contains; k++) {
                // if the decision is not found, the neighbor is not in conflict
                contains = (dop == neighbor[k].getDecOp()
                        && var == neighbor[k].getDecisionVariables()
                        && val == neighbor[k].getDecisionValue());
            }
            subset = contains;
        }
        return subset;
    }

    private void incWeight(IntMetaDecision k, int i, double w) {
        TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>> _w1 = weights.get(k.getVar(i).getId());
        if (_w1 == null) {
            _w1 = new TIntObjectHashMap<>(10, .5f, Integer.MAX_VALUE);
            weights.put(k.getVar(i).getId(), _w1);
        }
        TObjectDoubleMap<DecisionOperator> _w2 = _w1.get(k.getVal(i));
        if (_w2 == null) {
            _w2 = new TObjectDoubleHashMap<>(10, .5f, 0);
            _w1.put(k.getVal(i), _w2);
        }
        _w2.adjustOrPutValue(k.getDop(i), w, w);
    }

    private void decWeight(IntMetaDecision k) {
        int size = k.size();
        double w = -1d / size;
        for (int i = 0; i < size; i++) {
            TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>> _w1 = weights.get(k.getVar(i).getId());
            TObjectDoubleMap<DecisionOperator> _w2 = _w1.get(k.getVal(i));
            _w2.adjustValue(k.getDop(i), w);
        }
    }

    private double getWeight(IntMetaDecision k, int i) {
        TIntObjectHashMap<TObjectDoubleMap<DecisionOperator>> _w1 = weights.get(k.getVar(i).getId());
        TObjectDoubleMap<DecisionOperator> _w2 = _w1.get(k.getVal(i));
        return _w2.get(k.getDop(i));
    }

    private IntMetaDecision extractConlict(Resolver resolver, Explanation lastExplanation) {
        int offset = resolver.getSearchWorldIndex();
        int wi = resolver.getModel().getEnvironment().getWorldIndex() - 1;
        int k = wi - offset;
        int size = lastExplanation.getDecisions().cardinality();
        double w = 1d / size;

        // prepare internal data for record
        current = 0;
        neighbor = new IntDecision[k];
        L.clear();

        // start iteration over decisions
        IntMetaDecision md = new IntMetaDecision();
        Decision decision = resolver.getLastDecision();
        IntDecision id;
        if (DEBUG) System.out.printf("Conflict: ");
        while (decision != RootDecision.ROOT) { // all decisions needs to be explored
            id = (IntDecision) decision.duplicate();
            if (decision.triesLeft() != id.triesLeft() - 1) {
                id.flip();
            }
            neighbor[--k] = id;
            if (lastExplanation.getDecisions().get(wi)) {
                md.add(id.getDecisionVariables(),
                        id.getDecisionValue(),
                        id.getDecOp());
                incWeight(md, md.size() - 1, w);
                L.put(k, getWeight(md, md.size() - 1));
                if (DEBUG) System.out.printf("%s, ", neighbor[k]);
            }
            wi--;
            decision = decision.getPrevious();
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
    public void setTopDecision(Decision topDecision) {
        move.setTopDecision(topDecision);
    }
}
