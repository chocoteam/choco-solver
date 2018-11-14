/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2018, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license. See LICENSE file in the project root for full license
 * information.
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
 * A propagator dedicated to express in a compact way: (x = y + c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXeqYCReif extends Propagator<IntVar> {

    int cste;

    public PropXeqYCReif(IntVar x, IntVar y, int c, BoolVar b) {
        super(new IntVar[]{x, y, b}, PropagatorPriority.TERNARY, false);
        this.cste = c;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (vars[2].getLB() == 1) {
            if (vars[0].isInstantiated()) {
                vars[1].instantiateTo(vars[0].getValue() - cste, this);
                setPassive();
            } else if (vars[1].isInstantiated()) {
                vars[0].instantiateTo(vars[1].getValue() + cste, this);
                setPassive();
            }
        } else if (vars[2].getUB() == 0) {
            if (vars[0].isInstantiated()) {
                if (vars[1].removeValue(vars[0].getValue() - cste, this) || !vars[1].contains(vars[0].getValue() - cste)) {
                    setPassive();
                }
            } else if (vars[1].isInstantiated()) {
                if (vars[0].removeValue(vars[1].getValue() + cste, this) || !vars[0].contains(vars[1].getValue() + cste)) {
                    setPassive();
                }
            }
        } else {
            if (vars[0].isInstantiated()) {
                if (vars[1].isInstantiated()) {
                    if (vars[0].getValue() == vars[1].getValue() + cste) {
                        vars[2].instantiateTo(1, this);
                    } else {
                        vars[2].instantiateTo(0, this);
                    }
                    setPassive();
                } else if (!vars[1].contains(vars[0].getValue() + cste)) {
                    vars[2].instantiateTo(0, this);
                    setPassive();
                }
            } else {
                if (vars[1].isInstantiated()) {
                    if (!vars[0].contains(vars[1].getValue() - cste)) {
                        vars[2].instantiateTo(0, this);
                        setPassive();
                    }
                } else {
                    if (vars[0].getLB() > vars[1].getUB() + cste
                            || vars[0].getUB() < vars[1].getLB() + cste) {
                        vars[2].instantiateTo(0, this);
                        setPassive();
                    }
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            if (vars[2].isInstantiatedTo(1)) {
                return ESat.eval(vars[0].getValue() == vars[1].getValue() + cste);
            } else {
                return ESat.eval(vars[0].getValue() != vars[1].getValue() + cste);
            }
        }
        return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     *
     * Premise: (x = y + c) &hArr; b
     * <p>
     * 6 cases here (only cases that triggered filtering are reported):
     * <ol type="a">
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; {m} &and; y &isin; {n} &and; m = n + c) &rarr; b = 1
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; (U \ m) &or; y &isin; (U \ n)
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; {m} &and; y &isin; {n} &and; m &ne; n + c) &rarr; b = 1
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
     */
    @Override
    public void explain(ExplanationForSignedClause explanation,
                        ValueSortedMap<IntVar> front,
                        Implications ig, int p) {
        IntVar pivot = ig.getIntVarAt(p);
        IntIterableRangeSet tmp;
        if (vars[2].isInstantiatedTo(1)) { // b is true and X = Y + c holds
            if (pivot == vars[2]) { // b is the pivot
                explanation.addLiteral(vars[0], explanation.getComplementSet(vars[0]), false);
                explanation.addLiteral(vars[1], explanation.getComplementSet(vars[1]), false);
                explanation.addLiteral(vars[2], explanation.getFreeSet(1), true);
            } else if (pivot == vars[0]) { // x is the pivot
                IntIterableRangeSet dom0 = explanation.getRootSet(vars[0]);
                tmp = explanation.getSet(vars[1]);
                tmp.plus(cste);
                dom0.retainAll(tmp);
                explanation.addLiteral(vars[0], dom0, true);
                explanation.addLiteral(vars[1], explanation.getComplementSet(vars[1]), false);
                explanation.addLiteral(vars[2], explanation.getFreeSet(0), false);
            } else if (pivot == vars[1]) { // y is the pivot
                IntIterableRangeSet dom1 = explanation.getRootSet(vars[1]);
                tmp = explanation.getSet(vars[0]);
                tmp.minus(cste);
                dom1.retainAll(tmp);
                explanation.addLiteral(vars[0], explanation.getComplementSet(vars[0]), false);
                explanation.addLiteral(vars[1], dom1, true);
                explanation.addLiteral(vars[2], explanation.getFreeSet(0), false);
            }
        } else if (vars[2].isInstantiatedTo(0)) {
            if (pivot == vars[2]) { // b is the pivot
                IntIterableRangeSet dom0 = explanation.getComplementSet(vars[0]);
                tmp = explanation.getSet(vars[1]);
                tmp.plus(cste);
                dom0.retainAll(tmp);
                explanation.addLiteral(vars[0], dom0, false);
                explanation.returnSet(tmp);
                IntIterableRangeSet dom1 = explanation.getComplementSet(vars[1]);
                tmp = explanation.getSet(vars[0]);
                tmp.minus(cste);
                dom1.retainAll(tmp);
                explanation.addLiteral(vars[1], dom1, false);
                explanation.returnSet(tmp);
                explanation.addLiteral(vars[2], explanation.getFreeSet(0), true);
            } else if (pivot == vars[0]) { // x is the pivot
                IntIterableRangeSet dom1 = explanation.getComplementSet(vars[1]);
                explanation.addLiteral(vars[1], dom1, false);
                IntIterableRangeSet dom0 = explanation.getRootSet(vars[0]);
                tmp = explanation.getComplementSet(vars[1]);
                tmp.plus(cste);
                dom0.retainAll(tmp);
                explanation.addLiteral(vars[0], dom0, true);
                explanation.returnSet(tmp);
                explanation.addLiteral(vars[2], explanation.getFreeSet(1), false);
            } else if (pivot == vars[1]) { // y is the pivot
                IntIterableRangeSet dom0 = explanation.getComplementSet(vars[0]);
                explanation.addLiteral(vars[0], dom0, false);
                IntIterableRangeSet dom1 = explanation.getRootSet(vars[1]);
                tmp = explanation.getComplementSet(vars[0]);
                tmp.minus(cste);
                dom1.retainAll(tmp);
                explanation.addLiteral(vars[1], dom1, true);
                explanation.returnSet(tmp);
                explanation.addLiteral(vars[2], explanation.getFreeSet(1), false);
            }
        } else {
            throw new UnsupportedOperationException();
        }
//        super.explain(explanation, front, ig, p);
    }

    @Override
    public String toString() {
        return "(" + vars[0].getName() + " = " + vars[1].getName() + (cste !=0?" + "+cste:"")+") <=> " + vars[2].getName();
    }
}
