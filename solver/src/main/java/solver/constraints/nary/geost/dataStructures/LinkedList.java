/**
 *  Copyright (c) 1999-2010, Ecole des Mines de Nantes
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

package solver.constraints.nary.geost.dataStructures;

import java.util.NoSuchElementException;

public class LinkedList {
  ListItem head;
 /*
  * creates an empty list
  */
  public LinkedList() {
    head = new ListItem(null);
    head.next = head.previous = head;
  }

 /*
  * remove all elements in the list
  */
  public final synchronized void clear() {
    head.next = head.previous = head;
  }

 /*
  * returns true if this container is empty.
  */
  public final boolean isEmpty() {
    return head.next == head;
  }

 /*
  * insert element after current position
  */
  public final synchronized void insertAfter(Object obj, ListIterator cursor) {
    ListItem newItem = new ListItem(cursor.pos, obj, cursor.pos.next);
    newItem.next.previous = newItem;
    cursor.pos.next = newItem;
  }

 /*
  * insert element before current position
  */
  public final synchronized void insertBefore(Object obj, ListIterator cursor) {
    ListItem newItem = new ListItem(cursor.pos.previous, obj, cursor.pos);
    newItem.previous.next = newItem;
    cursor.pos.previous = newItem;
  }

 /*
  * remove the element at current position
  */
  public final synchronized void remove(ListIterator cursor) {
    if (isEmpty()) {
      throw new IndexOutOfBoundsException("empty list.");
    }
    if (cursor.pos == head) {
      throw new NoSuchElementException("cannot remove the head");
    }
    cursor.pos.previous.next = cursor.pos.next;
    cursor.pos.next.previous = cursor.pos.previous;
  }

 /*
  * Return an iterator positioned at the head.
  */
  public final ListIterator head() {
    return new ListIterator(this, head);
  }

 /*
  * find the first occurrence of the object in a list
  */
  public final synchronized ListIterator find(Object obj) {
    if (isEmpty()) {
      throw new IndexOutOfBoundsException("empty list.");
    }
    ListItem pos = head;
    while (pos.next != head) {  // There are still elements to be inspected
      pos = pos.next;
      if (pos.obj == obj) {
        return new ListIterator(this, pos);
      }
    }
    throw new NoSuchElementException("no such object found");
  }
}