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

import common.util.procedure.IntProcedure;
import common.util.procedure.SafeIntProcedure;
import gnu.trove.list.array.TIntArrayList;
import solver.ICause;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.delta.IIntDeltaMonitor;

import java.util.ArrayList;

/**
 * A delta monitor dedicated to views
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11/01/13
 */
public abstract class ViewDeltaMonitor implements IIntDeltaMonitor {

    private class Filler implements SafeIntProcedure {

        @Override
        public void execute(int i) {
            values.add(i);
        }
    }

    IIntDeltaMonitor deltamonitor;
    protected ICause propagator;
    protected TIntArrayList values;
    protected ArrayList<ICause> causes;
    protected Filler filler;

    public ViewDeltaMonitor(IIntDeltaMonitor deltamonitor, ICause propagator) {
        this.deltamonitor = deltamonitor;
        this.propagator = propagator;
        values = new TIntArrayList(8);
        filler = new Filler();
    }


    @Override
    public void freeze() {
        this.deltamonitor.freeze();
    }

    @Override
    public void unfreeze() {
        this.deltamonitor.unfreeze();
    }

    @Override
    public void clear() {
        this.deltamonitor.clear();
    }


    @Override
    public void forEach(SafeIntProcedure proc, EventType eventType) {
        values.clear();
        deltamonitor.forEach(filler, eventType);
        filter();
        for (int v = 0; v < values.size(); v++) {
            proc.execute(transform(values.toArray()[v]));
        }
    }

    @Override
    public void forEach(IntProcedure proc, EventType eventType) throws ContradictionException {
        values.clear();
        deltamonitor.forEach(filler, eventType);
        filter();
        for (int v = 0; v < values.size(); v++) {
            proc.execute(transform(values.toArray()[v]));
        }
    }

    protected void filter() {
        // nothing to do
    }

    protected abstract int transform(int value);

}
