/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;

import org.chocosolver.memory.structure.BasicIndexedBipartiteSet;
import org.chocosolver.memory.structure.IOperation;




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
    void save(IOperation operation);

    /**
     * Return the current time stamp.
     * It differs from world index since it never decrements.
     * @return the timestamp
     */
    int getTimeStamp();
}
