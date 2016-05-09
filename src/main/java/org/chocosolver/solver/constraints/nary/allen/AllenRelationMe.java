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
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.ranges.IntIterableRangeSet;
import org.chocosolver.solver.variables.ranges.IntIterableSetUtils;

import static org.chocosolver.solver.variables.ranges.IntIterableSetUtils.intersectionOf;

/**
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 08/01/2016.
 */
public class AllenRelationMe extends AllenRelation{

    /**
     * Temporary structure, for filter
     */
    protected final IntIterableRangeSet tmp1;
    /**
     * Temporary structure, for filter
     */
    protected final IntIterableRangeSet tmp2;

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
    public AllenRelationMe(IntVar Rel, IntVar Oi, IntVar Li, IntVar Oj, IntVar Lj, ICause cause) {
        super(Rel, Oi, Li, Oj, Lj, cause);
        tmp1 = new IntIterableRangeSet();
        tmp2 = new IntIterableRangeSet();
    }



    /**
     * Compute the forbidden region for Oi and Li wrt to Oj and Lj and relation pc
     *
     * @param relation relation to consider from 1 to 13 for origin, 14 to 27 for length
     * @param sOi      origin of first interval (set of integers)
     * @param sOj      origin of second interval (set of integers)
     * @param sLi      length of first interval (set of integers)
     * @param sLj      length of seconf interval (set of integers)
     * @return <tt>true</tt> if a domain has changed
     */
    protected boolean forbidden_region(
            int relation,
            IntIterableRangeSet sOi, IntIterableRangeSet sOj,
            IntIterableRangeSet sLi, IntIterableRangeSet sLj) {

        boolean change = false;
        switch (relation) {
            default:
                throw new UnsupportedOperationException();
            case b: { // forbidden region for Oi with before
                change = sOi.removeBetween(sOj.last() - sLi.first(), sOi.last());
                break;
            }
            case b + 13: { // forbidden region for Li with before
                change = sLi.removeBetween(sOj.last() - sOi.first(), sLi.last());
                break;
            }
            case bi: { // forbidden region for Oi with before inverse
                change = sOi.removeBetween(sOi.first(), sOj.first() + sLj.first());
                break;
            }
            case bi + 13: // no forbidden region for Li with before inverse
                break;
            case d: { // allowed region for Oi with during
                tmp1.clear();
                int last = sLj.last() - sLi.first() - 1;
                for (int a = sOj.first(); a <= sOj.last(); a = sOj.nextValue(a)) {
                    for (int b = 1; b <= last; b++) {
                        tmp1.add(a + b);
                    }
                }

                tmp1.clear();
                IntIterableSetUtils.plus(tmp1, sOj, 1, sLj.last() - sLi.first() - 1);
                change = intersectionOf(sOi, tmp1);
                break;
            }
            case d + 13: {// forbidden region for Li with during
                int min = Integer.MAX_VALUE;
                for (int c = sOj.first(); c <= sOj.last(); c = sOj.nextValue(c)) {
                    for (int a = sOi.nextValue(c); a <= sOi.last(); a = sOi.nextValue(a)) {
                        if (min > a - c) {
                            min = a - c;
                        }
                    }
                }
//                assert min < Integer.MAX_VALUE;
                change = sLi.removeBetween(sLj.last() - min, sLi.last());
                break;
            }
            case di: { // allowed region for Oi with during inverse
                tmp1.clear();
                IntIterableSetUtils.plus(tmp1, sOj, sLj.first() - sLi.last() + 1, -1);
                change = intersectionOf(sOi, tmp1);
                break;
            }
            case di + 13: {// forbidden region for Li with during
                int min = Integer.MAX_VALUE;
                for (int a = sOi.first(); a <= sOi.last(); a = sOi.nextValue(a)) {
                    for (int c = sOj.nextValue(a); c <= sOj.last(); c = sOj.nextValue(c)) {
                        if (min > c - a) {
                            min = c - a;
                        }
                    }
                }
                assert min < Integer.MAX_VALUE;
                change = sLi.removeBetween(sLi.first(), sLj.first() + min);
                break;
            }
            case e: { // allowed region for Oi with equal
                change = intersectionOf(sOi, sOj);
                break;
            }
            case e + 13: { // allowed region for Li with equal
                change = intersectionOf(sLi, sLj);
                break;
            }
            case f: { // allowed region for Oi with finish
                tmp1.clear();
                tmp2.clear();
                for (int d = sLi.first(); d <= sLi.last(); d = sLi.nextValue(d)) {
                    for (int c = sLj.nextValue(d); c <= sLj.last(); c = sLj.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.plus(tmp2, tmp1, sOj);
                change = IntIterableSetUtils.intersectionOf(sOi, tmp2);
                break;
            }
            case f + 13: { // allowed region for Li with finish
                tmp1.clear();
                tmp2.clear();
                for (int d = sOj.first(); d <= sOj.last(); d = sOj.nextValue(d)) {
                    for (int c = sOi.nextValue(d); c <= sOi.last(); c = sOi.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.minus(tmp2, sLj, tmp1);
                change = intersectionOf(sLi, tmp2);
                break;
            }
            case fi: { // allowed region for Oi with finish inverse
                tmp1.clear();
                tmp2.clear();
                for (int d = sLj.first(); d <= sLj.last(); d = sLj.nextValue(d)) {
                    for (int c = sLi.nextValue(d); c <= sLi.last(); c = sLi.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.minus(tmp2, sOj, tmp1);
                change = intersectionOf(sOi, tmp2);
                break;
            }
            case fi + 13: { // allowed region for Li with finish inverse
                tmp1.clear();
                tmp2.clear();
                for (int d = sOi.first(); d <= sOi.last(); d = sOi.nextValue(d)) {
                    for (int c = sOj.nextValue(d); c <= sOj.last(); c = sOj.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.plus(tmp2, tmp1, sLj);
                change = intersectionOf(sLi, tmp2);
                break;
            }
            case m: { // allowed region for Oi with meet
                tmp1.clear();
                IntIterableSetUtils.minus(tmp1, sOj, sLi);
                change = intersectionOf(sOi, tmp1);
                break;
            }
            case m + 13: { // allowed region for Li with meet
                tmp1.clear();
                IntIterableSetUtils.minus(tmp1, sOj, sOi);
                change = intersectionOf(sLi, tmp1);
                break;
            }
            case mi: { // allowed region for Oi with meet inverse
                tmp1.clear();
                IntIterableSetUtils.plus(tmp1, sOj, sLj);
                change = intersectionOf(sOi, tmp1);
                break;
            }
            case mi + 13: // no forbidden region for Li with meet inverse
                break;
            case o: { // allowed region for Oi with overlap
                tmp1.clear();
                tmp2.clear();
                for (int d = 1; d <= sLj.last() - 1; d++) {
                    for (int c = sLi.nextValue(d); c <= sLi.last(); c = sLi.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.minus(tmp2, sOj, tmp1);
                change = intersectionOf(sOi, tmp2);
                break;
            }
            case o + 13: { // allowed region for Li with overlap
                tmp1.clear();
                tmp2.clear();
                for (int d = sOi.first(); d <= sOi.last(); d = sOi.nextValue(d)) {
                    for (int c = sOj.nextValue(d); c <= sOj.last(); c = sOj.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.plus(tmp2, tmp1, 1, sLj.last() - 1);
                change = intersectionOf(sLi, tmp2);
                break;
            }
            case oi: { // allowed region for Oi with overlap inverse
                tmp1.clear();
                tmp2.clear();
                for (int d = 1; d <= sLi.last() - 1; d++) {
                    for (int c = sLj.nextValue(d); c <= sLj.last(); c = sLj.nextValue(c)) {
                        tmp1.add(c - d);
                    }
                }
                IntIterableSetUtils.plus(tmp2, sOj, tmp1);
                change = intersectionOf(sOi, tmp2);
                break;
            }
            case oi + 13: { // forbidden region for Li with overlap inverse
                int min = Integer.MAX_VALUE;
                for (int a = sOj.first(); a <= sOj.last(); a = sOj.nextValue(a)) {
                    for (int b = sLj.first(); b <= sLj.last(); b = sLj.nextValue(b)) {
                        for (int c = sOi.nextValue(a); c <= sOi.previousValue(a + b); c = sOi.nextValue(c)) {
                            if (/*a < c && c < a + b && */min > a - c + b) {
                                min = a + b - c;
                            }
                        }
                    }
                }
                change = sLi.removeBetween(sLi.first(), min);
                break;
            }
            case s: { // allowed region for Oi with start
                change = intersectionOf(sOi, sOj);
                break;
            }
            case s + 13: { // forbidden region for Li with start
                change = sLi.removeBetween(sLj.last(), sLi.last());
                break;
            }
            case si: { // allowed region for Oi with start inverse
                change = intersectionOf(sOi, sOj);
                break;
            }
            case si + 13: { // forbidden region for Li with start inverse
                change = sLi.removeBetween(sLi.first(), sLj.first());
                break;
            }
        }
        return change;
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
    @Override
    int prune_relation(int extrel, IntIterableRangeSet sOi, IntIterableRangeSet sLi, IntIterableRangeSet sOj, IntIterableRangeSet sLj) {
        int intrel = /*13 - */extrel, change;
        int cintrel = converse[intrel];
//        do {
            change = 0;
            /* must check at least one variable */
            if (forbidden_region(intrel, sOi, sOj, sLi, sLj)) {
                change |= sOi.size() > 0 ? 1 : -1;
            }
            if (change >= 0 && /*sLi.size() > 1 &&*/ forbidden_region(intrel + 13, sOi, sOj, sLi, sLj)) {
                change |= sLi.size() > 0 ? 1 : -1;
            }
            if (change >= 0 && /*sOj.size() > 1 &&*/ forbidden_region(cintrel, sOj, sOi, sLj, sLi)) {
                change |= sOj.size() > 0 ? 1 : -1;
            }
            if (change >= 0 && /*sLj.size() > 1 &&*/ forbidden_region(cintrel + 13, sOj, sOi, sLj, sLi)) {
                change |= sLj.size() > 0 ? 1 : -1;
            }
//        } while (change > 0);
        return change;
    }

}
