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
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.ranges.IntIterableRangeSet;
import org.chocosolver.solver.variables.ranges.IntIterableSetUtils;

import java.util.BitSet;

/**
 * Domain-consistency Allen relation constraint with variable lengths:
 * <p>
 * allen(Relation, Oi, Li, Oj, Lj)
 * </p>
 * <p>
 * Relation in 1..13 encodes the relation between (Oi, Li) and (Oj, Lj) where:
 * <ul>
 * <li><b>Oi</b>: origin of the first interval</li>
 * <li><b>Li</b>: length of the first interval</li>
 * <li><b>Oj</b>: origin of the second interval</li>
 * <li><b>Lj</b>: length of the second interval</li>
 * </ul>
 * </p>
 * <p>
 * Relation encoding:
 * <p>
 * [b, bi, d, di, e, f, fi, m, mi, o, oi, s, si]<br/>
 * 1  2   3  4   5  6  7   8  9   10 11  12 13
 * </p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 05/02/2016.
 */
public abstract class AllenRelation {
    /**
     * Allen relation: I takes place before J
     */
    public static final int b = 1;
    /**
     * Allen relation: J takes place before I
     */
    public static final int bi = 2;
    /**
     * Allen relation: I during J
     */
    public static final int d = 3;
    /**
     * Allen relation: J during I
     */
    public static final int di = 4;
    /**
     * Allen relation: I is equal to J
     */
    public static final int e = 5;
    /**
     * Allen relation: I finishes J
     */
    public static final int f = 6;
    /**
     * Allen relation: J finishes I
     */
    public static final int fi = 7;
    /**
     * Allen relation: I meets J
     */
    public static final int m = 8;
    /**
     * Allen relation: J meets I
     */
    public static final int mi = 9;
    /**
     * Allen relation: I overlaps J
     */
    public static final int o = 10;
    /**
     * Allen relation: J overlaps I
     */
    public static final int oi = 11;
    /**
     * Allen relation: I starts J
     */
    public static final int s = 12;
    /**
     * Allen relation: J starts I
     */
    public static final int si = 13;

    /**
     * To convert relation from 1..13 to 12..0 (for technical reasons.
     */
    static int[] converse = {-1, 2, 1, 4, 3, 5, 7, 6, 9, 8, 11, 10, 13, 12};

    /**
     * The available relations
     */
    protected final IntVar Rel;
    /**
     * The origin of the first interval
     */
    protected final IntVar Oi;
    /**
     * The length of the first interval
     */
    protected final IntVar Li;
    /**
     * the origin of the second interval
     */
    protected final IntVar Oj;
    /**
     * the length of the second interval
     */
    protected final IntVar Lj;
    /**
     * Cause of filtering (master propagator, for technical purpose)
     */
    protected final ICause cause;

    /**
     * To store the domain of {@link #Oi} as a integer set
     */
    protected final IntIterableRangeSet sOi;
    /**
     * To store the domain of {@link #Li} as a integer set
     */
    protected final IntIterableRangeSet sLi;
    /**
     * To store the domain of {@link #Oj} as a integer set
     */
    protected final IntIterableRangeSet sOj;
    /**
     * To store the domain of {@link #Lj} as a integer set
     */
    protected final IntIterableRangeSet sLj;

    /** to compute union **/
    IntIterableRangeSet[] generalization;

    /**
     * Set up this Allen relation filtering algorithm.
     *
     * @param Rel   integer variable (domain should not exceed [1,13])
     * @param Oi    origin of the first interval
     * @param Li    length of the first interval
     * @param Oj    origin of the second interval
     * @param Lj    lenght of th second interval
     * @param cause master propagator which calls this
     */
    public AllenRelation(IntVar Rel, IntVar Oi, IntVar Li, IntVar Oj, IntVar Lj, ICause cause) {
        this.Rel = Rel;
        this.Oi = Oi;
        this.Li = Li;
        this.Oj = Oj;
        this.Lj = Lj;
        this.cause = cause;
        this.sOi = new IntIterableRangeSet();
        this.sLi = new IntIterableRangeSet();
        this.sOj = new IntIterableRangeSet();
        this.sLj = new IntIterableRangeSet();
        generalization = new IntIterableRangeSet[4];
        for(int i = 0; i < 4; i++){
            generalization[i] = new IntIterableRangeSet();
        }
    }

    /**
     * Filter from impossible relations from Rel and for remaining ones, compute union of possible values for Oi, Li, Oj, Lj and
     * restrict their domain to this unions.
     *
     * @return <tt>true</tt> if entailment is detected, <tt>false</tt> otherwise.
     * @throws ContradictionException if a domain wipe-out occurs.
     */
    public boolean filter() throws ContradictionException {
        for(int i = 0; i < 4; i++){
            generalization[i].clear();
        }
        int lr = Rel.getUB();
        for (int r = Rel.getLB(); r <= lr; r = Rel.nextValue(r)) {
            IntIterableSetUtils.copyIn(Oi, sOi);
            IntIterableSetUtils.copyIn(Li, sLi);
            IntIterableSetUtils.copyIn(Oj, sOj);
            IntIterableSetUtils.copyIn(Lj, sLj);
            int rc = prune_relation(r, sOi, sLi, sOj, sLj);
            if (rc < 0) {
                Rel.removeValue(r, cause);
            } else {
                IntIterableSetUtils.unionOf(generalization[0], sOi);
                IntIterableSetUtils.unionOf(generalization[1], sOj);
                IntIterableSetUtils.unionOf(generalization[2], sLi);
                IntIterableSetUtils.unionOf(generalization[3], sLj);
            }
        }
        dvar_fix_set(Oi, generalization[0], cause);
        dvar_fix_set(Oj, generalization[1], cause);
        dvar_fix_set(Li, generalization[2], cause);
        dvar_fix_set(Lj, generalization[3], cause);
        return Rel.isInstantiated()
                && ((!Oi.isInstantiated() ^ !Oj.isInstantiated() ^ !Li.isInstantiated() ^ !Lj.isInstantiated())
                || Oi.isInstantiated() & Oj.isInstantiated() & Li.isInstantiated() & Lj.isInstantiated());
    }

    /**
     * Check if a relation holds for tuple < Oi, Li, Oj, Lj >
     *
     * @param extrel index of the relation
     * @param sOi    origin of the first interval
     * @param sLi    length of the first interval
     * @param sOj    origin of the second interval
     * @param sLj    length of the second interval
     * @return 0 if the fix point is reached, < 0 if the relation does not hold
     */
    abstract int prune_relation(int extrel, IntIterableRangeSet sOi, IntIterableRangeSet sLi, IntIterableRangeSet sOj, IntIterableRangeSet sLj);

    /**
     * restrict the domain of <i>dvar</i> to <i>integers</i>
     *
     * @param dvar     an integer variable
     * @param integers set of integers
     * @param cause    propagator which triggered this filtering algorith
     * @return <tt>true</tt> if domain changed, <tt>false</tt> otherwise
     * @throws ContradictionException if domain wiped out
     */
    final boolean dvar_fix_set(IntVar dvar, IntIterableRangeSet integers, ICause cause) throws ContradictionException {
        return dvar.removeAllValuesBut(integers, cause);
    }

    /**
     * Check the current state of the variables.
     * @return <tt>true</tt> if the constraint can be satisfied
     */
    public boolean check() {
        BitSet boi = new BitSet(), bli = new BitSet(), boj = new BitSet(), blj = new BitSet();
        for (int oi = Oi.getLB(); oi <= Oi.getUB(); oi = Oi.nextValue(oi)) {
            for (int li = Li.getLB(); li <= Li.getUB(); li = Li.nextValue(li)) {
                for (int oj = Oj.getLB(); oj <= Oj.getUB(); oj = Oj.nextValue(oj)) {
                    for (int lj = Lj.getLB(); lj <= Lj.getUB(); lj = Lj.nextValue(lj)) {
                        // at least one relation should validate the tuple
                        boolean valid = false;
                        for (int r = Rel.getLB(); r <= Rel.getUB(); r = Rel.nextValue(r)) {
                            valid |= checkRelation(r, oi, li, oj, lj);
                            valid |= checkRelation(converse[r], oj, lj, oi, li);
                        }
                        if(valid){
                            boi.set(oi);
                            bli.set(li);
                            boj.set(oj);
                            blj.set(lj);
                        }
                    }
                }
            }
        }
        if(boi.cardinality()!= Oi.getDomainSize())return false;
        for (int oi = Oi.getLB(); oi <= Oi.getUB(); oi = Oi.nextValue(oi)) {
            if(!boi.get(oi))return false;
        }
        if(bli.cardinality()!= Li.getDomainSize())return false;
        for (int li = Li.getLB(); li <= Li.getUB(); li = Li.nextValue(li)) {
            if(!bli.get(li))return false;
        }
        if(boj.cardinality()!= Oj.getDomainSize())return false;
        for (int oj = Oj.getLB(); oj <= Oj.getUB(); oj = Oj.nextValue(oj)) {
            if(!boj.get(oj))return false;
        }
        if(blj.cardinality()!= Lj.getDomainSize())return false;
        for (int lj = Lj.getLB(); lj <= Lj.getUB(); lj = Lj.nextValue(lj)) {
            if(!blj.get(lj))return false;
        }
        return true;
    }

    private boolean checkRelation(int r, int o_i, int l_i, int o_j, int l_j) {
        boolean valid;
        switch (r) {
            default:
                throw new UnsupportedOperationException();
            case b:
                valid = o_i + l_i < o_j;
                break;
            case bi:
                valid = o_j + l_j < o_i;
                break;
            case d:
                valid = o_j < o_i && o_i + l_i < o_j + l_j;
                break;
            case di:
                valid = o_i < o_j && o_j + l_j < o_i + l_i;
                break;
            case e:
                valid = o_i == o_j && o_i + l_i == o_j + l_j;
                break;
            case f:
                valid = o_j < o_i && o_i + l_i == o_j + l_j;
                break;
            case fi:
                valid = o_i < o_j && o_j + l_j == o_i + l_i;
                break;
            case m:
                valid = o_i + l_i == o_j;
                break;
            case mi:
                valid = o_j + l_j == o_i;
                break;
            case o:
                valid = o_i < o_j && o_i + l_i > o_j && o_i + l_i < o_j + l_j;
                break;
            case oi:
                valid = o_j < o_i && o_j + l_j > o_i && o_j + l_j < o_i + l_i;
                break;
            case s:
                valid = o_i == o_j && o_i + l_i < o_j + l_j;
                break;
            case si:
                valid = o_j == o_i && o_j + l_j < o_i + l_i;
                break;
        }
        return valid;
    }
}
