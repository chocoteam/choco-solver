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

package solver.propagation.engines.queues;

import choco.kernel.common.util.procedure.Procedure;
import choco.kernel.memory.IStateInt;
import solver.constraints.Constraint;
import solver.exception.ContradictionException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11 aožt 2010
 */
public final class StorePriorityQueues extends APriorityQueues implements ITwoStateStoreQueue<Constraint> {

    private final Constraint[][] elements;

    private final IStateInt[] firstEntailed;

    private int active;


    @SuppressWarnings({"unchecked"})
    public StorePriorityQueues(choco.kernel.memory.IEnvironment env) {
        this.elements = new Constraint[_NB_PRIORITY][];
        this.firstEntailed = new choco.kernel.memory.IStateInt[_NB_PRIORITY];
        for (int i = 0; i < _NB_PRIORITY; i++) {
            this.elements[i] = new Constraint[0];
            this.firstEntailed[i] = env.makeInt();
        }
    }

    /**
     * {@inheritDoc}
     */
    public void add(Constraint e) {
        int p = e.getPriority();
        Constraint[] tmp = elements[p];
        elements[p] = new Constraint[tmp.length + 1];
        System.arraycopy(tmp, 0, elements[p], 0, tmp.length);

        elements[p][tmp.length] = e;
        firstEntailed[p].add(1);
        active |= (1 << p);
    }

    /**
     * {@inheritDoc}
     */
    public void moveRight(Constraint c) {
        int p = c.getPriority();
        int last = firstEntailed[p].get();
//        get the index of the constraint within the list of constraints
        int i = last - 1;
        for (; i >= 0 && c != elements[p][i]; i--) {}
//        move constraint at the right side of firstEntailed
        Constraint tmp = elements[p][--last];
        elements[p][last] = c;
        elements[p][i] = tmp;

        firstEntailed[p].add(-1);
    }

    /**
     * {@inheritDoc}
     */
    public void forEach(Procedure<Constraint> proc) throws ContradictionException {
        int _active = active;
        int p = index[_active];
        for (; p >= 0; p = index[_active]) {
            int last = firstEntailed[p].get();
            for (int i = 0; i < last; i++) {
                proc.execute(elements[p][i]);
            }
            _active -= 1 << p;
        }
    }

    /**
     * {@inheritDoc}
     */
    public void forVeryEach(Procedure<Constraint> proc) throws ContradictionException {
        int _active = active;
        int p = index[_active];
        for (; p >= 0; p = index[_active]) {
            int last = firstEntailed[p].get();
            int cIdx = 0;
            while (cIdx < last) {
                Constraint c = elements[p][cIdx];
                proc.execute(c);
                if (!c.isActive()) {
                    last = firstEntailed[p].get();
                    cIdx--;
                }
                cIdx++;
            }
            _active -= 1 << p;
        }
    }

    /**
     * {@inheritDoc}
     */
    public int cardinality() {
        int size = 0;
        int _active = active;
        int p = index[_active];
        for (; p >= 0; p = index[_active]) {
            size += firstEntailed[p].get();
            _active -= 1 << p;
        }
        return size;
    }

    public int size() {
        int size = 0;
        int _active = active;
        int p = index[_active];
        for (; p >= 0; p = index[_active]) {
            size += elements[p].length;
            _active -= 1 << p;
        }
        return size;
    }
}
