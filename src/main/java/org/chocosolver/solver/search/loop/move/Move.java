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

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.Variable;

import java.io.Serializable;
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
public interface Move extends Serializable {

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
