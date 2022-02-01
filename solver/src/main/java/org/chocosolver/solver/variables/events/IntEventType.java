/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.variables.events;

import org.chocosolver.solver.ICause;

/**
 * An enum defining the integer variable event types:
 * <ul>
 * <li>{@link #REMOVE}: value removal event,</li>
 * <li>{@link #INCLOW}: lower bound increase event,</li>
 * <li>{@link #DECUPP}: upper bound decrease event,</li>
 * <li>{@link #BOUND}: lower bound increase and/or upper bound decrease event,</li>
 * <li>{@link #INSTANTIATE}: variable instantiation event </li>
 * </ul>
 * Int event types are used with four different purposes.
 * But first of all, there is a hierarchy between events REMOVE is the smallest one,
 * INCLOW and DECUPP are equivalent and induce REMOVE
 * and INSTANTIATE is the greatest and induced the three lower ones.
 * Those four events aim at reducing the event footprint:
 * for example if one propagator removes values 1,2 and 3 from V={1, 2, 3, 4, 5},
 * it can be seen either by a sequence of 3 REMOVE OR one INCLOW.
 * Both express the same operations, one with 3 items the second with only one
 *
 * Now, the purposes:
 * <ol>
 *     <li>
 *         in {@link org.chocosolver.solver.constraints.Propagator#getPropagationConditions(int i)}.
 *         It helps sorting the propagator of the variable <tt>i</tt> wrt the types of event they can deal with.
 *         Propagators which react to the same type of events are placed in the same "bucket".
 *         Thus, when the variable <tt>i</tt> is modified, only propagators which are able to deduce something from that event are called.
 *         In that case, combinations of events are allowed, such as {{@link #REMOVE}, {@link #INCLOW}}
 *         or {{@link #REMOVE}, {@link #INCLOW}, {@link #DECUPP}, {@link #INSTANTIATE}},
 *         and are stored, for each variable, with the help of bit mask.
 *     </li>
 *     <li>
 *          when the variable is actually modified.
 *          Any propagators can modify its variable, always through one of the provided methods, such as
 *          {@link org.chocosolver.solver.variables.IntVar#instantiateTo(int, ICause)}
 *          or {@link org.chocosolver.solver.variables.IntVar#updateLowerBound(int, ICause)}.
 *          In that case, when the propagation engine is triggered, with only single one event as input: {@link #INSTANTIATE}
 *          or {@link #INCLOW}.
 *          Then, in the propagation engine, the event is stored, possibly merged with not propagates ones, for future execution.
 *     </li>
 *     <li>
 *          when a propagator is selected for propagation, the events its variables have registered are treated sequentially.
 *          Here, if a variable has registered multiple events, the union is given as input to the propagator through
 *          {@link org.chocosolver.solver.constraints.Propagator#propagate(int, int)}.
 *          In this method, it may happen that the events registered are filtered,
 *          calling {@link #isRemove(int)} or {@link #isBound(int)}, to adapt the filtering to the type of events registered.
 *          Note that the conditional tests should consider that events can be merged.
 *     </li>
 *     <li>
 *         for explanation purpose, to adapt the explanation schema to the type of event received.
 *     </li>
 * </ol>
 * If a propagator reacts on value removals, it is executed on any events occurring on the variable(s), since {@link #REMOVE} is the smallest event.
 * Then, if other type of event occurs, it is greater and maybe a specific and/or more efficient algorithm can be used instead.
 * So, we prefer testing whether an event is greater than smaller.
 * More over, using deltas, we can iterate over newly removed values, without paying interest to the type of events registered.
 *
 * <p>
 * When, in your propagator, you declare {@link org.chocosolver.solver.constraints.Propagator#getPropagationConditions(int i)}
 * like this:
 *
 * <pre><code>
 *      public int getPropagationConditions(int vIdx) {
 *          return IntEventType.boundAndInst();
 *      }
 * </code>
 * </pre>
 * your propagator will be informed anytime one if its variables is modified by any events but {@link #REMOVE}
 * (that is {@link #INCLOW} and/or {@link #DECUPP} and/or {@link #INSTANTIATE}).
 * On the contrary, if you defined something like:
 *
 * <pre>
 * <code>
 *      public int getPropagationConditions(int vIdx) {
 *          return IntEventType.all();
 *      }
 * </code>
 * </pre>
 * your propagator will be informed anytime one of its variable is modified by any events.
 * Later, when your propagator is actually executed, the aggregated events mask is given as input
 * (2nd parameter of {@link org.chocosolver.solver.constraints.Propagator#propagate(int i,int m)}),
 * and here you can adapt the algorithm to the events got.
 *
 * Indeed, you can have a merge event made of {@link #DECUPP} and {@link #INCLOW} and {@link #REMOVE} which indicates that:
 * <ol>
 *    <li>the upper bound of the variable i has decreased,</li>
 *    <li>its upper bound has increased and</li>
 *    <li>some values has been removed between old LB and old UB.</li>
 * </ol>
 *
 * <b>IMPORTANT:</b> note that, if a variablev={1,2,3,4,5,6} is chronologically modified like this:
 * <ol>
 *     <li>v \ {2}</li>
 *     <li>v < 6</li>
 *     <li>v > 3</li>
 * </ol>
 * then the first event ( v \ {2}) is subsumed by the third one (v > 3).
 * There is no efficient way to consider the subsumption by the propagation engine.
 *
 * @author Charles Prud'homme, Jean-Guillaume Fages
 */
public enum IntEventType implements IEventType {

    /**
     * No event
     */
    VOID(0),
    /**
     * Value removal event
     */
    REMOVE(1),
    /**
     * Increasing lower bound event
     */
    INCLOW(2),
    /**
     * Decreasing upper bound event
     */
    DECUPP(4),

    /**
     * Increasing lower bound and decreasing upper bound event
     */
    BOUND(6),

    /**
     * Instantiation event
     */
    INSTANTIATE(8);

    /**
     * mask of an event
     */
    private final int mask;

    IntEventType(int mask) {
        this.mask = mask;
    }

    @Override
    public int getMask() {
        return mask;
    }

    //******************************************************************************************************************
    //******************************************************************************************************************

    /**
     * Combination of masks for all events.
     */
    private static final int RIDI = combine(REMOVE, BOUND, INSTANTIATE);

    /**
     * Combination of masks for bound + instantiate events
     */
    private static final int IDI = combine(BOUND, INSTANTIATE);

    /**
     * Combines into a single mask some evts.
     * @param evts list of events to combine
     * @return mask expressing the combination of evts
     */
    public static int combine(IntEventType... evts) {
        int mask = 0;
        for (int i = 0; i < evts.length; i++) {
            mask |= evts[i].mask;
        }
        return mask;
    }

    /**
     * @return the remove, bounds and instantiation events' mask
     */
    public static int all() {
        return RIDI;
    }

    /**
     * @return the bound and instantiation events' mask
     */
    public static int boundAndInst() {
        return IDI;
    }

    /**
     * @return the instantiation event's mask
     */
    public static int instantiation() {
        return INSTANTIATE.mask;
    }

    //******************************************************************************************************************
    //******************************************************************************************************************

    /**
     * @param mask the mask to check
     * @return <tt>true</tt> if <code>mask</code> contains an {@link #INSTANTIATE} event mask.
     */
    public static boolean isInstantiate(int mask) {
        return (mask & INSTANTIATE.mask) != 0;
    }

    /**
     * @param mask the mask to check
     * @return <tt>true</tt> if <code>mask</code> contains an {@link #REMOVE} event mask.
     * Note that it does not test event hierarchy: even if {@link #INCLOW} is a sequence of {@link #REMOVE} this method
     * will return <tt>false</tt> when '2' is given as input.
     */
    public static boolean isRemove(int mask) {
        return (mask & REMOVE.mask) != 0;
    }


    /**
     * @param mask the mask to check
     * @return <tt>true</tt> if <code>mask</code> contains an {@link #BOUND} event mask.
     */
    public static boolean isBound(int mask) {
        return (mask & BOUND.mask) != 0;
    }

    /**
     * @param mask the mask to check
     * @return <tt>true</tt> if <code>mask</code> contains an {@link #INCLOW} event mask.
     */
    public static boolean isInclow(int mask) {
        return (mask & INCLOW.mask) != 0;
    }

    /**
     * @param mask the mask to check
     * @return <tt>true</tt> if <code>mask</code> contains an {@link #DECUPP} event mask.
     */
    public static boolean isDecupp(int mask) {
        return (mask & DECUPP.mask) != 0;
    }
}
