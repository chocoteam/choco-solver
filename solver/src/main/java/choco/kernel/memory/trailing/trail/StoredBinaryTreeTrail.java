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

package choco.kernel.memory.trailing.trail;

import choco.kernel.memory.IStateBinaryTree;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Apr 24, 2008
 * Time: 4:47:45 PM
 */
public class StoredBinaryTreeTrail implements ITrailStorage {


	private IStateBinaryTree[] treeStack;

	private IStateBinaryTree.Node[] nodeStack;
	private int[] opStack;
	private int[] oldValues;
	private int[] stampStack;


	private int[] worldStartLevels;

	private int maxHist;
	private int currentLevel;

	public StoredBinaryTreeTrail(int maxHist, int maxWorld)
	{
        this.currentLevel = 0;
		this.opStack = new int[maxHist];
		this.oldValues = new int[maxHist];
		this.treeStack = new IStateBinaryTree[maxHist];
		this.nodeStack = new IStateBinaryTree.Node[maxHist];
		this.stampStack = new int[maxHist];
		this.worldStartLevels = new int[maxWorld];
		this.maxHist = maxHist;
	}
	


	public void stack(IStateBinaryTree b, IStateBinaryTree.Node n, int operation)
	{
		treeStack[currentLevel] = b;
		nodeStack[currentLevel] = n;
		opStack[currentLevel] = operation;
		switch(operation)
		{
		case IStateBinaryTree.INF : oldValues[currentLevel] = n.inf ; stampStack[currentLevel] = n.infStamp; break ;
		case IStateBinaryTree.SUP : oldValues[currentLevel] = n.sup ; stampStack[currentLevel] = n.supStamp; break ;
		default : break;
		}

		currentLevel++;




		if (currentLevel == maxHist){
			resizeUpdateCapacity();
        }

	}

	private void resizeUpdateCapacity() {
		final int newCapacity = ((maxHist * 3) / 2);
		final IStateBinaryTree[] tmp1 = new IStateBinaryTree[newCapacity];
		System.arraycopy(treeStack, 0, tmp1, 0, treeStack.length);
		treeStack = tmp1;

		// then, copy the stack of former values
		final IStateBinaryTree.Node[] tmp2 = new IStateBinaryTree.Node[newCapacity];
		System.arraycopy(nodeStack, 0, tmp2, 0, nodeStack.length);
		nodeStack = tmp2;


		final int[] tmp3 = new int[newCapacity];
		System.arraycopy(opStack, 0, tmp3, 0, opStack.length);
		opStack = tmp3;

		// then, copy the stack of world stamps
		final int[] tmp4 = new int[newCapacity];
		System.arraycopy(oldValues, 0, tmp4, 0, oldValues.length);
		oldValues = tmp4;

		final int[] tmp5 = new int[newCapacity];
		System.arraycopy(stampStack, 0, tmp5, 0, stampStack.length);
		stampStack = tmp5;

		// last update the capacity
		maxHist = newCapacity;
	}


	public void worldPush(int worldIndex) {
		worldStartLevels[worldIndex] = currentLevel;
	}

	public void worldPop(int worldIndex) {

		while (currentLevel > worldStartLevels[worldIndex]) {
			currentLevel--;

			final IStateBinaryTree b = treeStack[currentLevel];



			final IStateBinaryTree.Node n = nodeStack[currentLevel];
			int operation = opStack[currentLevel];

			switch (operation)
			{
			case IStateBinaryTree.INF : n._setInf(oldValues[currentLevel],stampStack[currentLevel]); break;
			case IStateBinaryTree.SUP : n._setSup(oldValues[currentLevel],stampStack[currentLevel]); break;
			case IStateBinaryTree.ADD :
				b.remove(n,false);
				break;
			case IStateBinaryTree.REM : n.leftNode = null ;
			n.rightNode = null ;
			n.father = null ;
			b.add(n,false); break;
			}
		}
	}

	public void worldCommit() {
		//TODO
	}

	public int getSize() {
		return currentLevel;
	}


	public void resizeWorldCapacity(int newWorldCapacity) {
		final int[] tmp = new int[newWorldCapacity];
		System.arraycopy(worldStartLevels, 0, tmp, 0, worldStartLevels.length);
		worldStartLevels = tmp;
	}
}