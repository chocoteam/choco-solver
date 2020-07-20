/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2020, IMT Atlantique. All rights reserved.
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

    private boolean explainForallDiffForall(ExplanationForSignedClause e, IntVar pivot, IntStream Indexes) {
        final boolean[] flag = {false};
        int[] indices = Indexes.toArray();
        IntIterableRangeSet union = IntStream
                .of(indices)
                .mapToObj(i -> e.readDom(vars[i]))
                .collect(IntIterableRangeSet::new,
                        IntIterableRangeSet::addAll,
                        IntIterableRangeSet::addAll);
        IntStream
                .of(indices)
                .mapToObj(i -> vars[i])
                .forEach(v -> {
                    IntIterableRangeSet dom = e.universe();
                    dom.removeAll(union);
                    flag[0] |= !dom.isEmpty();
                    v.unionLit(dom, e);
                });
        return flag[0];
    }
    private void explainDiffForalliForallt(ExplanationForSignedClause e, int[] indexes) {
        for (int i : indexes)  {
            vars[i].unionLit(e.domain(vars[i]),e);
        }
    }
    private void explainDiffForalliForalltDifft(ExplanationForSignedClause e, int[] indexes, int t) {
        for (int i : indexes)  {
            IntIterableRangeSet set = e.domain(vars[i]);
            set.remove(t);
            vars[i].unionLit(set,e);
        }
    }//TODO ca ressemble a de l'emnsembliste ca ???
    private void explainEquaForalliForalltDifft(ExplanationForSignedClause e, int[] indexes, int t) {
        for (int i : indexes)  {
            IntIterableRangeSet set = e.complement(vars[i]);
            set.add(t);
            vars[i].unionLit(set,e);
        }
     }

    private void explainEquaForalliForallt(ExplanationForSignedClause e, int[] indexes) {
        for (int i : indexes)  {
            vars[i].unionLit(e.complement(vars[i]),e);
        }
    }

    @Override
    public void explain(int p, ExplanationForSignedClause e) {
        IntVar pivot = e.readVar(p);
        int[] X = IntStream.rangeClosed(0, vars.length - 2).filter(i->vars[i]!=pivot).toArray();

        switch (e.readMask(p)) {
            case 4://DECUPP
                explainDiffForalliForallt(e, X);
                pivot.intersectLit(e.domain(pivot), e);
                break;
            case 8://INSTANTIATE
                int t = e.domain(pivot).min();
                explainDiffForalliForalltDifft(e, X, t);
                //explainEquaForalliForalltDifft(e, X, t);
                pivot.intersectLit(e.domain(pivot), e);
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
