/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.memory;

import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.memory.structure.Operation;




/**
 * An interface to ease declaration of backtrackable objects (mostly primitives).
 *
 * @author Charles Prud'homme, Hadrien Cambazard, Guillaume Rochart
 */
public interface IEnvironment  {

    /**
     * Returns the world number.
     *
     * @return current world index
     */

    int getWorldIndex();

    /**
     * Starts a new branch in the search tree.
     */

    void worldPush();

    /**
     * Backtracks to the previous choice point in the search tree.
     */
    void worldPop();

    /**
     * Backtracks to the <code>w</code> previous choice point in the tree search
     *
     * @param w world index to pop to
     */
    void worldPopUntil(int w);

    /**
     * Comitting the current world: merging it with the previous one.
     * <p>
     * Not used yet.
     */
    void worldCommit();

    /**
     * Force to build fake history when a stored object on a particular condition.
     * The default condition is {@link ICondition#FALSE}.
     * @param condition to satisfy to build fake history
     */
    void buildFakeHistoryOn(ICondition condition);

    /**
     * @return true if building fake history is needed (the condition is satisfied).
     */
    boolean fakeHistoryNeeded();

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Factory pattern: new IStateBool objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable boolean
     * @return Boolean object created by the environment
     */

    IStateBool makeBool(boolean initialValue);

    /**
     * Factory pattern: new IStateInt objects are created by the environment
     * (no initial value is assigned to the backtrackable search)
     *
     * @return new IStateInt computed by the environment
     */

    IStateInt makeInt();

    /**
     * Factory pattern: new IStateInt objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable integer
     * @return new IStateInt computed by the environment
     */

    IStateInt makeInt(int initialValue);

    /**
     * Factory pattern: new StoredFloat objects are created by the environment
     * (no initial value is assigned to the backtrackable search)
     * @return new IStateDouble computed by the environment
     */

    IStateDouble makeFloat();

    /**
     * Factory pattern: new StoredFloat objects are created by the environment
     *
     * @param initialValue the initial value of the backtrackable search
     * @return new IStateDouble computed by the environment
     */
    IStateDouble makeFloat(double initialValue);

    /**
     * Factory pattern: new backtrackable long attached to this environment.
     * @return a backtrackable long
     */
    IStateLong makeLong();

    /**
     * Factory pattern: new backtrackable long attached to this environment.
     * @param init its initial value
     * @return a backtrackable long
     */
    IStateLong makeLong(long init);

    /**
     * Factory pattern: new IStateBitSet objects are created by the environment
     *
     * @param size initial size of the IStateBitSet
     * @return IStateBitSet
     */
    IStateBitSet makeBitSet(int size);

    /**
     * Factory pattern: new IStateIntVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return a backtrackable vector
     */
    IStateIntVector makeIntVector(int size, int initialValue);


    /**
     * Factory pattern: new IStateDoubleVector objects are created by the environment
     *
     * @param size         the number of entries in the vector
     * @param initialValue the common initial value for all entries (backtrackable integers)
     * @return IStateDoubleVector
     */
    IStateDoubleVector makeDoubleVector(int size, double initialValue);

    /**
     * Factory pattern : shared IndexedBipartiteSet object is return by the environment
     *
     * @return IndexedBipartiteSet
     */
    BasicIndexedBipartiteSet getSharedBipartiteSetForBooleanVars();

    /**
     * Save this operation onto the stack of operations to undo on backtrack.
     * @param operation operation to undo
     */
    void save(Operation operation);

    /**
     * Return the current time stamp.
     * It differs from world index since it never decrements.
     * @return the timestamp
     */
    int getTimeStamp();
}
