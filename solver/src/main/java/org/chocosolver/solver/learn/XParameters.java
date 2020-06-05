/**
 * Copyright (c) 1999-2020, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * <p>
 * * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * * Neither the name of the Ecole des Mines de Nantes nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
     * When set to <i>true</i>, print the learnt clause
     */
    public static boolean PRINT_CLAUSE = false;
    /**
     * When set to <i>true</i>, store signed clauses in a unique structure driven by an interval tree.
     * Otherwise, each clause is turned into a constraint.
     */
    public static boolean INTERVAL_TREE = true;
}
