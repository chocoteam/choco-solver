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

package solver.search.strategy.enumerations.values.heuristics.nary;

import gnu.trove.THashMap;
import solver.search.strategy.enumerations.values.comparators.IntComparator;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.heuristics.unary.Lookahead;

public class Join extends NaryHeuristicVal<Lookahead> {

    IntComparator f;

    private Join(Action action) {
        super(action);
    }

    public Join(IntComparator f, HeuristicVal left, HeuristicVal right) {
        super(new Lookahead[]{new Lookahead(left), new Lookahead(right)});
        this.f = f;
    }

    /**
     * Beware: action is NOT given in parameter to Lookahead constructor.
     * @param f comparator
     * @param left first heuristic val
     * @param right second heuristic val
     * @param action action of <code>this</code>
     */
    public Join(IntComparator f, HeuristicVal left, HeuristicVal right, Action action) {
        super(new Lookahead[]{new Lookahead(left, left.getAction()), new Lookahead(right, right.getAction())}, action);
        this.f = f;
    }

    public boolean hasNext() {
        return subs[0].hasNext() || subs[1].hasNext();
    }

    public int next() {
        if (!subs[0].hasNext()) {
            return subs[1].next();
        } else if (!subs[1].hasNext()) {
            return subs[0].next();
        } else if (f.compare(subs[0].peekNext(), subs[1].peekNext()) >= 0) {
            return subs[0].next();
        } else {
            return subs[1].next();
        }
    }

    public void remove() {
        throw new UnsupportedOperationException("Join.remove not implemented");
    }

    @Override
    public void update(Action action) {
        subs[0].update(action);
        subs[1].update(action);
        f.update(action);
    }

    @Override
    protected void doUpdate(Action action) {}

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            Join duplicata = new Join(this.action);
            duplicata.subs = new Lookahead[]{
                    (Lookahead) this.subs[0].duplicate(map),
                    (Lookahead) this.subs[1].duplicate(map)
            };
            duplicata.f = f;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}

