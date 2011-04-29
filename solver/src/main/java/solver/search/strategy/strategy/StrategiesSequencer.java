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

package solver.search.strategy.strategy;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import solver.search.strategy.decision.Decision;
import solver.variables.Variable;

/**
 * A <code>StrategiesSequencer</code> is class for <code>AbstractStrategy</code> composition.
 * <code>this</code> is created with a list of <code>AbstractStrategy</code>, and calling
 * <code>getDecision()</code> retrieves the current active <code>AbstractStrategy</code> and
 * calls the delegate <code>getDecision()</code> method.
 * <br/>
 * A <code>AbstractStrategy</code> becomes "inactive" when no more decision can be computed,
 * ie every decisions have been computed and used.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 5 juil. 2010
 */
@SuppressWarnings({"UnusedDeclaration"})
public class StrategiesSequencer extends AbstractStrategy<Variable> {

    AbstractStrategy[] strategies;

    IStateInt index;

    IStateInt size;

    public StrategiesSequencer(IEnvironment environment, AbstractStrategy... strategies) {
        super(new Variable[0]);
        index = environment.makeInt();
        this.strategies = strategies;
        size = environment.makeInt(strategies.length);
    }

    /**
     * Adds a new strategy at the end of the list of strategies.
     *
     * @param strategy the strategy to add
     */
    public void addStrategy(AbstractStrategy strategy) {
        ensureCapacity();
        int _size = size.get();
        strategies[_size] = strategy;
        size.add(1);
    }

    /**
     * Deletes the current strategy from the list of strategies
     *
     * @param strategy the strategy to delete
     */
    public void deleteStrategy(AbstractStrategy strategy) {
        throw new UnsupportedOperationException();
    }

    /**
     * Increases the capacity of the <code>StrategiesSequencer</code> internal strategies list,
     * to ensure new addings.
     */
    protected void ensureCapacity() {
        int _size = size.get();
        if (strategies.length == _size) {
            AbstractStrategy[] tmp = new AbstractStrategy[_size * 3 / 2 + 1];
            System.arraycopy(strategies, 0, tmp, 0, _size);
            strategies = tmp;
        }
    }

    @Override
    public void init() {
        for (int i = 0; i < strategies.length; i++) {
            strategies[i].init();
        }
    }

    /**
     * {@inheritDoc}
     * Iterates over the declared sub-strategies and gets the overall current decision.
     */
    @Override
    public Decision getDecision() {
        int idx = index.get();
        Decision decision = strategies[idx].getDecision();
        while (decision == null && idx < strategies.length - 1) {
            decision = strategies[++idx].getDecision();
            index.add(1);
        }
        return decision;
    }

    /**
     * {@inheritDoc}
     * This is based on the <code>print()</code> method of every sub-strategies.
     */
    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("Sequence of:\n");
        for (int i = 0; i < strategies.length; i++) {
            st.append("\t").append(strategies[i].toString()).append("\n");
        }
        return st.toString();
    }
}
