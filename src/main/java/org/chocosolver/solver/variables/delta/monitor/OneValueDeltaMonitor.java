/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.variables.delta.monitor;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.search.loop.TimeStampedObject;
import org.chocosolver.solver.variables.delta.IEnumDelta;
import org.chocosolver.solver.variables.delta.IIntDeltaMonitor;
import org.chocosolver.util.procedure.IntProcedure;
import org.chocosolver.util.procedure.SafeIntProcedure;

/**
 * A monitor for OneValueDelta
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 07/12/11
 */
public class OneValueDeltaMonitor extends TimeStampedObject implements IIntDeltaMonitor {

    protected final IEnumDelta delta;
    protected boolean used;
    protected ICause propagator;

    public OneValueDeltaMonitor(IEnumDelta delta, ICause propagator) {
        super(delta.getEnvironment());
        this.delta = delta;
        this.used = false;
        this.propagator = propagator;
    }

    @Override
    public void freeze() {
        if (needReset()) {
            delta.lazyClear();
            used = false;
            resetStamp();
        }
        used = delta.size() == 1;
    }

    @Override
    public void unfreeze() {
        used = false;
        delta.lazyClear(); // fix 27/07/12
    }

    @Override
    public void forEachRemVal(SafeIntProcedure proc) {
		if (used && propagator != delta.getCause(0))
			proc.execute(delta.get(0));
    }

    @Override
    public void forEachRemVal(IntProcedure proc) throws ContradictionException {
		if (used && propagator != delta.getCause(0))
			proc.execute(delta.get(0));
    }

    @Override
   	public int sizeApproximation(){
   		return used && propagator != delta.getCause(0)?1:0;
   	}
}
