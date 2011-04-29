/**
*  Copyright (c) 2010, Ecole des Mines de Nantes
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

package choco.kernel.memory.trailing;


import choco.kernel.memory.trailing.trail.StoredVectorTrail;

/**
 * <p>
 * Implements a backtrackable search vector.
 * </p>
 */
public final class StoredVector<E> implements choco.kernel.memory.IStateVector<E> {

	/**
	 * Contains the elements of the vector.
	 */
	private Object[] elementData;

	/**
	 * Contains time stamps for all entries (the world index of the last update for each entry)
	 */

    public int[] worldStamps;

	/**
	 * A backtrackable search with the size of the vector.
	 */

	private StoredInt size;


	/**
	 * The current environment.
	 */

	private final EnvironmentTrailing environment;

    protected final StoredVectorTrail myTrail;

	/**
	 * Constructs a stored search vector with an initial size, and initial values.
	 *
	 * @param env The current environment.
	 */

	public StoredVector(EnvironmentTrailing env) {
		int initialCapacity = MIN_CAPACITY;
		int w = env.getWorldIndex();

		this.environment = env;
		this.elementData = new Object[initialCapacity];
		this.worldStamps = new int[initialCapacity];

		this.size = new StoredInt(env, 0);
        myTrail = env.getVectorTrail();

	}


	public StoredVector(int[] entries) {
		// TODO
		throw new UnsupportedOperationException();
	}

	private boolean rangeCheck(int index) {
		return index < size.get() && index >= 0;
	}
	
	public int size() {
		return size.get();
	}


	public boolean isEmpty() {
		return (size.get() == 0);
	}

	/*    public Object[] toArray() {
        // TODO : voir ci c'est utile
        return new Object[0];
    }*/


	public void ensureCapacity(int minCapacity) {
		int oldCapacity = elementData.length;
		if (minCapacity > oldCapacity) {
			Object[] oldData = elementData;
			int[] oldStamps = worldStamps;
			int newCapacity = (oldCapacity * 3) / 2 + 1;
			if (newCapacity < minCapacity)
				newCapacity = minCapacity;
			elementData = new Object[newCapacity];
			worldStamps = new int[newCapacity];
			System.arraycopy(oldData, 0, elementData, 0, size.get());
			System.arraycopy(oldStamps, 0, worldStamps, 0, size.get());
		}
	}


	public boolean add(E i) {
		int newsize = size.get() + 1;
		ensureCapacity(newsize);
		size.set(newsize);
		elementData[newsize - 1] = i;
		worldStamps[newsize - 1] = environment.getWorldIndex();
		return true;
	}

	public void removeLast() {
		int newsize = size.get() - 1;
		if (newsize >= 0)
			size.set(newsize);
	}

	public E get(int index) {
		if (rangeCheck(index)) {
			return (E)elementData[index];
		}
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
	}


	public E set(int index, E val) {
		if (rangeCheck(index)) {
			assert(this.worldStamps[index] <= environment.getWorldIndex());
			final E oldValue = (E) elementData[index];
			if (val != oldValue) {
				int oldStamp = this.worldStamps[index];
				if (oldStamp < environment.getWorldIndex()) {
					myTrail.savePreviousState(this, index, oldValue, oldStamp);
					worldStamps[index] = environment.getWorldIndex();
				}
				elementData[index] = val;
			}
			return oldValue;
		}
		throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size.get());
	}


	/**
	 * Sets an element without storing the previous value.
	 */

    public E _set(int index, Object val, int stamp) {
    	assert(rangeCheck(index));
    	E oldval = (E) elementData[index];
		elementData[index] = val;
		worldStamps[index] = stamp;
		return oldval;
	}
}
