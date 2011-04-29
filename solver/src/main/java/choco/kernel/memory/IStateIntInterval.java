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

public interface IStateIntInterval extends Serializable {

    /**
     * Returns the current lower bound according to the current world
     * @return The current lower bound of the storable variable.
     */
    int getInf();

    /**
     * Modifies the inf value and stores if needed the former value on the
     * trailing stack.
     * @param y the new value of the inf variable.
     */
    void setInf(int y);

  /**
   * Modifying a StoredIntInterval inf by an increment.
   * @param delta the inf value to add to the current value.
   */
  void addInf(int delta);

    /**
     * Returns the current upper bound according to the current world
     * @return The current upper bound of the storable variable.
     */
    int getSup();

    /**
     * Modifies the sup value and stores if needed the former value on the
     * trailing stack.
     * @param y the new value of the sup variable.
     */
    void setSup(int y);

    /**
   * Modifying a StoredIntInterval sup by an increment.
   * @param delta the sup value to add to the current value.
   */
    void addSup(int delta);


    /**
     * Returns size of the interval
     * @return size of the interval
     */
    int getSize();

  /**
   * Retrieving the environment.
   * @return the environment associated to this variable (the object
   * responsible to manage worlds and storable variables).
   */
    IEnvironment getEnvironment();

    /**
     * Checks wether the stored interval contains x
     * @param x the value to check
     * @return wether x is contained
     */
    boolean contains(int x);

}
