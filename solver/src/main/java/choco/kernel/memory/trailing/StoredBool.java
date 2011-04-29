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

package choco.kernel.memory.trailing;

import choco.kernel.memory.IStateBool;
import choco.kernel.memory.trailing.trail.StoredBoolTrail;


/**
 * A class implementing backtrackable booleans.
 */
public final class StoredBool extends AbstractStoredObject implements IStateBool {
  
  
  /**
   * Current value of the search.
   */
  private boolean currentValue;

  protected final StoredBoolTrail myTrail;

  /**
   * Constructs a stored search with an initial value.
   * Note: this constructor should not be used directly: one should instead
   * use the IEnvironment factory
   */

  public StoredBool(final EnvironmentTrailing env, final boolean b) {
	  super(env);
      myTrail = env.getBoolTrail();
	  currentValue = b;
  }


  /**
   * Returns the current value.
   */
  public boolean get() {
    return currentValue;
  }

  /**
   * Modifies the value and stores if needed the former value on the
   * trailing stack.
   */

  public void set(final boolean b) {
    if (b != currentValue) {
      final int wi = environment.getWorldIndex();
      if (this.worldStamp < wi) {
        myTrail.savePreviousState(this, currentValue, worldStamp);
        worldStamp = wi;
      }
      currentValue = b;
    }
  }

  /**
   * Modifies the value without storing the former value on the trailing stack.
   *
   * @param b      the new value
   * @param wstamp the stamp of the world in which the update is performed
   */

  public void _set(final boolean b, final int wstamp) {
    currentValue = b;
    worldStamp = wstamp;
  }


  @Override
public String toString() {
    return String.valueOf(currentValue);
  }

}

