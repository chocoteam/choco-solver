/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
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
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSetUtils;

/**
 * A propagator dedicated to express in a compact way: (x = c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXinSReif extends Propagator<IntVar> {

    IntVar var;
    IntIterableRangeSet set;
    BoolVar r;

    public PropXinSReif(IntVar x, IntIterableRangeSet set, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false, true);
        this.set = set;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            var.removeAllValuesBut(set, this);
            setPassive();
        } else if (r.getUB() == 0) {
            if (var.removeValues(set, this) || !set.intersect(var)) {
                setPassive();
            }
        } else {
            if (IntIterableSetUtils.includedIn(var, set)) {
                r.setToTrue(this);
                setPassive();
            } else if (!set.intersect(var)) {
                r.setToFalse(this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(IntIterableSetUtils.includedIn(var, set));
            }else{
                return ESat.eval(!set.intersect(var));
            }
        }
        return ESat.UNDEFINED;
    }


    /**
     * @implSpec
     *
     * Premise: (x &isin; S) &hArr; b
     * <p>
     * 4 cases here (only cases that triggered filtering are reported):
     * <ol type="a">
     *  <li>
     *  <pre>
     *      (b = 1 &and; x &isin; (-&infin;, +&infin;)) &rarr; x &isin; S
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; S)
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; S) &rarr; b = 1
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; (U \ S))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 0 &and; x &isin; (-&infin;, +&infin;)) &rarr; x &isin; (U \ S)
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; (U \ S))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; (U \ S)) &rarr; b = 0
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; S)
     *  </pre>
     *  </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        if (vars[1].isInstantiatedTo(1)) { // b is true and X > c holds
            if (pivot == vars[1]) { // b is the pivot
                vars[1].intersectLit(1, explanation);
                IntIterableRangeSet set0 = explanation.universe();
                set0.removeAll(this.set);
                vars[0].unionLit(set0, explanation);
            } else if (pivot == vars[0]) { // x is the pivot
                vars[1].unionLit(0, explanation);
                vars[0].intersectLit(explanation.empty().copyFrom(set), explanation);
            }
        } else if (vars[1].isInstantiatedTo(0)) {
            if (pivot == vars[1]) { // b is the pivot
                vars[1].intersectLit(0, explanation);
                vars[0].unionLit(explanation.empty().copyFrom(set), explanation);
            } else if (pivot == vars[0]) { // x is the pivot, case e. in javadoc
                vars[1].unionLit(1, explanation);
                IntIterableRangeSet set0 = explanation.universe();
                set0.removeAll(this.set);
                vars[0].intersectLit(set0, explanation);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return "(" + var.getName() +" ∈ " + set + ") <=> "+r.getName();
    }

}
