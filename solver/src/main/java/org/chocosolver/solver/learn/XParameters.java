/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.learn;

/**
 * This class exposes paramters that sets explanations behavior or allow fine debugging trace.
 * <br/>
 * @author Charles Prud'homme
 * @since 29/05/2020
 */
public class XParameters {
    /**
     * <ul>
     *     <li>
     *         0: merge is disabled
     *     </li>
     *     <li>
     *         1: merge two consecutive entries with the same variable and cause
     *     </li>
     * </ul>
     */
    @SuppressWarnings("WeakerAccess")
    public static final int MERGE_CONDITIONS = 0;
    /**
     * Set to false when skip assertion that
     * no left branch are backtracked to
     */
    public static boolean ASSERT_NO_LEFT_BRANCH = true;
    /**
     * Set to true to force all cause to use default explanations
     */
    public static boolean DEFAULT_X = false;
    /**
     * FOR DEBUGGING PURPOSE ONLY.
     * Set to true to output proofs
     */
    public static boolean PROOF = false;
    /**
     * FOR DEBUGGING PURPOSE ONLY.
     * Set to true to output proofs with details
     */
    public static boolean FINE_PROOF = PROOF;
    /**
     * FOR DEBUGGING PURPOSE ONLY.
     * Check implication graph integrity, ie nodes are correctly connected to each other
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean DEBUG_INTEGRITY = false;
    /**
     * When set to <i>true</i>:
     * Eliminate views from learning clause, replaced by the underlying variable.
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean ELIMINATE_VIEWS = true;
    /**
     * When set to <i>true</i>, assert unit propagation of the latest learnt clause
     */
    public static boolean ASSERT_UNIT_PROP = true;
    /**
     * When set to <i>true</i>, assert that the asserting level of the learnt clause is good
     */
    public static boolean ASSERT_ASSERTING_LEVEL = false;
    /**
     * When set to <i>true</i>, print the learnt clause
     */
    public static boolean PRINT_CLAUSE = false;
    /**
     * When set to <i>true</i>, store signed clauses in a unique structure driven by an interval tree.
     * Otherwise, each clause is turned into a constraint.
     */
    public static boolean INTERVAL_TREE = true;
    /**
     * Allow locking {@link org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet}
     */
    public static boolean ALLOW_LOCK = true;
}
