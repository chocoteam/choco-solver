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

package solver.search.strategy.enumerations.values.heuristics.unary;

import solver.search.strategy.enumerations.values.comparators.IntComparator;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/01/11
 */
public class Max extends UnaryHeuristicVal<HeuristicVal> {

    IntComparator comp;

    private Max(Action action) {
        super(action);
    }

    public Max(HeuristicVal sub, IntComparator comp) {
        super(sub);
        this.comp = comp;
    }

    public Max(HeuristicVal sub, IntComparator comp, Action action) {
        super(sub, action);
        this.comp = comp;
    }

    @Override
    public void update(Action action) {
        sub.update(action);
        comp.update(action);
    }

    @Override
    protected void doUpdate(Action action) {}

    @Override
    protected UnaryHeuristicVal duplicate() {
        Max duplicata = new Max(action);
        duplicata.comp = this.comp;
        return duplicata;
    }

    @Override
    public boolean hasNext() {
        return sub.hasNext();
    }

    @Override
    public int next() {
        int max = sub.next();
        while (sub.hasNext()) {
            int value = sub.next();
            if (comp.compare(max, value) < 0) {
                max = value;
            }
        }
        return max;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Max.remove not implemented");
    }
}
