/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.set;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.events.SetEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.BitSet;

/**
 * A UNION constraint that ensures that a set variable U is the union of set variables Si,
 * where i is given by a set variable I.
 *
 * @author Arthur Gontier
 * @author Charles Prud'homme
 * @since 28/10/2021
 */
public class PropUnionVar extends Propagator<SetVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int k;
    private final int iOffset;
    private final TIntObjectHashMap<int[]> mates;
    private final BitSet iii = new BitSet();
    private boolean firstProp = true;
    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * The union of sets is equal to union
     *
     * @param union   resulting set variable
     * @param indices set of allowed indices
     * @param iOffset index offset
     * @param sets    set variables to unify
     */
    public PropUnionVar(SetVar union, SetVar indices, int iOffset, SetVar[] sets) {
        super(ArrayUtils.append(sets, new SetVar[]{union, indices}), PropagatorPriority.QUADRATIC, false);
        k = sets.length;
        this.iOffset = iOffset;
        mates = new TIntObjectHashMap<>();
        for (int i : union.getUB()) {
            mates.put(i, new int[]{0, 1});
        }
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************
    @Override
    public void propagate(int evtmask) throws ContradictionException {
        SetVar indices = vars[k + 1];
        if (firstProp) {
            firstProp = false;
            model.getEnvironment().save(() -> firstProp = true);
            boundIndices(indices);
        }
        SetVar union = vars[k];
        filter1(indices, union);
        filter2(indices, union);
        filter6(indices, union);
        filter1(indices, union); // again for fx-point
    }

    private void boundIndices(SetVar indices) throws ContradictionException {
        if (indices.getUB().size() > 0) {
            int min = indices.getUB().min();
            while (min - iOffset < 0 && indices.getUB().size() > 0) {
                indices.remove(min, this);
                min = indices.getUB().min();
            }
        }
        if (indices.getUB().size() > 0) {
            int max = indices.getUB().max();
            while (max - iOffset >= k && indices.getUB().size() > 0) {
                indices.remove(max, this);
                max = indices.getUB().max();
            }
        }
    }

    @Override
    public int getPropagationConditions(int vIdx) {
        return SetEventType.ALL_EVENTS;
    }

    /**
     * <p>
     * Add values in lb(union) wrt to lb(indices) and lb(sets).
     * </p>
     * <p>
     * Remove from ub(union) and lb(indices) values from ub(sets)
     * </p>
     *
     * @param indices index variable
     * @param union   union variable
     * @throws ContradictionException if failure occurs
     */
    private void filter1(SetVar indices, SetVar union) throws ContradictionException {
        ISetIterator it;
        for (int i : indices.getLB()) {
            i -= iOffset;
            it = vars[i].getUB().newIterator();
            while (it.hasNext()) {
                int v = it.nextInt();
                if (!union.getUB().contains(v)) {
                    vars[i].remove(v, this);
                } else if (vars[i].getLB().contains(v)) {
                    union.force(v, this);
                }
            }
        }
    }

    /**
     * <p>
     * Remove from ub(indices) wrt to ub(sets) and ub(union)
     * </p>
     *
     * @param indices index variable
     * @param union   union variable
     * @throws ContradictionException if failure occurs
     */
    private void filter2(SetVar indices, SetVar union) throws ContradictionException {
        ISetIterator it;
        //todo gérer lb(set) vs ub(union) 
        for (int i : indices.getUB()) {
            if (indices.getLB().contains(i)) continue;
            boolean found;
            boolean isInstantiated = vars[i - iOffset].isInstantiated();
            it = vars[i - iOffset].getUB().newIterator();
            found = isInstantiated;
            while (it.hasNext()) {
                int v = it.nextInt();
                if (union.getUB().contains(v) != isInstantiated) {
                    found = !isInstantiated;
                    break;
                }
            }
            if (!found) {
                indices.remove(i, this);
            }
        }
    }

    /**
     * <p>
     * Filter from ub(union) => {@link #filter66(int, SetVar, SetVar)}
     * </p>
     *
     * @param indices index variable
     * @param union   union variable
     * @throws ContradictionException if failure occurs
     */
    private void filter6(SetVar indices, SetVar union) throws ContradictionException {
        iii.clear();
        ISetIterator it = indices.getUB().newIterator();
        while (it.hasNext()) {
            iii.set(it.nextInt() - iOffset);
        }
        for (int u : union.getUB()) {
            filter66(u, indices, union);
        }
    }

    /**
     * That's the tricky part.
     * Based on the principle of support (or wl).
     * <p>
     * If a value in ub(union) has no support, then remove it.
     * </p>
     * <p>
     * If a set is the only one containing a value from lb(union), force its index in lb(indices)
     * and the value in lb(sets_i)
     * </p>
     *
     * @param u       position of a variable in sets
     * @param indices index variable
     * @param union   union variable
     * @throws ContradictionException if failure occurs
     */
    private void filter66(int u, SetVar indices, SetVar union) throws ContradictionException {
        int[] ms = mates.get(u);
        boolean l0 = iii.get(ms[0]) && vars[ms[0]].getUB().contains(u);
        boolean l1 = iii.get(ms[1]) && vars[ms[1]].getUB().contains(u);
        if (l0 && !l1) {
            // swap
            int m = ms[0];
            ms[0] = ms[1];
            ms[1] = m;
            boolean b = l0;
            l0 = l1;
            l1 = b;
        }
        if (!l0) { // find another watcher
            int k = 0;
            for (int i = iii.nextSetBit(0); i > -1; i = iii.nextSetBit(i + 1)) {
                if (i == ms[0] || i == ms[1]) continue;
                if (vars[i].getUB().contains(u)) {
                    if (k == 0) {
                        ms[k++] = i;
                        l0 = true;
                        if (l1) {
                            break;
                        }
                    } else {
                        ms[k] = i;
                        l1 = true;
                        break;
                    }
                }
            }
        }
        if (!l0 && !l1) {
            union.remove(u, this);
        } else if (l0 ^ l1 && union.getLB().contains(u)) {
            int mate = l0 ? ms[0] : ms[1];
            indices.force(mate + iOffset, this);
            vars[mate].force(u, this);
        }
    }

    private final BitSet checker = new BitSet();

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            checker.clear();
            for (int i : vars[k + 1].getLB().toArray()) {
                i -= iOffset;
                for (int j : vars[i].getLB().toArray()) {
                    if (!vars[k].getLB().contains(j)) {
                        return ESat.FALSE;
                    }
                    checker.set(j);
                }
            }
            return ESat.eval(checker.cardinality() == vars[k].getLB().size());
        }
        return ESat.UNDEFINED;
    }

}
