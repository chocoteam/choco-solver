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
import solver.variables.IntVar;
import solver.variables.domain.delta.IntDelta;
import solver.variables.domain.delta.image.DeltaAddCste;


/**
 * declare an IntVar based on X and C, such as X + C
 *
 * <p/>
 * Based on "Views and Iterators for Generic Constraint Implementations",
 * C. Schulte and G. Tack
 *
 * @author Charles Prud'homme
 * @since 04/02/11
 */
public final class OffsetView extends ImageIntVar<IntVar> {

    final int cste;
    final IntDelta delta;

    public OffsetView(IntVar var, int cste, Solver solver) {
        super("("+var.getName()+"+"+cste+")", var, solver);
        this.cste = cste;
        delta = new DeltaAddCste(var.getDelta(), cste);
    }

    @Override
    public boolean removeValue(int value, ICause cause) throws ContradictionException {
        return var.removeValue(value - cste, cause);
    }

    @Override
    public boolean removeInterval(int from, int to, ICause cause) throws ContradictionException {
        return var.removeInterval(from - cste, to - cste, cause);
    }

    @Override
    public boolean instantiateTo(int value, ICause cause) throws ContradictionException {
        return var.instantiateTo(value - cste, cause);
    }

    @Override
    public boolean updateLowerBound(int value, ICause cause) throws ContradictionException {
        return var.updateLowerBound(value - cste, cause);
    }

    @Override
    public boolean updateUpperBound(int value, ICause cause) throws ContradictionException {
        return var.updateUpperBound(value - cste, cause);
    }

    @Override
    public boolean contains(int value) {
        return var.contains(value - cste);
    }

    @Override
    public boolean instantiatedTo(int value) {
        return var.instantiatedTo(value - cste);
    }

    @Override
    public int getValue() {
        return var.getValue() + cste;
    }

    @Override
    public int getLB() {
        return var.getLB() + cste;
    }

    @Override
    public int getUB() {
        return var.getUB() + cste;
    }

    @Override
    public int nextValue(int v) {
        return var.nextValue(v - cste);
    }

    @Override
    public int previousValue(int v) {
        return var.previousValue(v - cste);
    }

    @Override
    public String toString() {
        return "("+this.var.getName() + " + " + this.cste+") = [" + getLB() + "," + getUB() + "]";
    }

    @Override
    public IntDelta getDelta() {
       return delta;
    }

	@Override
	public int getType() {
		return INTEGER;
	}
}
