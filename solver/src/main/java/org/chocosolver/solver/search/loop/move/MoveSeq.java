/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;
import org.chocosolver.solver.variables.Variable;

import java.util.Arrays;
import java.util.List;

/**
 * BETA: This a work-in-progress.
 * It is certainly not bug free, specially the repair method may lead to unexpected behavior.
 *
 * A move made of two or more moves.
 * The i^th move is called when the i-1 ^th returns false.
 *
 * Created by cprudhom on 29/10/2015.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 29/10/2015
 */
public class MoveSeq implements Move {

    /**
     * List of moves to consider
     */
    private List<Move> moves;
    /**
     * Index of the current move
     */
    private final IStateInt index;
    /**
     * Sequence of strategies in use
     */
    private final AbstractStrategy seqStrat;

    /**
     * Create a move which sequentially apply a move. When a move can not be extended, the next one is used.
     * @param model a model
     * @param moves list of moves to apply
     */
    public MoveSeq(Model model, Move... moves) {
        this.moves = Arrays.asList(moves);
        this.index = model.getEnvironment().makeInt(0);
        AbstractStrategy[] strats = new AbstractStrategy[moves.length];
        for (int i = 0; i < moves.length; i++) {
            strats[i] = moves[i].getStrategy();
        }
        this.seqStrat = new StrategiesSequencer(strats);
    }

    @Override
    public boolean init() {
        boolean init = true;
        for (int i = 0; i < moves.size() && init; i++) {
            init = moves.get(i).init();
        }
        return init;
    }

    @Override
    public boolean extend(Solver solver) {
        boolean extend = false;
        int i = index.get();
        // if the current Move is able to extend the decision path
        if (i < moves.size()) {
            extend = moves.get(i).extend(solver);
        }
        //otherwise, try the remaining ones.
        while (i < moves.size() - 1 && !extend) {
            // first, store the world index in which the first decision of this move is taken.
            i++;
            moves.get(i).setTopDecisionPosition(solver.getDecisionPath().size());
            extend = moves.get(i).extend(solver);
        }
        index.set(i);
        return extend;
    }

    @Override
    public boolean repair(Solver solver) {
        boolean repair = false;
        int i = index.get() + 1;
        while (i > 0 && !repair) {
            repair = moves.get(--i).repair(solver);
            if (i > 0) {
                solver.getDecisionPath().synchronize();
            }
        }
        index.set(i);
        return repair;
    }

    @Override
    public void setTopDecisionPosition(int position) {
        for (int i = 0; i < moves.size(); i++) {
            moves.get(i).setTopDecisionPosition(position);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <V extends Variable> AbstractStrategy<V> getStrategy() {
        //noinspection unchecked
        return seqStrat;
    }

    @Override
    public <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy) {
        throw new UnsupportedOperationException("A sequential Move does not support declaring search strategy in retrospect.\n" +
                "It has to be done on each of it child nodes.");
    }

    @Override
    public void removeStrategy() {
        throw new UnsupportedOperationException("A sequential Move does not support removing search strategy in retrospect.\n" +
                "It has to be done on each of it child nodes.");
    }

    @Override
    public List<Move> getChildMoves() {
        return moves;
    }

    @Override
    public void setChildMoves(List<Move> someMoves) {
        this.moves = someMoves;
    }
}
