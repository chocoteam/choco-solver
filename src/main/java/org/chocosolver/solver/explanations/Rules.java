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
package org.chocosolver.solver.explanations;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.Variable;

import java.util.BitSet;

/**
 * A set of rules for {@link RuleStore}
 * <p>
 * Created by cprudhom on 17/03/15.
 * Project: choco.
 * @author Charles Prud'homme
 */
public class Rules {

    /**
     * no entry value for vmRules
     */
    private static final int NO_ENTRY = Integer.MIN_VALUE;

    /**
     * Stores index of propagators ({@link Propagator#getId()}) involved into an explanation
     */
    private BitSet paRules;

    /**
     * Stores index of variables ({@link Variable#getId()}) involved into an explanation
     */
    private BitSet vmRules;

    /**
     * Stores for variables in {@link #vmRules} the type of modifications related to an explanation.
     * Possible masks are: REM or LB or UB or MOD.
     */
    private int[] vmMasks;

    /**
     * Stores value removals of a given variable ({@link Variable#getId()}), when needed
     */
    private NoIteratorIntHashSet[] remVal;

    /**
     * Creates a set of rules for {@link RuleStore}.
     * It stores events related to an explanation.
     * @param i1 initial size of {@link #vmMasks}
     * @param i2 initial size of {@link #remVal}
     */
    public Rules(int i1, int i2) {
        this.paRules = new BitSet();
        this.vmRules = new BitSet();
        this.vmMasks = new int[i1];
        this.remVal = new NoIteratorIntHashSet[i2];
    }

    /**
     * Get the propagator activation rule associated with 'pid'
     *
     * @param pid a propagator id
     * @return true if the rule exists
     */
    public boolean getPaRules(int pid) {
        return paRules.get(pid);
    }

    /**
     * Clear the propagator activation rule associated with 'pid'
     *
     * @param pid a propagator id
     */
    public void paRulesClear(int pid) {
        paRules.clear(pid);
    }

    /**
     * Add a propagator activation rule for 'pid'
     *
     * @param pid a propagator id
     */
    public void addPaRules(int pid) {
        paRules.set(pid);
    }

    /**
     * Get the variable modification rule associated with 'vid'
     *
     * @param vid a variable id
     * @return the mask of rules
     */
    public int getVmRules(int vid) {
        if (vid < vmMasks.length && vmRules.get(vid)) {
            return vmMasks[vid];
        }
        return NO_ENTRY;
    }

    /**
     * Makes sure that {@link #remVal} is large enough to store data
     * @param size excepted size remVal
     */
    private void ensureRemvalCapacity(int size) {
        if (size >= remVal.length) {
            int nsize = Math.max(size, remVal.length * 3 / 2 + 1);
            NoIteratorIntHashSet[] tmp = remVal;
            remVal = new NoIteratorIntHashSet[nsize];
            System.arraycopy(tmp, 0, remVal, 0, tmp.length);
        }
    }

    /**
     * Get the removed values associated with 'vid'
     *
     * @param vid the variable id
     * @return the set of removed values up to now
     */
    public TIntSet getVmRemval(int vid) {
        ensureRemvalCapacity(vid + 1);
        NoIteratorIntHashSet remvals = remVal[vid];
        if (remvals == null) {
            remvals = new NoIteratorIntHashSet(16, .5f, NO_ENTRY);
            remVal[vid] = remvals;
        }
        return remVal[vid];
    }

    /**
     * Makes sure that {@link #vmMasks} is large enough to store data
     * @param size excepted size of vmMasks
     */
    private void ensureRulesCapacity(int size) {
        if (size >= vmMasks.length) {
            int nsize = Math.max(size, vmMasks.length * 3 / 2 + 1);
            int[] tmp = vmMasks;
            vmMasks = new int[nsize];
            System.arraycopy(tmp, 0, vmMasks, 0, tmp.length);
        }
    }

    /**
     * Update the rule mask for a given variable (denoted by its vid)
     *
     * @param vid  the index of the variable
     * @param mask the new mask to merge
     * @return true if the mask has been updated (false = already existing mask)
     */
    public boolean putMask(int vid, int mask) {
        ensureRulesCapacity(vid + 1);
        int cmask = vmMasks[vid];
        if (!vmRules.get(vid)) {
            vmMasks[vid] = mask;
            vmRules.set(vid);
            return true;
        } else {
            int amount = (cmask | mask) - cmask;
            vmMasks[vid] += amount;
            return amount > 0;
        }
    }

    /**
     * Clear this
     */
    public void clear() {
        paRules.clear();
        for (int i = vmRules.nextSetBit(0); i > -1; i = vmRules.nextSetBit(i + 1)) {
            if (i < remVal.length && remVal[i] != null) remVal[i].clear();
        }
        vmRules.clear();
    }

    /**
     * Check whether an interval intersects at least one value from a given set
     *
     * @param i1  lower bound of the interval (included)
     * @param i2  upper bound of the interval (included)
     * @param vid variable id
     * @return <tt>true</tt> if intersection is not empty
     */
    public boolean intersect(int i1, int i2, int vid) {
        assert vid < remVal.length && remVal[vid].size() > 0;
        while (i1 <= i2 && !remVal[vid].contains(i1)) {
            i1++;
        }
        return i1 <= i2;
    }

    /**
     * Duplicate the current rules
     *
     * @return a copy of the current object
     */
    public Rules duplicate() {
        Rules nrules = new Rules(this.vmMasks.length, this.remVal.length);
        nrules.paRules.or(this.paRules);
        for (int i = vmRules.nextSetBit(0); i > -1; i = vmRules.nextSetBit(i + 1)) {
            nrules.vmRules.set(i);
            nrules.vmMasks[i] = this.vmMasks[i];
            if (this.remVal[i] != null && this.remVal[i].size() > 0) {
                nrules.remVal[i] = new NoIteratorIntHashSet(16, .5f, NO_ENTRY);
                this.remVal[i].addAllIn(nrules.remVal[i]);
            }
        }
        return nrules;
    }

    /**
     * Merge 'rules' into this
     *
     * @param rules a set of rules
     */
    public void or(Rules rules) {
        if (rules != null) {
            this.paRules.or(rules.paRules);
            for (int i = rules.vmRules.nextSetBit(0); i > -1; i = rules.vmRules.nextSetBit(i + 1)) {
                putMask(i, rules.vmMasks[i]);
                if (i < rules.remVal.length
                        && rules.remVal[i] != null
                        && rules.remVal[i].size() > 0) {
                    ensureRemvalCapacity(i + 1);
                    if (remVal[i] == null) {
                        remVal[i] = new NoIteratorIntHashSet(16, .5f, NO_ENTRY);
                    }
                    rules.remVal[i].addAllIn(remVal[i]);
                }
            }
        }
    }

    /**
     * An extension of TIntHashSet enabling addAll() operation without creating iterator
     */
    private static class NoIteratorIntHashSet extends TIntHashSet {

        public NoIteratorIntHashSet(int initial_capacity, float load_factor, int no_entry_value) {
            super(initial_capacity, load_factor, no_entry_value);
        }

        public NoIteratorIntHashSet() {
        }

        /**
         * Main reason this class exists
         * @param aset another NoIteratorIntHashSet to merge with
         */
        public void addAllIn(NoIteratorIntHashSet aset) {
            int i = _states.length;
            while (i-- > 0) {
                if (_states[i] == 1) {
                    aset.add(_set[i]);
                }
            }
        }

    }
}
