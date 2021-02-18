/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.nvalue;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.ExplanationForSignedClause;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

import java.util.stream.IntStream;

import static org.chocosolver.solver.constraints.PropagatorPriority.QUADRATIC;
import static org.chocosolver.util.tools.ArrayUtils.concat;

/**
 * Propagator for the atMostNValues constraint
 * The number of distinct values in the set of variables vars is at most equal to nValues
 * No level of consistency but better than BC in general (for enumerated domains with holes)
 *
 * @author Jean-Guillaume Fages
 */
public class PropAtLeastNValues extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private int[] concernedValues;
    private int n;
    private int[] mate;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Propagator for the NValues constraint
     * The number of distinct values among concerned values in the set of variables vars is exactly equal to nValues
     * No level of consistency for the filtering
     *
     * @param variables       array of integer variables
     * @param concernedValues will be sorted!
     * @param nValues         integer variable
     */
    public PropAtLeastNValues(IntVar[] variables, int[] concernedValues, IntVar nValues) {
        super(concat(variables, nValues), QUADRATIC, false);
        n = variables.length;
        this.concernedValues = concernedValues;
        mate = new int[concernedValues.length];
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        vars[n].updateUpperBound(n, this);
        int count = 0;
        int countMax = 0;
        for (int i = concernedValues.length - 1; i >= 0; i--) {
            boolean possible = false;
            boolean mandatory = false;
            mate[i] = -1;
            int value = concernedValues[i];
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(value)) {
                    possible = true;
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        mate[i] = -2;
                        break;
                    } else {
                        if (mate[i] == -1) {
                            mate[i] = v;
                        } else {
                            mate[i] = -2;
                        }
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                count++;
            }
        }
        // filtering cardinality variable
        vars[n].updateUpperBound(countMax, this);
        // filtering decision variables
        boolean again = false;
        if (count < countMax && countMax == vars[n].getLB()) {
            for (int i = concernedValues.length - 1; i >= 0; i--) {
                if (mate[i] >= 0) {
                    if (vars[mate[i]].instantiateTo(concernedValues[i], this)) {
                        again = true;
                    }
                }
            }
            if (!again) {
                int nbInst = 0;
                for (int i = 0; i < n; i++) {
                    if (vars[i].isInstantiated()) {
                        nbInst++;
                    }
                }
                // remove used variables when alldiff is required over uninstantiated variables
                if (n - nbInst == countMax - count) {
                    for (int i = concernedValues.length - 1; i >= 0; i--) {
                        boolean mandatory = false;
                        int value = concernedValues[i];
                        for (int v = 0; v < n; v++) {
                            if (vars[v].isInstantiatedTo(value)) {
                                mandatory = true;
                                break;
                            }
                        }
                        if (mandatory) {
                            for (int v = 0; v < n; v++) {
                                if (!vars[v].isInstantiated()) {
                                    if (vars[v].removeValue(value, this)) {
                                        again = true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (count >= vars[n].getUB()) {
            setPassive();
        } else if (again) {
            propagate(0);// fix point is required as not all possible values add a mate
        }
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        int countMin = 0;
        int countMax = 0;
        for (int i = concernedValues.length - 1; i >= 0; i--) {
            boolean possible = false;
            boolean mandatory = false;
            for (int v = 0; v < n; v++) {
                if (vars[v].contains(concernedValues[i])) {
                    possible = true;
                    if (vars[v].isInstantiated()) {
                        mandatory = true;
                        break;
                    }
                }
            }
            if (possible) {
                countMax++;
            }
            if (mandatory) {
                countMin++;
            }
        }
        if (countMin >= vars[n].getUB()) {
            return ESat.TRUE;
        }
        if (countMax < vars[n].getLB()) {
            return ESat.FALSE;
        }
        return ESat.UNDEFINED;
    }
    /**
     * Find in the implication graph and add to the explanation all the remove value events (the real events added are inverted because only disjunctions are allowed for explanation)
     */
    private void explainDiffForalliForallt(ExplanationForSignedClause e, int[] indexes) {
        for (int i : indexes)  {
            for(int t : e.root(vars[i])){
                if (!e.domain(vars[i]).contains(t)) {
                    vars[i].unionLit(t,e);
                }
            }//vars[i].unionLit(e.complement(vars[i]),e);
        }
    }
    /**
     * Find in the implication graph and add to the explanation all the remove value events except those on value t (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param t exception value
     */
    private void explainDiffForalliForalltDifft(ExplanationForSignedClause e, int[] indexes, int t) {
        for (int i : indexes)  {
            for(int tt : e.root(vars[i])){
                if (!e.domain(vars[i]).contains(tt)&&t!=tt) {
                    vars[i].unionLit(tt,e);
                }
            }//vars[i].unionLit(e.complement(vars[i]),e);
        }
    }
    /**
     * Find in the implication graph and add in the explanation remove value t events (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param t value
     */
    private void explainDiffForallit(ExplanationForSignedClause e, int[] indexes, int t) {
        for (int i : indexes)  {
            if (!e.domain(vars[i]).contains(t)) {
                vars[i].unionLit(t,e);
            }
        }
    }
    /**
     * Find in the implication graph and add to the explanation all the instantiate events except those on value t (the real events added are inverted because only disjunctions are allowed for explanation)
     * @param t exception value
     */
    private void explainEquaForalliForalltDifft(ExplanationForSignedClause e, int[] indexes, int t) {
        for (int i : indexes)  {
            for(int tt : e.root(vars[i])){
                if (e.domain(vars[i]).contains(tt)&&t!=tt) {
                    vars[i].intersectLit(e.setDiffVal(tt),e);
                }
            }
        }
     }
    /**
     * Find in the implication graph and add in the explanation all instantiation events (the real events added are inverted because only disjunctions are allowed for explanation)
     */
    private void explainEquaForalliForallt(ExplanationForSignedClause e, int[] indexes) {
        for (int i : indexes) {
            for (int t : e.root(vars[i])) {
                if (e.domain(vars[i]).contains(t)) {
                    vars[i].intersectLit(e.setDiffVal(t), e);
                }
            }
        }
    }
    /**
     * Detect and explain the event at pivot variable p
     * @param p pivot variable
     */
    @Override
    public void explain(int p, ExplanationForSignedClause e) {
        IntVar pivot = e.readVar(p);
        int[] X = IntStream.rangeClosed(0, vars.length - 2).filter(i->vars[i]!=pivot).toArray();
        switch (e.readMask(p)) {
            case 4://DECUPP
                explainDiffForalliForallt(e, X);
                pivot.intersectLit(IntIterableRangeSet.MIN, e.domain(pivot).max(), e);
                break;
            case 8://INSTANTIATE
                assert e.readDom(p).size()==1;
                int t = e.readDom(p).min();
                explainDiffForallit(e, X, t);
                explainDiffForalliForalltDifft(e, X, t);
                explainEquaForalliForalltDifft(e, X, t);
                vars[vars.length - 1].unionLit(e.complement(vars[vars.length - 1]),e);
                IntIterableRangeSet set = e.complement(pivot);
                set.add(t);
                pivot.intersectLit(set, e);
                break;
            case 2://INCLOW
            case 1://REMOVE
            case 0://VOID
            case 6://BOUND inclow+decup
            default:
                throw new UnsupportedOperationException("Unknown event type explanation");
        }
    }
}
