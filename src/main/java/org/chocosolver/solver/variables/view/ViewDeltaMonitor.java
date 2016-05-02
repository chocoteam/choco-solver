/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.variables.view;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.SafeIntProcedure;

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

    private IIntDeltaMonitor deltamonitor;
    private TIntArrayList values;
    private Filler filler;

    public ViewDeltaMonitor(IIntDeltaMonitor deltamonitor) {
        this.deltamonitor = deltamonitor;
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
    public void forEachRemVal(SafeIntProcedure proc) {
        values.clear();
        deltamonitor.forEachRemVal(filler);
        for (int v = 0; v < values.size(); v++) {
            proc.execute(transform(values.toArray()[v]));
        }
    }

    @Override
    public void forEachRemVal(IntProcedure proc) throws ContradictionException {
        values.clear();
        deltamonitor.forEachRemVal(filler);
        for (int v = 0; v < values.size(); v++) {
            proc.execute(transform(values.toArray()[v]));
        }
    }

    @Override
   	public int sizeApproximation(){
   		return deltamonitor.sizeApproximation();
   	}

    protected abstract int transform(int value);
}
