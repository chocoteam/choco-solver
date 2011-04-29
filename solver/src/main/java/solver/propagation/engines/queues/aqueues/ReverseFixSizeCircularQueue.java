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

package solver.propagation.engines.queues.aqueues;

/**
 * A fix sized circular queue optimized for removing first and last elements.
 * Some few (essential regarding the usage) methods are implemented.
 * <br/>
 * Be aware of the size computation: the modulo operation is not efficient in java.
 * On the other hand, the modulo of powers of 2 can alternatively be expressed as a bitwise AND operation:
 * <br/>
 * x % 2n == x & (2n - 1)
 * <br/>
 * That is why the size of the data is automatically set to the closest greater powers of 2 value.
 *
 * @author Charles Prud'homme
 * @since 29 sept. 2010
 */
public class ReverseFixSizeCircularQueue<E> extends FixSizeCircularQueue<E> {
    @SuppressWarnings({"unchecked"})
    public ReverseFixSizeCircularQueue(int size) {
        super(size);
    }

    // The convert() method takes a logical index (as if head was
    // always 0) and calculates the index within elementData

    private int convert(int index, int base) {
        return (index + base) & (capacity - 1);
    }

    /**
     * {@inheritDoc}
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     */
    public E pop() {
        int pos = convert(tail, head);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        E tmp = elementData[pos];
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        if (pos == head) {
            head = convert(1, head);
        }
        size--;
        return tmp;
    }

    /**
     * {@inheritDoc}
     * This method is the main reason we re-wrote the class.
     * It is optimized for removing first and last elements
     * but also allows you to remove in the middle of the list.
     */
    public E remove() {
        int pos = convert(tail, head);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        E tmp = elementData[pos];
        elementData[pos] = null; // Let gc do its work
        // optimized for FIFO access, i.e. adding to back and
        // removing from front
        if (pos == head) {
            head = convert(1, head);
        }
        size--;
        return tmp;
    }



}
