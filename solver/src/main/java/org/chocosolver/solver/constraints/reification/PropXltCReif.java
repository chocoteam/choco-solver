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

/**
 * A propagator dedicated to express in a compact way: (x < c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXltCReif extends Propagator<IntVar> {

    IntVar var;
    int cste;
    BoolVar r;

    public PropXltCReif(IntVar x, int c, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false, true);
        this.cste = c;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            var.updateUpperBound(cste - 1, this);
            setPassive();
        } else if (r.getUB() == 0) {
            var.updateLowerBound(cste, this);
            setPassive();
        } else {
            if (var.getUB() < cste) {
                r.setToTrue(this);
                setPassive();
            } else if (var.getLB() >= cste) {
                r.setToFalse(this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(var.getUB() < cste);
            }else{
                return ESat.eval(var.getLB() >= cste);
            }
        }
        return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     *
     * Premise: (x < c) &hArr; b
     * <p>
     * 4 cases here (only cases that triggered filtering are reported):
     * <ol type="a">
     *  <li>
     *  <pre>
     *      (b = 1 &and; x &isin; (-&infin;, +&infin;)) &rarr; x &isin; (-&infin;, c - 1]
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; (-&infin;, c - 1])
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; (-&infin;,c - 1]) &rarr; b = 1
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; [c, +&infin;))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 0 &and; x &isin; (-&infin;, +&infin;)) &rarr; x &isin; [c, +&infin;)
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; [c, +&infin;))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; [c, +&infin;)) &rarr; b = 0
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; (-&infin;, c - 1])
     *  </pre>
     *  </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        if (vars[1].isInstantiatedTo(1)) { // b is true and X < c holds
            if (pivot == vars[1]) { // b is the pivot
                vars[1].intersectLit(1, explanation);
                IntIterableRangeSet dom0 = explanation.complement(vars[0]);
                dom0.retainBetween(cste, IntIterableRangeSet.MAX);
                vars[0].unionLit(dom0, explanation);
            } else if (pivot == vars[0]) { // x is the pivot
                vars[1].unionLit(0, explanation);
                vars[0].intersectLit(IntIterableRangeSet.MIN, cste - 1, explanation);
            }
        } else if (vars[1].isInstantiatedTo(0)) {
            if (pivot == vars[1]) { // b is the pivot
                vars[1].intersectLit(0, explanation);
                IntIterableRangeSet dom0 = explanation.complement(vars[0]);
                dom0.retainBetween(IntIterableRangeSet.MIN, cste - 1);
                vars[0].unionLit(dom0, explanation);
            } else if (pivot == vars[0]) { // x is the pivot, case e. in javadoc
                vars[1].unionLit(1, explanation);
                vars[0].intersectLit(cste, IntIterableRangeSet.MAX, explanation);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return "(" + var.getName() +" < " + cste + ") <=> "+r.getName();
    }
}
