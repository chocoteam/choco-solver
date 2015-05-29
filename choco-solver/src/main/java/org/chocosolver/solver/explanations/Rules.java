/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
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
package org.chocosolver.solver.explanations;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;

import java.util.Arrays;
import java.util.BitSet;

/**
 * A set of rules for the rulestore
 * <p>
 * Created by cprudhom on 17/03/15.
 * Project: choco.
 */
public class Rules {

    private static final int NO_ENTRY = Integer.MIN_VALUE;
    private final BitSet paRules; // rules for propagator activation
    private final int[] vmRules;    // rules for variable modification
    private final TIntSet[] vmRemval;    // store value removal when necessary

    public Rules(int maxPid, int maxVid) {
        this.paRules = new BitSet(maxPid);
        this.vmRules = new int[maxVid];
        Arrays.fill(vmRules, NO_ENTRY);
        this.vmRemval = new TIntSet[maxVid];
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
        return vmRules[vid];
    }

    /**
     * Get the removed values associated with 'vid'
     *
     * @param vid the variable id
     * @return the set of removed values up to now
     */
    public TIntSet getVmRemval(int vid) {
        TIntSet remvals = vmRemval[vid];
        if (remvals == null) {
            remvals = new TIntHashSet(16, .5f, NO_ENTRY);
            vmRemval[vid] = remvals;
        }
        return vmRemval[vid];
    }

    /**
     * Update the rule mask for a given variable (denoted by its vid)
     *
     * @param vid  the index of the variable
     * @param mask the new mask to merge
     * @return true if the mask has been updated (false = already existing mask)
     */
    public boolean putMask(int vid, int mask) {
        int cmask = vmRules[vid];
        if (cmask == NO_ENTRY) {
            vmRules[vid] = mask;
            return true;
        } else {
            int amount = (cmask | mask) - cmask;
            vmRules[vid] += amount;
            return amount > 0;
        }
    }

    /**
     * Clear this
     */
    public void clear() {
        paRules.clear();
        Arrays.fill(vmRules, NO_ENTRY);
        for (int k = vmRemval.length - 1; k >= 0; k--) {
            if (vmRemval[k] != null) vmRemval[k].clear();
        }
    }

    /**
     * Check whether an interval intersects at least one value from a given set
     *
     * @param i1  lower bound of the interval (included)
     * @param i2  upper bound of the interval (included)
     * @param vid variable id
     */
    public boolean intersect(int i1, int i2, int vid) {
        assert vmRemval[vid].size() > 0;
        while (i1 <= i2 && !vmRemval[vid].contains(i1)) {
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
        Rules nrules = new Rules(paRules.length(), vmRemval.length);
        nrules.paRules.or(this.paRules);
        System.arraycopy(this.vmRules, 0, nrules.vmRules, 0, nrules.vmRules.length);
        for (int i = 0; i < this.vmRemval.length; i++) {
            if (this.vmRemval[i] != null && this.vmRemval[i].size() > 0) {
                nrules.vmRemval[i] = new TIntHashSet(16, .5f, NO_ENTRY);
                nrules.vmRemval[i].addAll(this.vmRemval[i]);
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
            for (int i = vmRules.length - 1; i >= 0; i--) {
                if (rules.vmRules[i] != NO_ENTRY) {
                    putMask(i, rules.vmRules[i]);
                    if (rules.vmRemval[i] != null && rules.vmRemval[i].size() > 0) {
                        getVmRemval(i).addAll(rules.vmRemval[i]);
                    }
                }
            }
        }
    }
}
