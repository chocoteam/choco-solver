/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropLargeGAC2001 extends PropLargeCSP<LargeRelation> {

    // Last valid supports Last(x_i, val) = supports( (blocks(i) + val) * size )

    private IStateInt[] supports;

    private int[] blocks;

    // Cardinality
    private int size;

    // offsets(i) = Min(x_i)
    private int[] offsets;

    // check if none of the tuple is trivially outside
    //the domains and if yes use a fast valid check
    //by avoiding checking the bounds
    private ValidityChecker valcheck;

    private final IntIterableBitSet vrms;

    private PropLargeGAC2001(IntVar[] vs, LargeRelation relation) {
        super(vs, relation);
        this.size = vs.length;
        this.blocks = new int[size];
        this.offsets = new int[size];

        int nbElt = 0;
        boolean allboolean = true;
        for (int i = 0; i < size; i++) {
            offsets[i] = vs[i].getLB();
            blocks[i] = nbElt;
            if ((vars[i].getTypeAndKind() & Variable.KIND) != Variable.BOOL) allboolean = false;
            if (!vars[i].hasEnumeratedDomain()) {
                throw new SolverException("GAC2001 can not be used with bound variables");
            } else nbElt += vars[i].getUB() - vars[i].getLB() + 1;
        }
        this.supports = new IStateInt[nbElt * size];
        IEnvironment env = vs[0].getEnvironment();
        for (int i = 0; i < supports.length; i++) {
            supports[i] = env.makeInt(Integer.MIN_VALUE);
        }
        if (allboolean)
            valcheck = new FastBooleanValidityChecker(size, vars);
        else
            valcheck = new ValidityChecker(size, vars);

        vrms = new IntIterableBitSet();
    }

    public PropLargeGAC2001(IntVar[] vs, Tuples tuples) {
        this(vs, RelationFactory.makeLargeRelation(tuples, vs));
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            for (int i = 0; i < vars.length; i++) {
                reviseVar(i, true);
            }
        }
        for (int i = 0; i < size; i++)
            reviseVar(i, false);
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        filter(idxVarInProp);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // updates the support for all values in the domain of variable
    // and remove unsupported values for variable
    private void reviseVar(int indexVar, boolean fromScratch) throws ContradictionException {
        int[] currentSupport;
        vrms.clear();
        vrms.setOffset(vars[indexVar].getLB());
        int val;
        for (val = vars[indexVar].getLB(); val <= vars[indexVar].getUB(); val = vars[indexVar].nextValue(val)) {
            currentSupport = seekNextSupport(indexVar, val, fromScratch);
            if (currentSupport != null) {
                setSupport(indexVar, val, currentSupport);
            } else {
                vrms.add(val);
                //                vars[indexVar].removeVal(val, this, false);
            }
        }
        vars[indexVar].removeValues(vrms, this);
    }


    // Store Last(x_i, val) = support
    private void setSupport(int indexVar, int value, int[] support) {
        for (int i = 0; i < vars.length; i++) {
            supports[(blocks[indexVar] + value - offsets[indexVar]) * size + i].set(support[i]);
        }
    }


    // Get Last(x_i, val)
    private int[] getUBport(int indexVar, int value) {
        int[] resultat = new int[size];
        for (int i = 0; i < size; i++) {
            resultat[i] = supports[(blocks[indexVar] + value - offsets[indexVar]) * size + i].get();
        }
        return resultat;
    }

    /**
     * seek a new support for (variable, value), the smallest tuple greater than currentSupport
     * the search is made through valid tuples until and allowed one is found.
     */
    private int[] seekNextSupport(int indexVar, int val, boolean fromscratch) {
        int[] currentSupport = new int[size];
        int k = 0;
        if (fromscratch) {
            for (int i = 0; i < size; i++) {
                if (i != indexVar)
                    currentSupport[i] = vars[i].getLB();
                else currentSupport[i] = val;
            }
            if (relation.isConsistent(currentSupport)) {
                return currentSupport;
            }
        } else {
            currentSupport = getUBport(indexVar, val);
            if (valcheck.isValid(currentSupport)) {
                return currentSupport;
            } else {
                currentSupport = getFirstValidTupleFrom(currentSupport, indexVar);
                if (currentSupport == null) return null;
                if (relation.isConsistent(currentSupport))
                    return currentSupport;
            }
        }

        while (k < vars.length) {
            if (k == indexVar) k++;
            if (k < vars.length) {
                if (vars[k].nextValue(currentSupport[k]) == Integer.MAX_VALUE) {
                    currentSupport[k] = vars[k].getLB();
                    k++;
                } else {
                    currentSupport[k] = vars[k].nextValue(currentSupport[k]);
                    if ((relation.isConsistent(currentSupport))) {
                        return currentSupport;
                    }
                    k = 0;
                }
            }
        }

        return null;
    }


    /**
     * t is a consistent tuple not valid anymore, we need to go to the first valid tuple
     * greater than t before searching among the valid tuples
     */
    private int[] getFirstValidTupleFrom(int[] t, int indexVar) {
        int k = 0;
        while (k < vars.length) {
            if (k == indexVar) k++;
            if (k < vars.length) {
                if (vars[k].nextValue(t[k]) == Integer.MAX_VALUE) {
                    t[k] = vars[k].getLB();
                    k++;
                } else {
                    t[k] = vars[k].nextValue(t[k]);
                    if (valcheck.isValid(t)) {
                        return t;
                    }
                    k = 0;
                }
            }
        }
        return null;
    }

    private void filter(int idx) throws ContradictionException {
        //sort variables regarding domain sizes to speedup the check !
        valcheck.sortvars();
        if (vars[idx].hasEnumeratedDomain()) {
            for (int i = 0; i < size; i++)
                if (idx != valcheck.getPosition(i))
                    reviseVar(valcheck.getPosition(i), false);
        } else {
            for (int i = 0; i < size; i++)
                reviseVar(valcheck.getPosition(i), false);
        }
    }
}
