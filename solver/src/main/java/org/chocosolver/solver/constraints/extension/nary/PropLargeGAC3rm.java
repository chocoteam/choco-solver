/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 24/04/2014
 */
public class PropLargeGAC3rm extends PropLargeCSP<LargeRelation> {

    // Last valid supports Last(x_i, val) = supports( (blocks(i) + val) * size )
    private final int[] supports;

    private final int[] blocks;

    // Cardinality
    private final int size;

    // offsets(i) = Min(x_i)
    private final int[] offsets;

    private final DisposableValueIterator[] seekIter;

    private final IntIterableBitSet vrms;

    private PropLargeGAC3rm(IntVar[] vs, LargeRelation relation) {
        super(vs, relation);
        this.size = vs.length;
        this.blocks = new int[size];
        this.offsets = new int[size];

        int nbElt = 0;

        for (int i = 0; i < size; i++) {
            offsets[i] = vs[i].getLB();
            blocks[i] = nbElt;
            if (!vars[i].hasEnumeratedDomain()) {
                nbElt += 2;
            } else nbElt += vars[i].getUB() - vars[i].getLB() + 1;
        }

        this.supports = new int[nbElt * size];
        this.seekIter = new DisposableValueIterator[size];
        for (int i = 0; i < size; i++) {
            seekIter[i] = vars[i].getValueIterator(true);
        }
        Arrays.fill(supports, Integer.MIN_VALUE);
        vrms = new IntIterableBitSet();
    }

    public PropLargeGAC3rm(IntVar[] vs, Tuples tuples) {
        this(vs, RelationFactory.makeLargeRelation(tuples, vs));
    }



    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if ((evtmask & PropagatorEventType.FULL_PROPAGATION.getMask()) != 0) {
            for (int i = 0; i < vars.length; i++) {
                initializeSupports(i);
            }
        }

        for (int i = 0; i < size; i++)
            reviseVar(i);
    }


    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        for (int i = 0; i < size; i++)
            if (idxVarInProp != i) reviseVar(i);
        if (!vars[idxVarInProp].hasEnumeratedDomain()) {
            reviseVar(idxVarInProp);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * initialize the supports of each value of indexVar
     *
     * @throws ContradictionException
     */
    private void initializeSupports(int indexVar) throws ContradictionException {
        int[] currentSupport;
        int val;
        if (vars[indexVar].hasEnumeratedDomain()) {
            DisposableValueIterator it = vars[indexVar].getValueIterator(true);
            vrms.clear();
            vrms.setOffset(vars[indexVar].getLB());
            try {
                while (it.hasNext()) {
                    val = it.next();
                    if (lastSupport(indexVar, val)[0] == Integer.MIN_VALUE) { // no supports initialized yet for this value
                        currentSupport = seekNextSupport(indexVar, val);
                        if (currentSupport != null) {
                            setSupport(currentSupport);
                        } else {
                            vrms.add(val);
                            //                        vars[indexVar].removeVal(val, this, false);
                        }
                    }
                }
                vars[indexVar].removeValues(vrms, this);
            } finally {
                it.dispose();
            }
        } else {
            for (val = vars[indexVar].getLB(); val <= vars[indexVar].getUB(); val++) {
                currentSupport = seekNextSupport(indexVar, val);
                if (currentSupport != null) {
                    setBoundSupport(indexVar, 0, currentSupport);
                    break; //stop at the first consistent lower bound !
                }
            }
            vars[indexVar].updateLowerBound(val, this);
            for (val = vars[indexVar].getUB(); val >= vars[indexVar].getLB(); val--) {
                currentSupport = seekNextSupport(indexVar, val);
                if (currentSupport != null) {
                    setBoundSupport(indexVar, 1, currentSupport);
                    break; //stop at the first consistent upper bound !
                }
            }
            vars[indexVar].updateUpperBound(val, this);
        }
    }

    // updates the support for all values in the domain of variable
    // and remove unsupported values for variable
    private void reviseVar(int indexVar) throws ContradictionException {
        int[] currentSupport;
        int val;
        if (vars[indexVar].hasEnumeratedDomain()) {
            DisposableValueIterator it = vars[indexVar].getValueIterator(true);
            vrms.clear();
            vrms.setOffset(vars[indexVar].getLB());
            try {
                while (it.hasNext()) {
                    val = it.next();
                    if (isInvalid(lastSupport(indexVar, val))) {
                        currentSupport = seekNextSupport(indexVar, val);
                        if (currentSupport != null) {
                            setSupport(currentSupport);
                        } else {
                            vrms.add(val);
                            //                            vars[indexVar].removeVal(val, this, false);
                        }
                    }
                }
                vars[indexVar].removeValues(vrms, this);
            } finally {
                it.dispose();
            }
        } else {
            int[] inf_supports = lastBoundSupport(indexVar, 0);
            if (vars[indexVar].getLB() != inf_supports[indexVar] || isInvalid(inf_supports)) {
                for (val = vars[indexVar].getLB(); val <= vars[indexVar].getUB(); val++) {
                    currentSupport = seekNextSupport(indexVar, val);
                    if (currentSupport != null) {
                        setBoundSupport(indexVar, 0, currentSupport);
                        break; //stop at the first consistent lower bound !
                    }
                }
                vars[indexVar].updateLowerBound(val, this);
            }
            int[] sup_supports = lastBoundSupport(indexVar, 1);
            if (vars[indexVar].getUB() != sup_supports[indexVar] || isInvalid(sup_supports)) {
                for (val = vars[indexVar].getUB(); val >= vars[indexVar].getLB(); val--) {
                    currentSupport = seekNextSupport(indexVar, val);
                    if (currentSupport != null) {
                        setBoundSupport(indexVar, 1, currentSupport);
                        break; //stop at the first consistent upper bound !
                    }
                }
                vars[indexVar].updateUpperBound(val, this);
            }
        }
    }

    // Store Last(x_i, val) = support
    private void setSupport(int[] support) {
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].hasEnumeratedDomain())
                setOneSupport(i, support[i], support);
        }
    }

    private void setOneSupport(int indexVar, int value, int[] support) {
        System.arraycopy(support, 0, supports, (blocks[indexVar] + value - offsets[indexVar]) * size, vars.length);
    }


    // Store Last(x_i, val) = support
    private void setBoundSupport(int indexVar, int idxBound, int[] support) {
        System.arraycopy(support, 0, supports, (blocks[indexVar] + idxBound) * size, vars.length);
    }


    // Get Last(x_i, val)
    private int[] getUBport(int indexVar, int value) {
        int[] resultat = new int[size];
        System.arraycopy(supports, (blocks[indexVar] + value - offsets[indexVar]) * size, resultat, 0, size);
        return resultat;
    }


    // return the support standing for the lower bound
    // of indexVar if idxBound = 0 or upperbound if idxBound = 1
    private int[] getBoundSupport(int indexVar, int idxBound) {
        int[] resultat = new int[size];
        System.arraycopy(supports, (blocks[indexVar] + idxBound) * size, resultat, 0, size);
        return resultat;
    }

    // Get Last(x_i, val)
    private int[] lastSupport(int indexVar, int value) {
        return getUBport(indexVar, value);
    }

    // return the support standing for the lower bound
    // of indexVar if idxBound = 0 or upperbound if idxBound = 1
    private int[] lastBoundSupport(int indexVar, int idxBound) {
        return getBoundSupport(indexVar, idxBound);
    }


    // Is tuple invalid ?
    private boolean isInvalid(int[] tuple) {
        for (int i = 0; i < size; i++)
            if (!vars[i].contains(tuple[i])) return true;
        return false;
    }

    // seek a new support for (variable, value), the smallest tuple greater than currentSupport
    private int[] seekNextSupport(int indexVar, int val) {
        int[] currentSupport = new int[size];
        int k = 0;
        for (int i = 0; i < size; i++) {
            seekIter[i].dispose();
            seekIter[i] = vars[i].getValueIterator(true);
            if (i != indexVar)
                currentSupport[i] = seekIter[i].next();
            else currentSupport[i] = val;
        }
        if (relation.isConsistent(currentSupport)) {
            return currentSupport;
        }
        while (k < vars.length) {
            if (k == indexVar) k++;
            if (k < vars.length) {
                if (!seekIter[k].hasNext()) {
                    seekIter[k].dispose();
                    seekIter[k] = vars[k].getValueIterator(true);
                    currentSupport[k] = seekIter[k].next();
                    k++;
                } else {
                    currentSupport[k] = seekIter[k].next();
                    if ((relation.isConsistent(currentSupport))) {
                        return currentSupport;
                    }
                    k = 0;
                }
            }
        }
        return null;
    }
}
