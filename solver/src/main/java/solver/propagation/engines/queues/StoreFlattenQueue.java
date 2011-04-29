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
 * @since 29 sept. 2010
 */
public final class StoreFlattenQueue implements ITwoStateStoreQueue<Constraint> {

    private Constraint[] elements;

    private final IStateInt firstEntailed;

    @SuppressWarnings({"unchecked"})
    public StoreFlattenQueue(choco.kernel.memory.IEnvironment env) {
        this.elements = new Constraint[0];
        this.firstEntailed = env.makeInt();
    }

    @Override
    public void add(Constraint constraint) {
        Constraint[] tmp = elements;
        elements = new Constraint[tmp.length + 1];
        System.arraycopy(tmp, 0, elements, 0, tmp.length);

        elements[tmp.length] = constraint;
        firstEntailed.add(1);
    }

    @Override
    public void moveRight(Constraint c) {
        int last = firstEntailed.get();
//        get the index of the constraint within the list of constraints
        int i = last - 1;
        for (; i >= 0 && c != elements[i]; i--) {
        }
//        move constraint at the right side of firstEntailed
        Constraint tmp = elements[--last];
        elements[last] = c;
        elements[i] = tmp;

        firstEntailed.add(-1);
    }

    @Override
    public void forEach(Procedure<Constraint> proc) throws ContradictionException {
        int last = firstEntailed.get();
        for (int i = 0; i < last; i++) {
            proc.execute(elements[i]);
        }
    }

    @Override
    public void forVeryEach(Procedure<Constraint> proc) throws ContradictionException {
        int last = firstEntailed.get();
        int cIdx = 0;
        while (cIdx < last) {
            Constraint c = elements[cIdx];
            if (c.isActive()) {
                proc.execute(c);
                if (!c.isActive()) {
                    last = firstEntailed.get();
                    cIdx--;
                }
            }
            cIdx++;
        }
    }

    @Override
    public int size() {
        return elements.length;
    }

    @Override
    public int cardinality() {
        return firstEntailed.get();
    }
}
