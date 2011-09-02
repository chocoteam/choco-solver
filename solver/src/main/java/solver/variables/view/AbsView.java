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

package solver.variables.view;

import choco.kernel.common.util.iterators.DisposableIntIterator;
import org.slf4j.LoggerFactory;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.requests.ViewRequestWrapper;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.AbstractVariable;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.delta.view.ViewDelta;


/**
 * declare an IntVar based on X, such |X|
 * <p/>
 *
 * @author Charles Prud'homme
 * @since 09/08/11
 */
public final class AbsView extends View<IntVar> {

    final IntDelta delta;

    protected HeuristicVal heuristicVal;

    private AbsIt _iterator;

    public AbsView(final IntVar var, Solver solver) {
        super("|" + var.getName() + "|", var, solver);
        delta = new ViewDelta(var.getDelta()) {

            @Override
            public void add(int value) {
                var.getDelta().add(value);
                var.getDelta().add(-value);
            }
        };
    }

    @Override
    public void attachPropagator(Propagator propagator, int idxInProp) {
        ViewRequestWrapper req = new ViewRequestWrapper(propagator.makeRequest(var, idxInProp),
                ViewRequestWrapper.Modifier.ABS);
        propagator.addRequest(req);
        var.addRequest(req);
    }

    @Override
    public void setHeuristicVal(HeuristicVal heuristicVal) {
        LoggerFactory.getLogger("solver").warn("AbsView#setHeuristicVal: wrong usage");
        this.heuristicVal = heuristicVal;
    }

    @Override
    public HeuristicVal getHeuristicVal() {
        return heuristicVal;
    }

    @Override
    public boolean instantiated() {
        if (var.instantiated()) {
            return true;
        } else {
            if (var.getDomainSize() == 2 && Math.abs(var.getLB()) == var.getUB()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        if (value < 0) {
            return false;
        }
        boolean done = var.removeValue(-value, cause);
        done |= var.removeValue(value, cause);
        return done;
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        if (to < 0) {
            return false;
        }
        if (from < 0) {
            from = 0;
        }
        boolean done = var.removeInterval(-to, -from, cause);
        done |= var.removeInterval(from, to, cause);
        return done;
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        if (value < 0) {
            this.contradiction(cause, AbstractVariable.MSG_UNKNOWN);
        }
        int v = Math.abs(value);
        boolean done = var.updateLowerBound(-v, cause);
        done |= var.updateUpperBound(v, cause);
        if (var.hasEnumeratedDomain()) {
            done |= var.removeInterval(-v + 1, v - 1, cause);
        }
        return done;
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        return value > 0 && var.removeInterval(-value + 1, value - 1, cause);
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        if (value < 0) {
            this.contradiction(cause, AbstractVariable.MSG_UNKNOWN);
        }
        boolean done = var.updateLowerBound(-value, cause);
        done |= var.updateUpperBound(value, cause);
        return done;
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value) || var.contains(-value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(value) || var.instantiatedTo(-value) ||
                var.getDomainSize() == 2 && var.getLB() == var.getUB();
    }

    @Override
    public int getValue() {
        return Math.abs(var.getValue());
    }

    @Override
    public int getLB() {
        if (var.contains(0)) {
            return 0;
        }
        int elb = var.getLB();
        if (elb > 0) {
            return elb;
        }
        int eub = var.getUB();
        if (eub < 0) {
            return -eub;
        }
        int l = var.previousValue(0);
        int u = var.nextValue(0);
        return Math.min(-l, u);
    }

    @Override
    public int getUB() {
        int elb = var.getLB();
        int eub = var.getUB();
        int mm = -elb;
        if (elb < 0) {
            if (eub > 0 && eub > mm) {
                mm = eub;
            }
        } else {
            mm = eub;
        }
        return mm;
    }

    @Override
    public int nextValue(int v) {
        if (v < 0 && var.contains(0)) {
            return 0;
        }
        int l = var.previousValue(-v);
        if (l == Integer.MIN_VALUE) {
            l = Integer.MAX_VALUE;
        } else {
            l = Math.abs(l);
        }
        int u = var.nextValue(v);
        return Math.min(l, Math.abs(u));
    }

    @Override
    public int previousValue(int v) {
        if (v < 0) {
            return Integer.MIN_VALUE;
        }
        int l = var.nextValue(-v);
        if (l == Integer.MIN_VALUE) {
            l = Integer.MAX_VALUE;
        } else {
            l = Math.abs(l);
        }
        int u = var.previousValue(v);
        return Math.max(l, Math.abs(u));
    }

    @Override
    public String toString() {
        return "|" + this.var.toString() + "| = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntDelta getDelta() {
        return delta;
    }

    @Override
    public int getType() {
        return Variable.INTEGER;
    }

    @Override
    public int getDomainSize() {
        int d = 0;
        int ub = getUB();
        for (int val = getLB(); val <= ub; val = nextValue(val)) {
            d++;
        }
        return d;
    }

    @Override
    public DisposableIntIterator getLowUppIterator() {
        if (_iterator == null || !_iterator.isReusable()) {
            _iterator = new AbsItL2U();
        }
        _iterator.init(var);
        return _iterator;
    }

    @Override
    public DisposableIntIterator getUppLowIterator() {
        if (_iterator == null || !_iterator.isReusable()) {
            _iterator = new AbsItU2L();
        }
        _iterator.init(var);
        return _iterator;
    }

    private static abstract class AbsIt extends DisposableIntIterator {

        DisposableIntIterator u2l, l2u;
        int vl2u, vu2l;

        public void init(IntVar var) {
            super.init();
            l2u = var.getLowUppIterator();
            u2l = var.getUppLowIterator();
        }

        @Override
        public void dispose() {
            super.dispose();
            l2u.dispose();
            u2l.dispose();
        }
    }

    private static class AbsItL2U extends AbsIt {

        public void init(IntVar var) {
            super.init(var);
            while (l2u.hasNext()) {
                this.vl2u = l2u.next();
                if (this.vl2u >= 0) break;
            }
            while (u2l.hasNext()) {
                this.vu2l = u2l.next();
                if (this.vu2l <= 0) break;
            }
        }

        @Override
        public boolean hasNext() {
            return this.vl2u < Integer.MAX_VALUE || this.vu2l > -Integer.MAX_VALUE;
        }

        @Override
        public int next() {
            int min = this.vl2u > -this.vu2l ? this.vl2u : -this.vu2l;
            if (this.vl2u == min) {
                if (this.l2u.hasNext()) {
                    this.vl2u = l2u.next();
                } else {
                    this.vl2u = Integer.MAX_VALUE;
                }
            }
            if (-this.vu2l == min) {
                if (this.u2l.hasNext()) {
                    this.vu2l = u2l.next();
                } else {
                    this.vu2l = -Integer.MAX_VALUE;
                }
            }
            return min;
        }

    }

    private static class AbsItU2L extends AbsIt {

        public void init(IntVar var) {
            super.init(var);
            if (l2u.hasNext()) {
                this.vl2u = l2u.next();
            }
            if (u2l.hasNext()) {
                this.vu2l = u2l.next();
            }
        }

        @Override
        public boolean hasNext() {
            return this.vl2u <= 0 && this.vu2l >= 0;
        }

        @Override
        public int next() {
            int max = -this.vl2u > this.vu2l ? -this.vl2u : this.vu2l;
            if (-this.vl2u == max && this.l2u.hasNext()) {
                this.vl2u = this.l2u.next();
            }
            if (this.vu2l == max && this.u2l.hasNext()) {
                this.vu2l = u2l.next();
            }
            return max;
        }

    }
}
