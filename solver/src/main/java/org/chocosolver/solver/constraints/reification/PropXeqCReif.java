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
 * A propagator dedicated to express in a compact way: (x = c) &hArr; b
 *
 * @author Charles Prud'homme
 * @since 03/05/2016.
 */
public class PropXeqCReif extends Propagator<IntVar> {

    IntVar var;
    int cste;
    BoolVar r;

    public PropXeqCReif(IntVar x, int c, BoolVar r) {
        super(new IntVar[]{x, r}, PropagatorPriority.BINARY, false);
        this.cste = c;
        this.var = x;
        this.r = r;
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (r.getLB() == 1) {
            var.instantiateTo(cste, this);
            setPassive();
        } else if (r.getUB() == 0) {
            if (var.removeValue(cste, this) || !var.contains(cste)) {
                setPassive();
            }
        } else {
            if (var.isInstantiatedTo(cste)) {
                r.setToTrue(this);
                setPassive();
            } else if (!var.contains(cste)) {
                r.setToFalse(this);
                setPassive();
            }
        }
    }

    @Override
    public ESat isEntailed() {
        if(isCompletelyInstantiated()){
            if(r.isInstantiatedTo(1)){
                return ESat.eval(var.contains(cste));
            }else{
                return ESat.eval(!var.contains(cste));
            }
        }
        return ESat.UNDEFINED;
    }

    /**
     * @implSpec
     *
     * Premise: (x = c) &hArr; b
     * <p>
     * 4 cases here (only cases that triggered filtering are reported):
     * <ol type="a">
     *  <li>
     *  <pre>
     *      (b = 1 &and; x &isin; (-&infin;, +&infin;)) &rarr; x &isin; {c}
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; {c})
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; {c}) &rarr; b = 1
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; (U \ c))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = 0 &and; x &isin; (-&infin;, +&infin;)) &rarr; x &isin; (U \ c)
     *  </pre>
     *  <pre>
     *      &hArr; (b = 1 &or; x &isin; (U \ c))
     *  </pre>
     *  </li>
     *  <li>
     *  <pre>
     *      (b = [0,1] &and; x &isin; (U \ c)) &rarr; b = 0
     *  </pre>
     *  <pre>
     *      &hArr; (b = 0 &or; x &isin; {c})
     *  </pre>
     *  </li>
     * </ol>
     * </p>
     */
    @Override
    public void explain(int p, ExplanationForSignedClause explanation) {
        IntVar pivot = explanation.readVar(p);
        if (vars[1].isInstantiatedTo(1)) { // b is true and X = c holds
            if (pivot == vars[1]) { // b is the pivot
                vars[1].intersectLit(1, explanation);
                IntIterableRangeSet dom0 = explanation.universe();
                dom0.remove(cste);
                vars[0].unionLit(dom0, explanation);
            } else if (pivot == vars[0]) { // x is the pivot
                vars[1].unionLit(0, explanation);
                vars[0].intersectLit(cste, explanation);
            }
        } else if (vars[1].isInstantiatedTo(0)) {
            if (pivot == vars[1]) { // b is the pivot
                vars[1].intersectLit(0, explanation);
                vars[0].unionLit(cste, explanation);
            } else if (pivot == vars[0]) { // x is the pivot, case e. in javadoc
                vars[1].unionLit(1, explanation);
                IntIterableRangeSet dom0 = explanation.universe();
                dom0.remove(cste);
                vars[0].intersectLit(dom0, explanation);
            }
        } else {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString() {
        return "(" + var.getName() +" = " + cste + ") <=> "+r.getName();
    }
}
