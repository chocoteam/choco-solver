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
package org.chocosolver.solver.search.limits;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.measure.IMeasures;
import org.chocosolver.solver.search.measure.MeasuresRecorder;

/**
 * Overrides all but one services for ICounter and provides easy to implement counter based on {@link IMeasures}.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 29 juil. 2010
 */
public abstract class ACounter implements ICounter {

    protected MeasuresRecorder measures;
    protected long max;

    public ACounter(Model model, long limit) {
        this.max = limit;
        this.measures = model.getSolver().getMeasures();
    }

    @Override
    public void init() {
        // nothing
    }

    @Override
    public void update() {
        // nothing
    }

    @Override
    public boolean isMet() {
        return isMet(max);
    }

    @Override
    public boolean isMet(long value) {
        update();
        return currentValue()>= value;
    }

    @Override
    public final void overrideLimit(long newLimit) {
        max = newLimit;
    }


    @Override
    public long getLimitValue() {
        return max;
    }
}
