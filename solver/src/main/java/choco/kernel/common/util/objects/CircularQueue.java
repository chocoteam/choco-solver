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

package choco.kernel.common.util.objects;

/**
 * User : cprudhom
 * Mail : cprudhom(a)emn.fr
 * Date : 15 févr. 2010
 * Since : Choco 2.1.1
 *
 * <a href=http://www.javaspecialists.eu/archive/Issue027.html>javaspecialist</a>
 */
public class CircularQueue<E>{

    /**
     * The number of times this list has been <i>structurally modified</i>.
     * Structural modifications are those that change the size of the
     * list, or otherwise perturb it in such a fashion that iterations in
     * progress may yield incorrect results.
     * <p/>
     * <p>This field is used by the iterator and list iterator implementation
     * returned by the {@code iterator} and {@code listIterator} methods.
     * If the value of this field changes unexpectedly, the iterator (or list
     * iterator) will throw a {@code ConcurrentModificationException} in
     * response to the {@code next}, {@code remove}, {@code previous},
     * {@code set} or {@code add} operations.  This provides
     * <i>fail-fast</i> behavior, rather than non-deterministic behavior in
     * the face of concurrent modification during iteration.
     * <p/>
     * <p><b>Use of this field by subclasses is optional.</b> If a subclass
     * wishes to provide fail-fast iterators (and list iterators), then it
     * merely has to increment this field in its {@code add(int,E)} and
     * {@code remove(int)} methods (and any other methods that it overrides
     * that result in structural modifications to the list).  A single call to
     * {@code add(int,E)} or {@code remove(int)} must add no more than
     * one to this field, or the iterators (and list iterators) will throw
     * bogus {@code ConcurrentModificationExceptions}.  If an implementation
     * does not wish to provide fail-fast iterators, this field may be
     * ignored.
     */
    protected transient int modCount = 0;

    private E[] elementData;
    // head points to the first logical element in the array, and
    // tail points to the element following the last.  This means
    // that the list is empty when head == tail.  It also means
    // that the elementData array has to have an extra space in it.
    private int head = 0, tail = 0;
    // Strictly speaking, we don't need to keep a handle to size,
    // as it can be calculated programmatically, but keeping it
    // makes the algorithms faster.
    private int size = 0;

    public CircularQueue() {
        this(10);
    }

    @SuppressWarnings({"unchecked"})
    public CircularQueue(int size) {
        elementData = (E[])new Object[size];
    }

//    public CircularQueue(Collection c) {
//        // we also have to set the size - bug discovered by
//        // Jos van der Til from the Netherlands
//        size = tail = c.size();
//        elementData = new MyConstraintEvent[c.size()];
//        c.toArray(elementData);
//    }

    // The convert() method takes a logical index (as if head was
    // always 0) and calculates the index within elementData
    private int convert(int index) {
        return (index + head) % elementData.length;
    }

    public boolean isEmpty() {
        return head == tail; // or size == 0
    }

    // We use this method to ensure that the capacity of the
    // list will suffice for the number of elements we want to
    // insert.  If it is too small, we make a new, bigger array
    // and copy the old elements in.
    @SuppressWarnings({"unchecked"})
    public void ensureCapacity(int minCapacity) {
        int oldCapacity = elementData.length;
        if (minCapacity > oldCapacity) {
            int newCapacity = (oldCapacity * 3) / 2 + 1;
            if (newCapacity < minCapacity)
                newCapacity = minCapacity;
            Object newData[] = new Object[newCapacity];
            toArray(newData);
            tail = size;
            head = 0;
            elementData = (E[])newData;
        }
    }

    public int size() {
        // the size can also be worked out each time as:
        // (tail + elementData.length - head) % elementData.length
        return size;
    }

    public boolean contains(Object elem) {
        return indexOf(elem) >= 0;
    }

    public int indexOf(Object elem) {
        if (elem == null) {
            for (int i = 0; i < size; i++)
                if (elementData[convert(i)] == null)
                    return i;
        } else {
            for (int i = 0; i < size; i++)
                if (elem.equals(elementData[convert(i)]))
                    return i;
        }
        return -1;
    }

    public int lastIndexOf(Object elem) {
        if (elem == null) {
            for (int i = size - 1; i >= 0; i--)
                if (elementData[convert(i)] == null)
                    return i;
        } else {
            for (int i = size - 1; i >= 0; i--)
                if (elem.equals(elementData[convert(i)]))
                    return i;
        }
        return -1;
    }

//    public Object[] toArray() {
//        return toArray(new Object[size]);
//    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public <T> T[] toArray(T a[]) {
        if (a.length < size)
            a = (T[]) java.lang.reflect.Array.newInstance(
                    a.getClass().getComponentType(), size);
        if (head < tail) {
            System.arraycopy(elementData, head, a, 0, tail - head);
        } else {
            System.arraycopy(elementData, head, a, 0,
                    elementData.length - head);
            System.arraycopy(elementData, 0, a, elementData.length - head,
                    tail);
        }
        if (a.length > size)
            a[size] = null;
        return a;
    }

    private void rangeCheck(int index) {
        if (index >= size || index < 0)
            throw new IndexOutOfBoundsException(
                    "Index: " + index + ", Size: " + size);
    }

    public E get(int index) {
        rangeCheck(index);
        return elementData[convert(index)];
    }

    //    public MyConstraintEvent set(int index, MyConstraintEvent element) {
//        modCount++;
//        rangeCheck(index);
//        MyConstraintEvent oldValue = elementData[convert(index)];
//        elementData[convert(index)] = element;
//        return oldValue;
//    }

    public boolean add(E e) {
        modCount++;
        // We have to have at least one empty space
        ensureCapacity(size + 1 + 1);
        elementData[tail] = e;
        tail = (tail + 1) % elementData.length;
        size++;
        return true;
    }

    // This method is the main reason we re-wrote the class.
    // It is optimized for removing first and last elements
    // but also allows you to remove in the middle of the list.
    public E pop() {
        modCount++;
        rangeCheck(0);
        int pos = convert(0);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        try {
            return elementData[pos];
        } finally {
            elementData[pos] = null; // Let gc do its work
            // optimized for FIFO access, i.e. adding to back and
            // removing from front
            if (pos == head) {
                head = (head + 1) % elementData.length;
            }/* else if (pos == tail) {
                tail = (tail - 1 + elementData.length) % elementData.length;
            } else {
                if (pos > head && pos > tail) { // tail/head/pos
                    System.arraycopy(elementData, head, elementData, head + 1,
                            pos - head);
                    head = (head + 1) % elementData.length;
                } else {
                    System.arraycopy(elementData, pos + 1, elementData, pos,
                            tail - pos - 1);
                    tail = (tail - 1 + elementData.length) % elementData.length;
                }
            }*/
            size--;
        }

    }

    // This method is the main reason we re-wrote the class.
    // It is optimized for removing first and last elements
    // but also allows you to remove in the middle of the list.
    public E poll() {
        modCount++;
        rangeCheck(0);
        int pos = convert(0);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        try {
            return elementData[pos];
        } finally {
            elementData[pos] = null; // Let gc do its work
            // optimized for FIFO access, i.e. adding to back and
            // removing from front
            if (pos == head) {
                head = (head + 1) % elementData.length;
            } else if (pos == tail) {
                tail = (tail - 1 + elementData.length) % elementData.length;
            } else {
                if (pos > head && pos > tail) { // tail/head/pos
                    System.arraycopy(elementData, head, elementData, head + 1,
                            pos - head);
                    head = (head + 1) % elementData.length;
                } else {
                    System.arraycopy(elementData, pos + 1, elementData, pos,
                            tail - pos - 1);
                    tail = (tail - 1 + elementData.length) % elementData.length;
                }
            }
            size--;
        }

    }

    // This method is the main reason we re-wrote the class.
    // It is optimized for removing first and last elements
    // but also allows you to remove in the middle of the list.
    public E remove(int index) {
        modCount++;
        rangeCheck(index);
        int pos = convert(index);
        // an interesting application of try/finally is to avoid
        // having to use local variables
        try {
          return elementData[pos];
        } finally {
            elementData[pos] = null; // Let gc do its work
          // optimized for FIFO access, i.e. adding to back and
          // removing from front
          if (pos == head) {
            head = (head+1)%elementData.length;
          } else if (pos == tail) {
            tail = (tail-1+elementData.length)%elementData.length;
          } else {
            if (pos > head && pos > tail) { // tail/head/pos
              System.arraycopy(elementData, head, elementData, head+1,
                pos-head);
              head = (head+1)%elementData.length;
            } else {
              System.arraycopy(elementData, pos+1, elementData, pos,
                tail-pos-1);
              tail = (tail-1+elementData.length)%elementData.length;
            }
          }
          size--;
        }
      }

    // This method is the main reason we re-wrote the class.
    // It is optimized for removing first and last elements
    // but also allows you to remove in the middle of the list.
    public void remove(E elt) {
        remove(indexOf(elt));
      }


    public void clear() {
        modCount++;
        // Let gc do its work
        for (int i = head; i != tail; i = (i + 1) % elementData.length)
            elementData[i] = null;
        head = tail = size = 0;
    }

}
