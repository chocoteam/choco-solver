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

package choco.kernel.memory.trailing.trail;

import choco.kernel.memory.trailing.StoredLong;


public class StoredLongTrail implements ITrailStorage {


	/**
	 * Stack of backtrackable search variables.
	 */

	private StoredLong[] variableStack;


	/**
	 * Stack of values (former values that need be restored upon backtracking).
	 */

	private long[] valueStack;


	/**
	 * Stack of timestamps indicating the world where the former value
	 * had been written.
	 */

	private int[] stampStack;


	/**
	 * Points the level of the last entry.
	 */

	private int currentLevel;


	/**
	 * A stack of pointers (for each start of a world).
	 */

	private int[] worldStartLevels;

	/**
	 * capacity of the trailing stack (in terms of number of updates that can be stored)
	 */
	private int maxUpdates = 0;


	/**
	 * Constructs a trail with predefined size.
	 *
	 * @param nUpdates maximal number of updates that will be stored
     * @param nWorlds  maximal number of worlds that will be stored
     */

	public StoredLongTrail(int nUpdates, int nWorlds) {
		currentLevel = 0;
		maxUpdates = nUpdates;
		variableStack = new StoredLong[maxUpdates];
		valueStack = new long[maxUpdates];
		stampStack = new int[maxUpdates];
		worldStartLevels = new int[nWorlds];
	}


	/**
	 * Moving up to the next world.
     * @param worldIndex
     */

	public void worldPush(int worldIndex) {
		worldStartLevels[worldIndex] = currentLevel;
	}


	/**
	 * Moving down to the previous world.
     * @param worldIndex
     */

	public void worldPop(int worldIndex) {
        final int wsl = worldStartLevels[worldIndex];
		while (currentLevel > wsl) {
			currentLevel--;
			final StoredLong v = variableStack[currentLevel];
			v._set(valueStack[currentLevel], stampStack[currentLevel]);
		}
	}


	/**
	 * Returns the current size of the stack.
	 */

	public int getSize() {
		return currentLevel;
	}


	/**
	 * Comits a world: merging it with the previous one.
	 */

	public void worldCommit() {
		// TODO
	}


	/**
	 * Reacts when a StoredInt is modified: push the former value & timestamp
	 * on the stacks.
	 */

	public void savePreviousState(StoredLong v, long oldValue, int oldStamp) {
		valueStack[currentLevel] = oldValue;
		variableStack[currentLevel] = v;
		stampStack[currentLevel] = oldStamp;
		currentLevel++;
		if (currentLevel == maxUpdates){
			resizeUpdateCapacity();
        }
	}

	private void resizeUpdateCapacity() {
		final int newCapacity = ((maxUpdates * 3) / 2);
		// first, copy the stack of variables
		final StoredLong[] tmp1 = new StoredLong[newCapacity];
		System.arraycopy(variableStack, 0, tmp1, 0, variableStack.length);
		variableStack = tmp1;
		// then, copy the stack of former values
		final long[] tmp2 = new long[newCapacity];
		System.arraycopy(valueStack, 0, tmp2, 0, valueStack.length);
		valueStack = tmp2;
		// then, copy the stack of world stamps
		final int[] tmp3 = new int[newCapacity];
		System.arraycopy(stampStack, 0, tmp3, 0, stampStack.length);
		stampStack = tmp3;
		// last update the capacity
		maxUpdates = newCapacity;
	}

	public void resizeWorldCapacity(int newWorldCapacity) {
		final int[] tmp = new int[newWorldCapacity];
		System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
		worldStartLevels = tmp;
	}

}
