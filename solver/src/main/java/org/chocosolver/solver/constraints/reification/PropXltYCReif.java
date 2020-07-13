/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.reification;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.learn.Implications;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * A propagator dedicated to express in a compact way: (x < y + c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXltYCReif extends Propagator<IntVar> {

    int cste;

    public PropXltYCReif(IntVar x, IntVar y, int c, BoolVar r) {
        super(new IntVar[]{x, y, r}, PropagatorPriority.TERNARY, false);
        this.cste = c;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[2].getLB() == 1) {
            vars[0].updateUpperBound(vars[1].getUB() + cste - 1, this);
            vars[1].updateLowerBound(vars[0].getLB() - cste + 1, this);
            if (vars[0].getUB() < vars[1].getLB() + cste) {
                this.setPassive();
            }
        } else if (vars[2].getUB() == 0) {
            vars[0].updateLowerBound(vars[1].getLB() + cste, this);
            vars[1].updateUpperBound(vars[0].getUB() - cste, this);
            if (vars[0].getLB() >= vars[1].getUB() + cste) {
                setPassive();
            }
        } else {
            if (vars[0].getUB() < vars[1].getLB() + cste) {
                vars[2].instantiateTo(1, this);
                setPassive();
            } else if (vars[0].getLB() >= vars[1].getUB() + cste) {
                vars[2].instantiateTo(0, this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(vars[2].isInstantiatedTo(1)){
                return ESat.eval(vars[0].getValue() < vars[1].getValue() + cste);
            }else{
                return ESat.eval(vars[0].getValue() >= vars[1].getValue() + cste);
            }
        }
        return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     *
     * Premise: (x < y + c) &hArr; b
     * <p>
     * 6 cases here (only cases that triggered filtering are reported):
     * <ol type="a">
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; (-&infin;, m] &and; y &isin; [n, +&infin;) &and; m < n + c) &rarr; b = 1
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; [n + c, +&infin;) &or; y &isin; (-&infin;, n - 1])
     *  </pre>
     *  <pre>
     *      (alt.) (b = 1 &or; x &isin; [m + 1, +&infin;) &or; y &isin; (-&infin;, m - c])
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; [m, +&infin;) &and; y &isin; (-&infin;,n] &and; m &ge; n + c) &rarr; b = 0
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; (-&infin;, m - 1] &or; y &isin; [m - t + 1,+&infin;))
     *  </pre>
     *  <pre>
     *      (alt.) (b = 0 &or; x &isin; (-&infin;,n + c - 1] &or; y &isin; [n + 1, +&infin;))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 1 &and; x &isin; [m, +&infin;) &and; y &isin; (-&infin;, +&infin;)) &rarr; y &isin; [m - c + 1, +&infin;)
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; (-&infin;, m-1] &or; y &isin; [m - c + 1, +&infin;))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 1 &and; x &isin; (-&infin;, +&infin;) &and; y &isin; (-&infin;, n]) &rarr; x &isin; (-&infin;, n + c - 1]
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; (-&infin;, n + c - 1] &or; y &isin; [n + 1, +&infin;))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 0 &and; x &isin; (-&infin;,m] &and; y &isin; (-&infin;, +&infin;)) &rarr; y &isin; (-&infin;,m - c]
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; [m + 1, +&infin;) &or; y &isin; (-&infin;,m - c])
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 0 &and; x &isin; (-&infin;, +&infin;) &and; y &isin; [n, +&infin;)) &rarr; x &isin; [n + c, +&infin;)
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; [n + c, +&infin;) &or; y &isin; (-&infin;,n - 1])
     *  </pre>
     *  </li>
     * </ol>
     * </p>
     *
     *
     */
    @Override
    public void explain(ExplanationForSignedClause explanation,
                        ValueSortedMap<IntVar> front,
                        Implications ig, int p) {
        IntVar pivot = ig.getIntVarAt(p);
        IntIterableRangeSet set0, set1;
        set0 = explanation.universe();
        set1 = explanation.universe();
        if (vars[2].isInstantiatedTo(1)) { // b is true and X < Y + c holds
            if (pivot == vars[2]) { // b is the pivot, case a. in javadoc
                vars[2].intersectLit(1, explanation);
                // deal with alternatives
                if(front.getValue(vars[0]) > front.getValue(vars[1])){
                    int n = ig.getDomainAt(front.getValue(vars[1])).min();
                    set0.retainBetween(n+cste, IntIterableRangeSet.MAX);
                    vars[0].unionLit(set0, explanation);
                    set1.retainBetween(IntIterableRangeSet.MIN, n - 1);
                    vars[1].unionLit(set1, explanation);
                }else{
                    int m = ig.getDomainAt(front.getValue(vars[0])).max();
                    set0.retainBetween(m+1, IntIterableRangeSet.MAX);
                    vars[0].unionLit(set0, explanation);
                    set1.retainBetween(IntIterableRangeSet.MIN, m - cste);
                    vars[1].unionLit(set1, explanation);
                }
            } else if (pivot == vars[0]) { // x is the pivot, case d. in javadoc
                vars[2].unionLit(0, explanation);
                int n = ig.getDomainAt(front.getValue(vars[1])).max();
                set0.retainBetween(IntIterableRangeSet.MIN, n + cste - 1);
                vars[0].intersectLit(set0, explanation);
                set1.retainBetween(n + 1, IntIterableRangeSet.MAX);
                vars[1].unionLit(set1, explanation);
            } else if (pivot == vars[1]) { // y is the pivot, case c. in javadoc
                vars[2].unionLit(0, explanation);
                int m = ig.getDomainAt(front.getValue(vars[0])).min();
                set0.retainBetween(IntIterableRangeSet.MIN, m - 1);
                vars[0].unionLit(set0, explanation);
                set1.retainBetween(m - cste + 1, IntIterableRangeSet.MAX);
                vars[1].intersectLit(set1, explanation);
            }
        } else if (vars[2].isInstantiatedTo(0)) {
            if (pivot == vars[2]) { // b is the pivot, case b. in javadoc
                vars[2].intersectLit(0, explanation);
                // deal with alternatives
                if(front.getValue(vars[0]) > front.getValue(vars[1])){
                    int n = ig.getDomainAt(front.getValue(vars[1])).max();
                    set0.retainBetween(IntIterableRangeSet.MIN, n + cste - 1);
                    vars[0].unionLit(set0, explanation);
                    set1.retainBetween(n + 1, IntIterableRangeSet.MAX);
                    vars[1].unionLit(set1, explanation);
                }else{
                    int m = ig.getDomainAt(front.getValue(vars[0])).min();
                    set0.retainBetween(IntIterableRangeSet.MIN, m - 1);
                    vars[0].unionLit(set0, explanation);
                    set1.retainBetween(m - cste + 1, IntIterableRangeSet.MAX);
                    vars[1].unionLit(set1, explanation);
                }
            } else if (pivot == vars[0]) { // x is the pivot, case f. in javadoc
                vars[2].unionLit(1, explanation);
                int n = ig.getDomainAt(front.getValue(vars[1])).min();
                set0.retainBetween(n + cste, IntIterableRangeSet.MAX);
                vars[0].intersectLit(set0, explanation);
                set1.retainBetween(IntIterableRangeSet.MIN, n - 1);
                vars[1].unionLit(set1, explanation);
            } else if (pivot == vars[1]) { // y is the pivot, case e. in javadoc
                vars[2].unionLit(1, explanation);
                int m = ig.getDomainAt(front.getValue(vars[0])).max();
                set0.retainBetween(m + 1, IntIterableRangeSet.MAX);
                vars[0].unionLit(set0, explanation);
                set1.retainBetween(IntIterableRangeSet.MIN, m - cste);
                vars[1].intersectLit(set1, explanation);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return "(" + vars[0].getName() +" < " + vars[1].getName() + " + "+cste+") <=> "+vars[2].getName();
    }
}
