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

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.Variable;
import solver.variables.delta.IntDelta;
import solver.variables.delta.view.ViewDelta;

/**
 * View for -V, where V is a IntVar or view
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 *
 * @author Charles Prud'homme
 * @since 23/08/11
 */
public class MinusView extends ImageIntVar<IntVar> {

    final IntDelta delta;

    public MinusView(final IntVar var, Solver solver) {
        super("-(" + var.getName() + ")", var, solver);
        delta = new ViewDelta(var.getDelta()){

            @Override
            public void add(int value) {
                var.getDelta().add(-value);
            }
        };
    }

    @Override
    public IntDelta getDelta() {
        return delta;
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        return var.removeValue(-value, cause);
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        return var.removeInterval(-to, -from, cause);
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        return var.instantiateTo(-value, cause);
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        return var.updateUpperBound(-value, cause);
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        return var.updateLowerBound(-value, cause);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(-value);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(-value);
    }

    @Override
    public int getValue() {
        return -var.getValue();
    }

    @Override
    public int getLB() {
        return -var.getUB();
    }

    @Override
    public int getUB() {
        return -var.getLB();
    }

    @Override
    public int nextValue(int v) {
        int value = var.previousValue(-v);
        if (value == Integer.MIN_VALUE) return Integer.MAX_VALUE;
        return -value;
    }

    @Override
    public int previousValue(int v) {
        int value = var.nextValue(-v);
        if (value == Integer.MAX_VALUE) return Integer.MIN_VALUE;
        return -value;
    }

    @Override
    public int getType() {
        return Variable.INTEGER;
    }

    @Override
    public String toString() {
        return "-(" + this.var.getName() + ") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public void notifyPropagators(EventType eventType, ICause o) throws ContradictionException {
        if (eventType.mask == 4 || eventType.mask == 8) {
            var.notifyPropagators(eventType.mask == 4 ? EventType.DECUPP : EventType.INCLOW, o);
        } else {
            var.notifyPropagators(eventType, o);
        }
    }
}
