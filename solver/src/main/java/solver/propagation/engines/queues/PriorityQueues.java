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

package solver.propagation.engines.queues;

import solver.propagation.IQueable;
import solver.propagation.engines.queues.aqueues.FixSizeCircularQueue;
import solver.propagation.engines.queues.aqueues.LinkedList;
import solver.propagation.engines.queues.aqueues.RandomLinkedList;
import solver.propagation.engines.queues.aqueues.ReverseFixSizeCircularQueue;

import java.util.NoSuchElementException;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 11 aožt 2010
 */
public class PriorityQueues<E extends IQueable> extends APriorityQueues {

    private final AQueue<E>[] elements;

    public static int _DEFAULT = 0;

    private int active;

    @SuppressWarnings({"unchecked"})
    public PriorityQueues(int[] nbElts) {
        this.elements = new AQueue[_NB_PRIORITY];
        for (int i = 0; i < _NB_PRIORITY; i++) {
            this.elements[i] = queue(nbElts[i]);
        }
    }

    @SuppressWarnings({"unchecked"})
    public PriorityQueues(int nbElts) {
        this.elements = new AQueue[_NB_PRIORITY];
        for (int i = 0; i < _NB_PRIORITY; i++) {
            this.elements[i] = queue(nbElts);
        }
    }

    private static <E> AQueue<E> queue(int nbElts) {
        switch (_DEFAULT) {
            case 1:
                new LinkedList<E>(nbElts);
            case 2:
                new ReverseFixSizeCircularQueue<E>(nbElts);
            case 3:
                new RandomLinkedList<E>(nbElts);
            case 0:
            default:
                return new FixSizeCircularQueue<E>(nbElts);
        }
    }


    public boolean add(E e, int priority) {
        if (!e.enqueued()) {
            elements[priority].add(e);
            active |= (1 << priority);
            e.enqueue();
            return true;
        }
        return false;
    }

    public E pop() {
        int i = index[active];
        if (active == 0) throw new NoSuchElementException();

        E tmp = elements[i].pop();
        tmp.deque();
        if (elements[i].isEmpty()) {
            active -= 1 << i;
        }
        return tmp;
    }

    public void remove(E e) {
        for (int i = 0; i < _NB_PRIORITY; i++) {
            elements[i].remove(e);
        }
    }

    public boolean isEmpty() {
        return active == 0;
    }


    public void clear() {
        while (active > 0) {
            pop();
        }
    }

    public int size() {
        int c = 0;
        int i = 0;
        for (; i < _NB_PRIORITY; i++) {
            c += elements[i].size();
        }
        return c;
    }

}
