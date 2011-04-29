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

package solver.variables.image;

import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 18/03/11
 */
public final class OppIntVar extends ImageIntVar<IntVar> {


    public OppIntVar(IntVar var) {
        super(var);
        throw new UnsupportedOperationException("opposite variable may be problematic in case of " +
                "bound modification: the event should be swapped, but from now on, it is not possible!");
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
        return var.previousValue(-v);
    }

    @Override
    public int previousValue(int v) {
        return var.nextValue(-v);
    }

    @Override
    public String toString() {
        return "-(" + this.var.getName()+")";
    }


}
