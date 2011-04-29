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

package choco.kernel.memory;

import java.io.Serializable;

/**
 * Describes an integer with states (describing some history of the data
 * structure).
 */
public interface IStateInt extends Serializable {
  /**
   * Value for an unknown integer.
   */

  int UNKNOWN_INT = Integer.MAX_VALUE;


  /**
   * Minimum value an integer can be equal to.
   */

  int MININT = Integer.MIN_VALUE;


  /**
   * Maximum value an integer can be equal to.
   */

  int MAXINT = Integer.MAX_VALUE - 1;


  /**
   * Returns the current value according to the current world.
   * @return The current value of the storable variable.
   */

  int get();



  /**
   * Modifies the value and stores if needed the former value on the
   * trailing stack.
   * @param y the new value of the variable.
   */
  void set(int y);

  /**
   * Modifying a StoredInt by an increment.
   * @param delta the value to add to the current value.
   */
  void add(int delta);

  /**
   * Retrieving the environment.
   * @return the environment associated to this variable (the object
   * responsible to manage worlds and storable variables).
   */

  IEnvironment getEnvironment();

}

