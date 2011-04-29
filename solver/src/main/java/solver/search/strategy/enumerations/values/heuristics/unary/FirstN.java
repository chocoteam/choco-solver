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

import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.search.strategy.enumerations.values.metrics.Const;
import solver.search.strategy.enumerations.values.metrics.Metric;

public class FirstN extends UnaryHeuristicVal<HeuristicVal> {
    Metric f;
    int idx;

    private FirstN(Action action) {
        super(action);
    }

    public FirstN(HeuristicVal sub) {
        this(sub, new Const(1));
    }

    public FirstN(HeuristicVal sub, Action action) {
        this(sub, new Const(1), action);
    }

    public FirstN(HeuristicVal sub, Metric metric) {
        super(sub);
        this.f = metric;
        doUpdate(action);
    }

    public FirstN(HeuristicVal sub, Metric metric, Action action) {
        super(sub, action);
        this.f = metric;
        doUpdate(action);
    }

    public boolean hasNext() {
        return idx > 0 && sub.hasNext();
    }

    public int next() {
        idx--;
        return sub.next();
    }

    public void remove() {
        throw new UnsupportedOperationException("FirstN.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        f.update(action);
        idx = f.getValue();
    }

    @Override
    public UnaryHeuristicVal duplicate() {
        FirstN duplicata = new FirstN(this.action);
        duplicata.f = this.f;
        duplicata.idx = this.idx;
        return duplicata;
    }
}
