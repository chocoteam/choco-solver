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
import org.chocosolver.solver.variables.ranges.IntIterableSet;
import org.chocosolver.solver.variables.ranges.IntIterableSetUtils;

/**
 * Project: choco.
 *
 * @author Charles Prud'homme, Mats Carlsson
 * @since 08/01/2016.
 */
public class AllenRelationMats extends AllenRelation{



    /**
     * Allen instructions.
     * Define all possible actions needed to compute forbidden regions.
     */
    @SuppressWarnings("JavaDoc")
    enum ai {
        AI_LI_EQ_LJM1,  // 1
        AI_LI_EQ_LJ,
        AI_LI_EQ_LJP1,
        AI_LI_LT_LJM1,
        AI_LI_LT_LJ,    // 5
        AI_LI_LT_LJP1,
        AI_LI_LT_LJP2,
        AI_LI_GT_LJM2,
        AI_LI_GT_LJP1,
        AI_LI_GT_LJ,    // 10
        AI_LI_GT_LJM1,
        AI_LI_NE_LJM1,
        AI_LI_NE_LJ,
        AI_LI_NE_LJP1,
        AI_LI_EQ_1,     // 15
        AI_LI_GT_1,
        AI_LJ_EQ_1,
        AI_LJ_GT_1,
        AI_LI_OR_LJ_EQ_1,
        AI_ZERO,        // 20
        AI_INF,
        AI_SUP,
        AI_PLUS_MINOI,
        AI_PLUS_MAXOI,
        AI_PLUS_MINOJ,  // 25
        AI_PLUS_MAXOJ,
        AI_PLUS_MINLI,
        AI_PLUS_MAXLI,
        AI_PLUS_MINLJ,
        AI_PLUS_MAXLJ,
        AI_MINUS_MINOI, // 30
        AI_MINUS_MAXOI,
        AI_MINUS_MINOJ,
        AI_MINUS_MAXOJ,
        AI_MINUS_MINLI,
        AI_MINUS_MAXLI, // 35
        AI_MINUS_MINLJ,
        AI_MINUS_MAXLJ,
        AI_PLUS_1,
        AI_PLUS_2,
        AI_PLUS_3,      // 40
        AI_PLUS_4,
        AI_MINUS_1,
        AI_MINUS_2,
        AI_MINUS_3,     // 45
        AI_MINUS_4,
        AI_INTERVAL,
        AI_BEGIN_DISJUNCT,
        AI_END_DISJUNCT,
        AI_COMPLEMENT,  //50
        AI_DOMOI,
        AI_DOMOJ,
        AI_DOMLI,
        AI_DOMLJ,
        AI_SETPLUS,     // 55
        AI_SETMINUS,
        AI_FROM_ONE,
        AI_MIN_GE_ONE,
        AI_ATLEAST_ONE,
        AI_SUB_FROM_MAXLJ, // 60
        AI_ADD_TO_MINLJ
    }

    /**
     * For each relation involving an origin, sequence of actions to execute to compute forbidden regions
     */
    static ai[][] sequence_o = new ai[][]{
            /*noop*/ new ai[]{},
            /* [b] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_ZERO, ai.AI_PLUS_MAXOJ, ai.AI_MINUS_MINLI, ai.AI_SUP, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [bi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_INF, ai.AI_ZERO, ai.AI_PLUS_MINOJ, ai.AI_PLUS_MINLJ, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [d] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_ZERO, ai.AI_PLUS_MAXLJ, ai.AI_MINUS_MINLI, ai.AI_MINUS_1, ai.AI_FROM_ONE, ai.AI_SETPLUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [di] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_ZERO, ai.AI_PLUS_MAXLI, ai.AI_MINUS_MINLJ, ai.AI_MINUS_1, ai.AI_FROM_ONE, ai.AI_SETMINUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [e] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [f] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMLJ, ai.AI_DOMLI, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_DOMOJ, ai.AI_SETPLUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [fi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMLI, ai.AI_DOMLJ, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_SETMINUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [m] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMLI, ai.AI_SETMINUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [mi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMLJ, ai.AI_SETPLUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [o] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMLI, ai.AI_ZERO, ai.AI_PLUS_MAXLJ, ai.AI_MINUS_1, ai.AI_FROM_ONE, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_SETMINUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [oi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMLJ, ai.AI_ZERO, ai.AI_PLUS_MAXLI, ai.AI_MINUS_1, ai.AI_FROM_ONE, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_SETPLUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [s] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [si] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
    };

    /**
     * For each relation involving an length, sequence of actions to execute to compute forbidden regions
     */
    static ai[][] sequence_l = new ai[][]{
            /*noop*/ new ai[]{},
            /* [b] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_ZERO, ai.AI_PLUS_MAXOJ, ai.AI_MINUS_MINOI, ai.AI_SUP, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [bi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_END_DISJUNCT},
            /* [d] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOI, ai.AI_DOMOJ, ai.AI_SETMINUS, ai.AI_MIN_GE_ONE, ai.AI_SUB_FROM_MAXLJ, ai.AI_SUP, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [di] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_INF, ai.AI_DOMOJ, ai.AI_DOMOI, ai.AI_SETMINUS, ai.AI_MIN_GE_ONE, ai.AI_ADD_TO_MINLJ, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [e] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMLJ, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [f] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMLJ, ai.AI_DOMOI, ai.AI_DOMOJ, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_SETMINUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [fi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMOI, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_DOMLJ, ai.AI_SETPLUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [m] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMOI, ai.AI_SETMINUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [mi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_END_DISJUNCT},
            /* [o] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_DOMOJ, ai.AI_DOMOI, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_ZERO, ai.AI_PLUS_MAXLJ, ai.AI_MINUS_1, ai.AI_FROM_ONE, ai.AI_SETPLUS, ai.AI_COMPLEMENT, ai.AI_END_DISJUNCT},
            /* [oi] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_INF, ai.AI_DOMLJ, ai.AI_DOMOI, ai.AI_DOMOJ, ai.AI_SETMINUS, ai.AI_ATLEAST_ONE, ai.AI_SETMINUS, ai.AI_MIN_GE_ONE, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [s] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_ZERO, ai.AI_PLUS_MAXLJ, ai.AI_SUP, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
            /* [si] */ new ai[]{ai.AI_BEGIN_DISJUNCT, ai.AI_INF, ai.AI_ZERO, ai.AI_PLUS_MINLJ, ai.AI_INTERVAL, ai.AI_END_DISJUNCT},
    };

    /**
     * Immutable singleton set to lowest lower bound
     */
    protected static final IntIterableRangeSet SET_INF;
    /**
     * Lowest lower bound
     */
    protected static final int INF = -Character.MAX_VALUE;
    /**
     * Immutable singleton set uppermost upper bound
     */
    protected static final IntIterableRangeSet SET_SUP;
    /**
     * Lowest lower bound
     */
    protected static final int SUP = Character.MAX_VALUE;

    /**
     * Immutable empty set
     */
    protected static final IntIterableRangeSet EMPTY_SET = new IntIterableRangeSet(){
        @Override
        public boolean add(int e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int... values) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(IntIterableSet set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(IntIterableSet set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(int e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(IntIterableSet set) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeBetween(int f, int t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void plus(int x) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void minus(int x) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void times(int x) {
            throw new UnsupportedOperationException();
        }
    };


    static {
        SET_INF = new IntIterableRangeSet();
        SET_INF.setOffset(INF);
        SET_INF.add(INF);
        SET_SUP = new IntIterableRangeSet();
        SET_SUP.setOffset(SUP);
        SET_SUP.add(SUP);
    }

    /**
     * Set up this Allen relation filtering algorithm.
     *
     * @param Rel   integer variable (domain should not exceed [1,13])
     * @param Oi    origin of the first interval
     * @param Li    length of the first interval
     * @param Oj    origin of the second interval
     * @param Lj    length of th second interval
     * @param cause master propagator which calls this
     */
    public AllenRelationMats(IntVar Rel, IntVar Oi, IntVar Li, IntVar Oj, IntVar Lj, ICause cause) {
        super(Rel, Oi, Li, Oj, Lj, cause);
    }

    /**
     * @param a a singleton
     * @param b a singleton
     * @return the set a..b
     */
    private static IntIterableRangeSet fd_interval(int a, int b) {
        if (a > b) {
            a = a ^ b ^ (b = a);
        }
        return new IntIterableRangeSet(a, b);
    }

    /**
     * @param set a set of ints
     * @param a   an int
     * @param b   an int
     * @return return the union of set and a..b
     */
    private static IntIterableRangeSet fd_union_interval(IntIterableRangeSet set, int a, int b) {
        IntIterableRangeSet t = new IntIterableRangeSet(a, b);
        t.addAll(set);
        return t;
    }

    /**
     * @param set a set of ints
     * @param x   a singleton
     * @return return the complement of set (wrt to INF..SUP)
     */
    private static IntIterableRangeSet fd_successor(IntIterableRangeSet set, int x) {
        int succ = set.nextValue(x);
        if(succ == Integer.MAX_VALUE){
            succ = SUP;
        }
        return new IntIterableRangeSet(succ);
    }


    private static boolean FDle(IntIterableRangeSet lb, IntIterableRangeSet ub) {
        assert lb.size() == 1;
        assert ub.size() == 1;
        return lb.first() <= ub.first();
    }

    private static boolean Tlt(IntIterableRangeSet cur, int a) {
        assert cur.size() == 1;
        return cur.first() < a;
    }

    /**
     * Remove <i>integers</i> from domain of var
     *
     * @param dvar     an integer variable
     * @param integers set of integers
     * @return > 0 if domain changed, 0 if nothing happens, < 0 if domain wiped out.
     */
    private static int dvar_prune_set(IntIterableRangeSet dvar, IntIterableRangeSet integers) {
        if (dvar.removeAll(integers)) {
            return dvar.size() == 0 ? -1 : 1;
        }
        return 0; // nothing happened
    }

    /**
     * Compute the forbidden region for Oi and Li wrt to Oj and Lj and relation pc
     *
     * @param sequence encode AI relations
     * @param sOi      origin of first interval (set of integers)
     * @param sOj      origin of second interval (set of integers)
     * @param sLi      length of first interval (set of integers)
     * @param sLj      length of seconf interval (set of integers)
     * @return a forbidden region
     */
    protected static IntIterableRangeSet forbidden_region(ai[] sequence,
                                                          IntIterableRangeSet sOi, IntIterableRangeSet sOj,
                                                          IntIterableRangeSet sLi, IntIterableRangeSet sLj) {
        IntIterableRangeSet[] stack = new IntIterableRangeSet[10];
        int minoi = sOi.first();
        int maxoi = sOi.last();
        int minoj = sOj.first();
        int maxoj = sOj.last();
        int minli = sLi.first();
        int maxli = sLi.last();
        int minlj = sLj.first();
        int maxlj = sLj.last();
        int tos = 0;
        IntIterableRangeSet lb, ub, cur;

        stack[tos++] = fd_interval(INF, SUP); // becomes intersection of FR
        stack[tos++] = EMPTY_SET; // becomes next FR
        for (int pc = 0; pc < sequence.length; pc++)
            switch (sequence[pc]) {
                case AI_LI_EQ_LJM1:
                case AI_LI_EQ_LJ:
                case AI_LI_EQ_LJP1:
                case AI_LI_LT_LJM1:
                case AI_LI_LT_LJ:
                case AI_LI_LT_LJP1:
                case AI_LI_LT_LJP2:
                case AI_LI_GT_LJM2:
                case AI_LI_GT_LJM1:
                case AI_LI_GT_LJ:
                case AI_LI_GT_LJP1:
                case AI_LI_NE_LJM1:
                case AI_LI_NE_LJ:
                case AI_LI_NE_LJP1:
                case AI_LI_EQ_1:
                case AI_LI_GT_1:
                case AI_LJ_EQ_1:
                case AI_LJ_GT_1:
                case AI_LI_OR_LJ_EQ_1:
                    assert false;
                    break;
                case AI_ZERO:
                    stack[tos++] = new IntIterableRangeSet(0);
                    break;
                case AI_INF:
                    stack[tos++] = SET_INF;
                    break;
                case AI_SUP:
                    stack[tos++] = SET_SUP;
                    break;
                case AI_PLUS_MINOI:
                    stack[tos - 1].plus(minoi);
                    break;
                case AI_PLUS_MAXOI:
                    stack[tos - 1].plus(maxoi);
                    break;
                case AI_PLUS_MINOJ:
                    stack[tos - 1].plus(minoj);
                    break;
                case AI_PLUS_MAXOJ:
                    stack[tos - 1].plus(maxoj);
                    break;
                case AI_PLUS_MINLI:
                    stack[tos - 1].plus(minli);
                    break;
                case AI_PLUS_MAXLI:
                    stack[tos - 1].plus(maxli);
                    break;
                case AI_PLUS_MINLJ:
                    stack[tos - 1].plus(minlj);
                    break;
                case AI_PLUS_MAXLJ:
                    stack[tos - 1].plus(maxlj);
                    break;
                case AI_MINUS_MINOI:
                    stack[tos - 1].minus(minoi);
                    break;
                case AI_MINUS_MAXOI:
                    stack[tos - 1].minus(maxoi);
                    break;
                case AI_MINUS_MINOJ:
                    stack[tos - 1].minus(minoj);
                    break;
                case AI_MINUS_MAXOJ:
                    stack[tos - 1].minus(maxoj);
                    break;
                case AI_MINUS_MINLI:
                    stack[tos - 1].minus(minli);
                    break;
                case AI_MINUS_MAXLI:
                    stack[tos - 1].minus(maxli);
                    break;
                case AI_MINUS_MINLJ:
                    stack[tos - 1].minus(minlj);
                    break;
                case AI_MINUS_MAXLJ:
                    stack[tos - 1].minus(maxlj);
                    break;
                case AI_PLUS_1:
                    stack[tos - 1].plus(1);
                    break;
                case AI_PLUS_2:
                    stack[tos - 1].plus(2);
                    break;
                case AI_PLUS_3:
                    stack[tos - 1].plus(3);
                    break;
                case AI_PLUS_4:
                    stack[tos - 1].plus(4);
                    break;
                case AI_MINUS_1:
                    stack[tos - 1].minus(1);
                    break;
                case AI_MINUS_2:
                    stack[tos - 1].minus(2);
                    break;
                case AI_MINUS_3:
                    stack[tos - 1].minus(3);
                    break;
                case AI_MINUS_4:
                    stack[tos - 1].minus(4);
                    break;
                case AI_INTERVAL:
                    ub = stack[--tos];
                    lb = stack[--tos];
                    assert lb.size() == 1 && ub.size() == 1;
                    if (FDle(lb, ub))
                        stack[tos - 1] = fd_union_interval(stack[tos - 1], lb.first(), ub.first());
                    break;
                case AI_BEGIN_DISJUNCT:	/* noop */
                    break;
                case AI_END_DISJUNCT:
                    cur = stack[--tos];
                    stack[tos - 1] = IntIterableSetUtils.intersection(stack[tos - 1], cur);
                    stack[tos++] = EMPTY_SET;
                    break;
                case AI_COMPLEMENT:
                    cur = IntIterableSetUtils.complement(stack[--tos], INF, SUP);
                    stack[tos - 1] = IntIterableSetUtils.union(stack[tos - 1], cur);
                    break;
                case AI_DOMOI:
                    stack[tos++] = sOi;
                    break;
                case AI_DOMOJ:
                    stack[tos++] = sOj;
                    break;
                case AI_DOMLI:
                    stack[tos++] = sLi;
                    break;
                case AI_DOMLJ:
                    stack[tos++] = sLj;
                    break;
                case AI_FROM_ONE:
                    cur = stack[--tos];
                    if (Tlt(cur, 1))
                        cur = EMPTY_SET;
                    else {
                        assert cur.size() == 1;
                        cur = fd_interval(1, cur.first());
                    }
                    stack[tos++] = cur;
                    break;
                case AI_MIN_GE_ONE:
                    stack[tos - 1] = fd_successor(stack[tos - 1], 0);
                    break;
                case AI_ATLEAST_ONE:
                    stack[tos - 1] = IntIterableSetUtils.intersection(stack[tos - 1], fd_interval(1, SUP));
                    break;
                case AI_SUB_FROM_MAXLJ:
                    if (stack[tos - 1] == SET_SUP)
                        stack[tos - 1] = SET_INF;
                    else
                        stack[tos - 1] = new IntIterableRangeSet(maxlj - stack[tos - 1].first());
                    break;
                case AI_ADD_TO_MINLJ:
                    if (stack[tos - 1] == SET_SUP)
                        stack[tos - 1] = SET_SUP;
                    else
//                        stack[tos - 1] = MakeSmall(minlj + GetSmall(stack[tos - 1]));
                        stack[tos - 1] = new IntIterableRangeSet(minlj + stack[tos - 1].first());
                    break;
                case AI_SETPLUS:
                    cur = stack[--tos];
                    stack[tos - 1] = IntIterableSetUtils.plus(stack[tos - 1], cur);
                    break;
                case AI_SETMINUS:
                    cur = stack[--tos];
                    stack[tos - 1] = IntIterableSetUtils.minus(stack[tos - 1], cur);
                    break;
            }
        return stack[0];
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
            change |= dvar_prune_set(sOi, forbidden_region(sequence_o[intrel], sOi, sOj, sLi, sLj));
            if (change >= 0 /*&& sLi.size() > 1*/) {
                change |= dvar_prune_set(sLi, forbidden_region(sequence_l[intrel], sOi, sOj, sLi, sLj));
            }
            if (change >= 0 /*&& sOj.size() > 1*/) {
                change |= dvar_prune_set(sOj, forbidden_region(sequence_o[cintrel], sOj, sOi, sLj, sLi));
            }
            if (change >= 0 /*&& sLj.size() > 1*/) {
                change |= dvar_prune_set(sLj, forbidden_region(sequence_l[cintrel], sOj, sOi, sLj, sLi));
            }
//        } while (change > 0);
        return change;
    }


}
