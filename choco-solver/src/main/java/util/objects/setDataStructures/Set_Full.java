/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package util.objects.setDataStructures;

/**
 * Fixed Set which ALWAYS contains all integers in range [0,n-1]
 * cannot add or remove elements
 *
 * @author Jean-Guillaume Fages
 * @since 21/10/12
 */
public class Set_Full implements ISet {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    int n;
    int current;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public Set_Full(int n) {
        this.n = n;
        current = 0;
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public boolean add(int element) {
        return false;
    }

    @Override
    public boolean remove(int element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean contain(int element) {
        return true;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public int getSize() {
        return n;
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getFirstElement() {
        current = 0;
        return 0;
    }

    @Override
    public int getNextElement() {
        current++;
        if (current < n)
            return current;
        else
            return -1;
    }

	@Override
	public SetType getSetType(){
		return SetType.BITSET;
	}

	@Override
	public String toString(){
		return "["+0+","+(n-1)+"]";
	}

	@Override
	public int[] toArray(){
		int[] a = new int[n];
		for(int i=0;i<n;i++){
			a[i] = i;
		}
		return a;
	}

	@Override
	public int getMaxSize(){
		return n;
	}
}
