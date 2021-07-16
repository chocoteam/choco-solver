/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.iterators;

/**
 * An interface to declare values iterator.
 * <p/>
 * A value iterator can be iterated in 2 ways: bottom-up (from lower bound to upper bound) <br/>
 * and top-down (from upper bound to lower bound).<br/>
 * To iterate in bottom-up way, first call bottomUpInit(), then hasNext() and next().<br/>
 * To iterate in bottom-up way, first call topDownInit(), then hasPrevious() and previous().<br/>
 * <br/>
 * Once a way is selected, using the wrong methods can lead to unexpected behaviour.
 * <p/>
 * <pre>
 * ValueIterator vit = ...;
 * vit.bottomUpInit();
 * while(vit.hasNext()){
 *    int v = vit.next();
 *    // operate on value v here
 * }</pre>
 * OR
 * <pre>
 * ValueIterator vit = ...;
 * vit.topDownInit();
 * while(vit.hasPrevious()){
 *    int v = vit.previous();
 *    // operate on value v here
 * }</pre>
 *
 * @author Charles Prud'homme
 * @since 05/10/11
 */
public interface ValueIterator {

    /**
     * Prepare iteration from smallest value to highest value (using {@link #hasNext()} / {@link #next()})
     * <pre>
     * ValueIterator vit = ...;
     * vit.bottomUpInit();
     * while(vit.hasNext()){
     *    int v = vit.next();
     *    // operate on value v here
     * }</pre>
     * OR
     * <pre>
     */
    void bottomUpInit();

    /**
     * Prepare iteration from highest value to smallest value (using {@link #hasPrevious()} / {@link #previous()})
     * <pre>
     * ValueIterator vit = ...;
     * vit.topDownInit();
     * while(vit.hasPrevious()){
     *    int v = vit.previous();
     *    // operate on value v here
     * }</pre>
     */
    void topDownInit();

    /**
     * Returns <tt>true</tt> if the iteration has more values. (In other
     * words, returns <tt>true</tt> if <tt>next</tt> would return valid value.)
     * <pre>
     * ValueIterator vit = ...;
     * vit.bottomUpInit();
     * while(vit.hasNext()){
     *    int v = vit.next();
     *    // operate on value v here
     * }</pre>
     * OR
     * <pre>
     *
     * @beware incompatible with {@link #hasPrevious()}
     *
     * @return <tt>true</tt> if the getIterator has more values.
     */
    boolean hasNext();

    /**
     * Returns <tt>true</tt> if the iteration has more ranges. (In other
     * words, returns <tt>true</tt> if <tt>previous</tt> would return a valid value.)
     * <pre>
     * ValueIterator vit = ...;
     * vit.topDownInit();
     * while(vit.hasPrevious()){
     *    int v = vit.previous();
     *    // operate on value v here
     * }</pre>
     *
     * @beware incompatible with {@link #hasNext()}
     *
     * @return <tt>true</tt> if the getIterator has more values.
     */
    boolean hasPrevious();

    /**
     * Compute and return the next value.
     * <pre>
     * ValueIterator vit = ...;
     * vit.bottomUpInit();
     * while(vit.hasNext()){
     *    int v = vit.next();
     *    // operate on value v here
     * }</pre>
     * OR
     * <pre>
     *
     * @beware incompatible with {@link #previous()}
     *
     * @return the next element in the iteration.
     */
    int next();

    /**
     * Compute and return the previous value.
     * <pre>
     * ValueIterator vit = ...;
     * vit.topDownInit();
     * while(vit.hasPrevious()){
     *    int v = vit.previous();
     *    // operate on value v here
     * }</pre>
     *
     * @beware incompatible with {@link #next()}
     *
     * @return the previous element in the iteration.
     */
    int previous();
}
