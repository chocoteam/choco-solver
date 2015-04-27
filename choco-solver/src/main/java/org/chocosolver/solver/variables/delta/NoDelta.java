/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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
import org.chocosolver.solver.exception.SolverException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 10/02/11
 */
public enum NoDelta implements IEnumDelta, IIntervalDelta, ISetDelta {
    singleton;

    @Override
    public void add(int value, ICause cause) {}

    @Override
    public void lazyClear() {}

    @Override
    public IEnvironment getEnvironment() {
        throw new SolverException("NoDelta#getEnvironment(): forbidden call!");
    }

    @Override
    public void add(int lb, int ub, ICause cause) {}

    @Override
    public int getLB(int idx) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("NoDelta#getLB(): forbidden call, size must be checked before!");
    }

    @Override
    public int getUB(int idx) throws IndexOutOfBoundsException {
        throw new IndexOutOfBoundsException("NoDelta#getUB(): forbidden call, size must be checked before!");
    }

    @Override
    public int get(int idx) {
        throw new IndexOutOfBoundsException("NoDelta#get(): forbidden call, size must be checked before!");
    }

    @Override
    public ICause getCause(int idx) {
        throw new IndexOutOfBoundsException("NoDelta#getCause(): forbidden call, size must be checked before!");
    }

    @Override
    public int size() {
        return 0;
    }

	@Override
	public int getSize(int kerOrEnv) {
		return 0;
	}

	@Override
	public int get(int index, int kerOrEnv) {
		throw new IndexOutOfBoundsException("NoDelta#get(): forbidden call, size must be checked before!");
	}

	@Override
	public ICause getCause(int index, int kerOrEnv) {
		throw new IndexOutOfBoundsException("NoDelta#getCause(): forbidden call, size must be checked before!");
	}
}
