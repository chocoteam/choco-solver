/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.loop.move;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import java.util.List;

/**
 * The "Move" component
 * (Inspired from "Unifying search algorithms for CSP" N. Jussien and O. Lhomme, Technical report 02-3-INFO, EMN).
 * <p>
 * The aim of the component, unlike other ones, is not to prune the search space but rather to to explore it.
 * <p>
 * Created by cprudhom on 01/09/15.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 3.3.1
 */
public interface Move  {

    /**
     * Called before the search starts.
     * Also initializes the search strategy.
     * @return false if something goes wrong, true otherwise
     */
    boolean init();

    /**
     * Performs a move when the CSP associated to the current node of the search space is not proven to be not consistent.
     *
     * @param solver reference the solver
     * @return {@code true} if an extension can be done, {@code false} when no more extension is possible.
     */
    boolean extend(Solver solver);

    /**
     * Performs a move when the CSP associated to the current node of the search space is proven to be not consistent.
     *
     * @param solver reference the solver
     * @return {@code true} if a reparation can be done, {@code false} when no more reparation is possible.
     */
    boolean repair(Solver solver);

    /**
     * Returns the search strategy in use.
     * @param <V> the type of variable managed by the strategy
     * @return the current search strategy
     */
    <V extends Variable> AbstractStrategy<V> getStrategy();

    /**
     * Defines a search strategy, that is, a service which computes and returns decisions.
     * @param aStrategy a search strategy
     * @param <V> the type of variable managed by the strategy
     */
    <V extends Variable> void setStrategy(AbstractStrategy<V> aStrategy);

    /**
     * Erases the defined search strategy.
     */
    void removeStrategy();

    /**
     * Returns the child moves or <tt>null</tt>
     * Some Move only accepts one single move as child.
     * @return the child moves
     */
    List<Move> getChildMoves();

    /**
     * Overrides this child moves (if possible and if any).
     * Some Move only accepts one single move as child.
     * @param someMoves a new child move
     * @throws UnsupportedOperationException when the size of Move expected is not respected.
     */
    void setChildMoves(List<Move> someMoves);

    /**
     * Indicates the position of decision made just before selecting this move.
     * When only one "terminal" move is declared, the top decision decision is <i>-1</i>.
     * When dealing with a sequence of Move, the position is the one of the last decision of the previous move.
     * In consequence, when backtracking, the right move can be applied or stopped when needed.
     * This has to be declared on the first call to {@link #extend(Solver)} and is checked on {@link #repair(Solver)}.
     * @param position position of the last decision taken before applying this move
     */
    void setTopDecisionPosition(int position);
}
