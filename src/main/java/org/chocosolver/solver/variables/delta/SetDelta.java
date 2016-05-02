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
package org.chocosolver.solver.variables.delta;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.search.loop.TimeStampedObject;

/**
 * @author Jean-Guillaume Fages
 * @since Oct 2012
 */
public class SetDelta extends TimeStampedObject implements ISetDelta {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

	private IEnumDelta[] delta;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public SetDelta(IEnvironment environment) {
        super(environment);
        delta = new IEnumDelta[2];
        delta[0] = new EnumDelta(environment);
        delta[1] = new EnumDelta(environment);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

	@Override
    public int getSize(int kerOrEnv) {
        return delta[kerOrEnv].size();
    }

	@Override
    public void add(int element, int kerOrEnv, ICause cause) {
		lazyClear();
        delta[kerOrEnv].add(element, cause);
    }

	@Override
    public void lazyClear() {
        if (needReset()) {
			delta[0].lazyClear();
			delta[1].lazyClear();
			resetStamp();
        }
    }

	@Override
    public int get(int index, int kerOrEnv) {
        return delta[kerOrEnv].get(index);
    }

	@Override
    public ICause getCause(int index, int kerOrEnv) {
        return delta[kerOrEnv].getCause(index);
    }
}
