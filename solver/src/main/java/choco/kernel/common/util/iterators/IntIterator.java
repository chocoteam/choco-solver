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


package choco.kernel.common.util.iterators;

import java.io.Serializable;


/**
 * An interface for collections over native integers.
 * (among other things, this is useful for iterating domains)
 * Note that it does not extend Iterator, as we iterate over native int,
 * and not Object.
 *
 * DEVELOPPERS :
 * This class must be protected:
 * 
 * Use directly DisposableIntIterator!!
 *
 */

public interface IntIterator extends Serializable {
  /**
   * Returns <tt>true</tt> if the iteration has more elements. (In other
   * words, returns <tt>true</tt> if <tt>next</tt> would return an element
   * rather than throwing an exception.)
   *
   * @return <tt>true</tt> if the getIterator has more elements.
   */
  boolean hasNext();

  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @throws java.util.NoSuchElementException
   *          iteration has no more elements.
   */
  int next();

  /**
   * Removes from the underlying collection the last element returned by the
   * getIterator (optional operation).  This method can be called only once per
   * call to <tt>next</tt>.  The behavior of an getIterator is unspecified if
   * the underlying collection is modified while the iteration is in
   * progress in any way other than by calling this method.
   *
   * @throws java.lang.UnsupportedOperationException
   *          if the <tt>remove</tt>
   *          operation is not supported by this Iterator.
   * @throws java.lang.IllegalStateException
   *          if the <tt>next</tt> method has not
   *          yet been called, or the <tt>remove</tt> method has already
   *          been called after the last call to the <tt>next</tt>
   *          method.
   */
  void remove();
}
