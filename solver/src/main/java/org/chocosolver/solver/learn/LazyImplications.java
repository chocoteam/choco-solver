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

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

import java.util.Comparator;
import java.util.HashMap;

/**
 * This implication graph is lazily built.
 * It maintains the list of events invoked during propagation on each variable,
 * and maintains a copy of each domain.
 * <p>
 * Data is stored in a stack, where each entry is a tuple:
 * <pre>< v, D, c, m, p, n></pre>
 * where :
 *     <ul>
 *         <li>v is the variable modified,</li>
 *         <li>D is its current domain,</li>
 *         <li>c is the cause (most of the time, a propagator),</li>
 *         <li>m is the mask of event that triggers the modification,</li>
 *         <li>p is a pointer to the previous entry on v in the stack</li>
 *         <li>n is a pointer to the next entry on v in the stack (if any)</li>
 *     </ul>
 * <p>
 *     Note that, under some conditions, two entries can be merged into a single one.
 *
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 25/01/2017.
 */
public class LazyImplications extends Implications {

    /**
     * An entry < v, D, c, m, p, n> where :
     *     <ul>
     *         <li>v is the variable modified,</li>
     *         <li>D is its current domain,</li>
     *         <li>c is the cause (most of the time, a propagator),</li>
     *         <li>m is the mask event that triggers the modification,</li>
     *         <li>i is the index of this entry in the stack</li>
     *         <li>p is a pointer to the previous entry on v in the stack</li>
     *     </ul>
     *
     *     Invar:  (p < n) xor ("root entry")
     */
    static class Entry implements Comparator<Entry> {
        // modified variable
        IntVar v;
        // domain after modification
        IntIterableRangeSet d;
        // who causes the modification
        ICause c;
        // mask of the event
        int m;
        int e;
        // index of this in the stack
        int i;
        // index of direct predecessor (same variable)
        int p;
        // decision level
        int dl;

        @Override
        public String toString() {
            return String.format("<%s, %s, %s, %s, %d, %d, %d>", v.getName(), d, c, m, i, p, dl);
        }

        Entry() {
            d = new IntIterableRangeSet();
        }

        public void set(IntVar v, ICause c, int m, int e, int i, int p, int dl) {
            this.v = v;
            this.c = c;
            this.m = m;
            this.e = e;
            this.i = i;
            this.p = p;
            this.dl = dl;
        }

        public IntIterableRangeSet getD() {
            return d;
        }

        void setPrev(int p) {
            this.p = p;
        }

        @Override
        public int compare(Entry o1, Entry o2) {
            return o1.i - o2.i;
        }
    }

    /**
     * Ordered list of entries.
     */
    Entry[] entries;
    /**
     * Pointer, per variable, to the root entry {@link #entries}
     */
    final HashMap<IntVar, Entry> rootEntries;
    /**
     * Number of entries stored -- backtrackable
     */
    private final IStateInt size;
    /**
     * Number of active entries in {@link #entries}
     */
    private int nbEntries;
    /**
     * Inform when a decision level changes
     */
    private boolean tagDl;

    /**
     * Create lazily built implication graph
     * @param model the model that uses this
     */
    LazyImplications(Model model) {
        nbEntries = 0;
        size = model.getEnvironment().makeInt(0);
        size._set(0, 0); // to force history manually -- required when created during the search
        entries = new Entry[16];
        rootEntries = new HashMap<>(16, .5f);
        init(model);
    }

    @Override
    public void init(Model model) {
        IntVar[] ivars = model.retrieveIntVars(true);
        for (IntVar var : ivars) {
            ensureCapacity();
            Entry root = entries[nbEntries] = new Entry();
            root.set(var, Cause.Null, IntEventType.VOID.getMask(), 0, nbEntries, nbEntries,1);
            root.getD().copyFrom(var);
            root.d.lock();
            var.createLit(root.d);
            rootEntries.put(var, root);
            nbEntries++;
        }
        size.set(nbEntries);
    }

    @Override
    public void reset(){
        synchronize(rootEntries.size());
    }

    /**
     * @return <i>true</i> if links between nodes in this graph are correct
     */
    private boolean checkIntegrity() {
        for (Entry r : rootEntries.values()) {
            int dec = nbEntries;
            Entry prev = entries[r.p];
            if(prev.i > dec) return false;
            while (dec > 0 && prev != r) {
                prev = entries[prev.p];
                dec--;
            }
            if (dec == 0) return false;
        }
        return true;
    }

    /**
     * Remove node below <i>upto</i> in the graph, and reconnect the new leaves with their root node.
     * @param upto last correct node
     */
    private void synchronize(int upto) {
        for (int p = upto; p < nbEntries; p++) {
            Entry e = entries[p];
            e.getD().unlock();
            Entry root = rootEntries.get(e.v);
            if (root.p >= upto) {
                root.setPrev(e.p);
            }
        }
        nbEntries = upto;
        assert !XParameters.DEBUG_INTEGRITY || checkIntegrity();
    }

    @Override
    public void tagDecisionLevel() {
        tagDl = true;
    }

    @Override
    public void undoLastEvent() {
        Entry toUndo = entries[--nbEntries];
        rootEntries.get(toUndo.v).p = toUndo.p;
    }

    /**
     * Make sure that a new entry can be added into this graph
     */
    private void ensureCapacity() {
        if (nbEntries >= entries.length) {
            int oldCapacity = entries.length;
            int newCapacity = oldCapacity + (oldCapacity >> 1);
            Entry[] entBigger = new Entry[newCapacity];
            System.arraycopy(entries, 0, entBigger, 0, oldCapacity);
            entries = entBigger;
        }
    }

    /**
     * Return <i>true</i> if two entries, based on the same variable, can be merged.
     * It depends on {@link XParameters#MERGE_CONDITIONS} which can be set to:
     * <ul>
     *     <li>
     *         0: merge is disabled
     *     </li>
     *     <li>
     *         1: merge two consecutive entries with the same variable and cause
     *     </li>
     *     <li>
     *          2: merge two entries with the same variable and cause,
     *          as long as they are done in the same filtering call.
     *     </li>
     * </ul>
     *
     * @param prev previous entry to merge with
     * @param cause cause of the current event to merge into prev
     * @return <i>true</i> if two entries can be merged into a single one.
     */
    @SuppressWarnings("ConstantConditions")
    private boolean mergeConditions(Entry prev, ICause cause) {
        switch (XParameters.MERGE_CONDITIONS){
            default:
            case 0:
                return false;
            case 1:
                return nbEntries - 1 == prev.i && cause == prev.c;
        }
    }

    /**
     * Merge two consecutive entries as long as they are based on the same variable and cause.
     * The domain and mask are merged.
     * @param evt event received
     * @param one value removed, new lower bound, new upper bound or singleton value, wrt to <i>evt</i>
     * @param nentry entry to merge with
     */
    private void mergeEntry(IntEventType evt, int one, Entry nentry){
        nentry.m |= evt.getMask();
        nentry.getD().unlock();
        mergeDomain(nentry.getD(), evt, one);
        nentry.getD().lock();
    }

    /**
     * Update a domain wrt a given event and its specialization
     * @param dom domain to update
     * @param mask event mask
     * @param one an int (value removed, new bound, or singleton)
     */
    private static void mergeDomain(IntIterableRangeSet dom, IntEventType mask, int one) {
        switch (mask) {
            case VOID:
            case BOUND:
                throw new Error("Unknown case "+ mask);
            case INSTANTIATE:
                dom.retainBetween(one, one);
                break;
            case REMOVE:
                dom.remove(one);
                break;
            case INCLOW:
                dom.removeBetween(dom.min(), one - 1);
                break;
            case DECUPP:
                dom.removeBetween(one + 1, dom.max());
                break;
        }
    }


    /**
     * Create a domain wrt a given event and its specialization
     * @param to domain to create
     * @param from domain to read
     * @param mask event mask
     * @param one an int (value removed, new bound, or singleton)
     */
    private static void createDomain(IntIterableRangeSet to, IntIterableRangeSet from, IntEventType mask, int one) {
        switch (mask) {
            case VOID:
            case BOUND:
                throw new Error("Unknown case VOID");
            case REMOVE:
                IntIterableSetUtils.unionOf(to, from);
                to.remove(one);
                break;
            case INCLOW:
                IntIterableSetUtils.intersection(to, from, one, from.max());
                break;
            case DECUPP:
                IntIterableSetUtils.intersection(to, from, from.min(), one);
                break;
            case INSTANTIATE:
                IntIterableSetUtils.intersection(to, from, one, one);
                break;
        }
    }

    /**
     * Create a new entry in this graph
     * @param var a variable
     * @param cause the cause of <i>var</i> modification
     * @param evt the type of event received
     * @param one value removed, new lower bound, new upper bound or singleton value, wrt to <i>evt</i>
     * @param root its root node
     * @param prev its predecessor
     */
    private void addEntry(IntVar var, ICause cause, IntEventType evt, int one,
                          Entry root, Entry prev){
        ensureCapacity();
        // create entry
        Entry nentry = entries[nbEntries];
        if (nentry == null) {
            nentry = entries[nbEntries] = new Entry();
        } else {
            nentry.getD().clear();
        }
        int dl = entries[nbEntries-1].dl;
        if(tagDl){
            tagDl = false;
            dl++;
        }
        nentry.set(var, cause, evt.getMask(), one, nbEntries, prev.i, dl);
        // make a (weak) copy of prev domain and update it wrt to current event
        createDomain(nentry.getD(), prev.d, evt, one);
        nentry.getD().lock();
        // connect everything
        root.setPrev(nbEntries);

        size.add(1);
        nbEntries++;
    }

    /**
     * @implSpec
     * Consecutive events for the same couple (variable, cause) are merged into a single one.
     */
    @Override
    public void pushEvent(IntVar var, ICause cause, IntEventType evt, int one, int two, int three) {
        int size_ = size.get();
        if (nbEntries != size_) {
            synchronize(size_);
        }
        Entry root = rootEntries.get(var);
        if (root == null) {
            throw new Error("Unknown variable. This happens when a constraint is added after the call to `solver.setLearningClause();`");
        }
        int pidx = root.p;
        Entry prev = entries[pidx];
        assert prev != null;
        assert prev.v == var;
        if(mergeConditions(prev, cause)){
            mergeEntry(evt, one, prev);
        }else{
            addEntry(var, cause, evt, one, root, prev);
        }
        assert !XParameters.DEBUG_INTEGRITY || checkIntegrity();
    }


    /**
     * Find the right-most node, before  <i>p</i>, in this,
     * such that <i>var</i> matches the node.
     * @param var a variable
     * @return right-most position of var between [0,p] in this
     */
    int rightmostNode(int limit, IntVar var) {
        if(var.isBool()){
            Entry root = rootEntries.get(var);
            int ri = root.i;
            assert ri < limit :"impossible right-most search";
            // consider the case where the variable failed
            if(root.p >= limit){
                root = entries[root.p];
            }
            return root.p < limit ? root.p : ri;
        }else {
            // two ways of looking for the node
            // 1. reverse-iteration over all nodes, starting from 'limit-1'
            int pos = limit - 1;
            // 2. reverse-iteration over nodes of var, starting from 'root.p'
            // (presumably far away from limit)
            int prev = rootEntries.get(var).p;
            while (pos > 0 && entries[pos].v != var && prev > limit) {
                pos--;
                prev = entries[prev].p;
            }
            return prev > limit ? pos: prev;
        }
    }

    @Override
    public int size() {
        return size.get();
    }

    @Override
    public void collectNodesFromConflict(ContradictionException cft, ValueSortedMap<IntVar> front) {
        if (cft.v != null) {
            Entry root = rootEntries.get(cft.v);
            assert entries[root.p].c == cft.c;
            front.put((IntVar) cft.v, root.p);
        } else {
            cft.c.forEachIntVar(v -> {
                Entry root = rootEntries.get(v);
                front.put(root.v, root.p);
            });
        }
    }

    @Override
    public void predecessorsOf(int p, ValueSortedMap<IntVar> front) {
        Entry entry = entries[p];
        ICause cause = entry.c;
        // add the predecessor of 'p'
        front.put(entry.v, entry.p);
        cause.forEachIntVar(v -> findPredecessor(front, v, p));
    }

    /**
     * Find the direct predecessor of a node, declared on variable <i>vi</i>, starting from node at
     * position <i>p</i>.
     * If a variable-based node already exists in <i>front</i>, then this node is used to look for the predecessor,
     * assuming that it is below <i>p</i> (otherwise, this node the predecessor we are looking for).
     * Otherwise, there is no node based on <i>vi</i> in <i>front</i> and the rightmost node above
     * <i>p</i>, starting from the predecessor of its root node, is added.
     * @param front the set to update
     * @param vi the variable to look the predecessor for
     * @param p the rightmost position of the node (below means outdated node).
     */
    public void findPredecessor(ValueSortedMap<IntVar> front, IntVar vi, int p) {
        int cpos = front.getValueOrDefault(vi, Integer.MAX_VALUE);
        if(cpos < Integer.MAX_VALUE) {
            while (cpos > p) {
                cpos = entries[cpos].p;
            }
            front.replace(vi, cpos);
        }else {
            front.put(vi, rightmostNode(p, vi));
        }
    }

    @Override
    public ICause getCauseAt(int idx) {
        return entries[idx].c;
    }

    @Override
    public int getEventMaskAt(int idx) {
        return entries[idx].m;
    }

    @Override
    public IntVar getIntVarAt(int idx) {
        return entries[idx].v;
    }

    @Override
    public int getValueAt(int idx) {
        assert XParameters.MERGE_CONDITIONS == 0;
        return entries[idx].e;
    }

    @Override
    public int getDecisionLevelAt(int idx) {
        return entries[idx].dl;
    }

    @Override
    public IntIterableRangeSet getDomainAt(int idx) {
        return entries[idx].d;
    }

    @Override
    public int getPredecessorOf(int idx) {
        return entries[idx].p;
    }

    @Override
    public IntIterableRangeSet getRootDomain(IntVar var) {
        return rootEntries.get(var).d;
    }

    @Override
    public void copyComplementSet(IntVar var, IntIterableRangeSet set, IntIterableRangeSet dest) {
        dest.copyFrom(rootEntries.get(var).d);
        dest.removeAll(set);
    }
}
