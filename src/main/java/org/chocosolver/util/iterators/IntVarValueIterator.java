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
package org.chocosolver.util.iterators;

import org.chocosolver.solver.variables.IntVar;

import java.util.Iterator;

/**
 * Object to iterate over an IntVar values using
 * <code>
 *     for(int value:var){
 *         ...
 *     }
 * </code>
 * that is equivalent to
 * <code>
 *     int ub = var.getUB();
 *     for(int value = var.getLB(); values <= ub; value = var.nextValue(value)){
 *         ...
 *     }
 * </code>
 *
 * @author Jean-Guillaume Fages
 */
public class IntVarValueIterator implements Iterator<Integer> {

	/**
	 * Variable to iterate on
	 */
	private IntVar var;
    /**
     * current returned value
     */
	private int value;
    /**
     * upper bound of {@link #var}
     */
	private int ub;

	/**
	 * Creates an object to iterate over an IntVar values using
	 * <code>
	 *     for(int value:var){
	 *         ...
	 *     }
	 * </code>
	 * that is equivalent to
	 * <code>
	 *     int ub = var.getUB();
	 *     for(int value = var.getLB(); values <= ub; value = var.nextValue(value)){
	 *         ...
	 *     }
	 * </code>
	 * @param v an integer variables
	 */
	public IntVarValueIterator(IntVar v){
		this.var = v;
	}

	/**
	 * Reset iteration (to avoid creating a new IntVarValueIterator() for every iteration)
	 * Stores the current upper bound
	 */
	public void reset(){
		value = var.getLB()-1;
		ub = var.getUB();
	}

	@Override
	public boolean hasNext() {
		return var.nextValue(value) <= ub;
	}

	@Override
	public Integer next() {
		value = var.nextValue(value);
		return value;
	}
}