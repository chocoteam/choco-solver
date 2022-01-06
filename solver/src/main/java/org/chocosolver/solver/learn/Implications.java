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

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * A abstract class that maintains an implication graph, built from events generated thanks to the propagation.
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 25/01/2017.
 */
public abstract class Implications {

    /**
     * Initialize this class
     * @param model if needed, a reference to the model that uses this class
     */
    public abstract void init(Model model);

    /**
     * Reset the model, set at its creation state, before calling {@link #init(Model)}.
     */
    public abstract void reset();

    /**
     * Push an event
     * @param var modified variable
     * @param cause cause of the modification
     * @param mask modification mask
     * @param one an int
     * @param two an int
     * @param three an int
     */
    public abstract void pushEvent(IntVar var, ICause cause, IntEventType mask, int one, int two, int three);

    /**
     * @return the number of nodes in the implication graph.
     */
    public abstract int size();

    /**
     * Fill <i>set</i> with indices of nodes that throws the <i>conflict</i>
     * @param conflict the failure, in the form of a {@link ContradictionException}
     * @param front (initially empty) map of (node, variable) in conflict
     */
    public abstract void collectNodesFromConflict(ContradictionException conflict, ValueSortedMap<IntVar> front);

    /**
     * Retrieve the nodes that are predecessors of node <i>p</i> in this and put them into <i>set</i>.
     * @param p index of the node whom predecessors have to be found
     * @param front map of (node, variable) in conflict
     */
    public abstract void predecessorsOf(int p, ValueSortedMap<IntVar> front);

    /**
     * Find the direct predecessor of a node, declared on variable <i>vi</i>, starting from node at
     * position <i>p</i>.
     * @param front the set to update
     * @param vi the variable to look the predecessor for
     * @param p the rightmost position of the node (below means outdated node).
     */
    public abstract void findPredecessor(ValueSortedMap<IntVar> front, IntVar vi, int p);

    /**
     * Get the {@link ICause} declared in node <i>idx</i>
     * @param idx position of the node to query
     * @return the cause declared in node <i>idx</i>
     */
    public abstract ICause getCauseAt(int idx);

    /**
     * Get the mask of event declared in node <i>idx</i>.
     * The mask can be a bitwise operation over mask of {@link IntEventType}.
     * @param idx position of the node to query
     * @return the event declared in node <i>idx</i>
     */
    public abstract int getEventMaskAt(int idx);

    /**
     * Get the {@link IntVar} declared in node <i>idx</i>
     * @param idx position of the node to query
     * @return the intvar declared in node <i>idx</i>
     */
    public abstract IntVar getIntVarAt(int idx);


    /**
     * Get the value declared in node <i>idx</i>
     * @param idx position of the node to query
     * @return the value declared in node <i>idx</i>
     */
    public abstract int getValueAt(int idx);

    /**
     * Get the decision level declared in node <i>idx</i>
     * @param idx position of the node to query
     * @return the decision level declared in node <i>idx</i>
     */
    public abstract int getDecisionLevelAt(int idx);

    /**
     * Get the {@link IntIterableRangeSet} that represents domain declared in node <i>idx</i>
     * @param idx position of the node to query
     * @return the domain declared in node <i>idx</i>
     */
    public abstract IntIterableRangeSet getDomainAt(int idx);

    /**
     * Get the position of precedent entry of the one declared in node <i>idx</i>
     * @param idx position of the node to query
     * @return its ancestor
     */
    public abstract int getPredecessorOf(int idx);

    /**
     * Get the {@link IntIterableRangeSet} that represents domain of this variables
     * as declared in the model
     * @param var variable to query
     * @return the domain declared in node <i>idx</i>
     */
    public abstract IntIterableRangeSet getRootDomain(IntVar var);

    /**
     * Copy in <i>dest</i> the complement of <i>set</i>
     * wrt to root domain of <i>var</i>
     * @param var variable to query
     * @return the domain declared in node <i>idx</i>
     */
    public abstract void copyComplementSet(IntVar var, IntIterableRangeSet set, IntIterableRangeSet dest);

    /**
     * Undo the last event stored, use only when dealing with views.
     */
    public abstract void undoLastEvent();

    public abstract void tagDecisionLevel();
}
