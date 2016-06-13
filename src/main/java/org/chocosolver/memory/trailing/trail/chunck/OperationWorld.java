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
package org.chocosolver.memory.trailing.trail.chunck;


import org.chocosolver.memory.structure.IOperation;

/**
 * @author Fabien Hermenier
 * @author Charles Prud'homme
 * @since 31/05/2016
 */
public class OperationWorld implements World{


    /**
     * Stack of backtrackable search variables.
     */
    private IOperation[] variableStack;

    private int now;

    private double loadfactor;

    public OperationWorld(int defaultSize, double loadfactor) {
        now = 0;
        this.loadfactor = loadfactor;
        variableStack = new IOperation[defaultSize];
    }

    /**
     * Reacts when a StoredInt is modified: push the former value & timestamp
     * on the stacks.
     */
    public void savePreviousState(IOperation v) {
        variableStack[now] = v;
        now++;
        if (now == variableStack.length) {
            resizeUpdateCapacity();
        }
    }

    @Override
    public void revert() {
        IOperation v;
        for (int i = now - 1; i >= 0; i--) {
            v = variableStack[i];
            v.undo();
        }
    }

    private void resizeUpdateCapacity() {
        final int newCapacity = (int)(variableStack.length * loadfactor);
        final IOperation[] tmp1 = new IOperation[newCapacity];
        System.arraycopy(variableStack, 0, tmp1, 0, variableStack.length);
        variableStack = tmp1;
    }

    public void clear() {
        now = 0;
    }

    @Override
    public int allocated() {
        return variableStack == null ? 0 : variableStack.length;
    }

    @Override
    public int used() {
        return now;
    }
}
