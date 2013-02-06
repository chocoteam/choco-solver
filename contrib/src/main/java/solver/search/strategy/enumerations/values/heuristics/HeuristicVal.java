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

package solver.search.strategy.enumerations.values.heuristics;

import common.util.iterators.IntIterator;
import gnu.trove.map.hash.THashMap;

public abstract class HeuristicVal implements IntIterator {

    protected final Action action;

    /**
     * Specific constructor with default action (<code>Action.open_node</code>)
     */
    protected HeuristicVal() {
        this(Action.open_node);
    }

    protected HeuristicVal(Action action) {
        this.action = action;
    }

    /**
     * Duplicate <code>this</code> and its internal structure
     * (objects that do not extend {@link HeuristicVal} are shared, not duplicated)
     *
     * @param map map to store duplicated HeuristicVal
     * @return copy of <code>this</code>
     */
    public abstract HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map);

    /**
     * Update the internal structure of <code>this</code>, according to the given <code>action</code>.
     *
     * @param action action of the update order
     */
    public void update(Action action) {
        if (this.action.equals(action)) {
            doUpdate(action);
        }
    }

    protected abstract void doUpdate(Action action);

    public Action getAction() {
        return action;
    }
}
