/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.binary.element;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.unary.PropEqualXC;
import org.chocosolver.solver.constraints.unary.PropMember;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableBitSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableSet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * A factory that selects the most adapted element propagator.
 * 
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 29/09/15
 */
public class ElementFactory {

    private ElementFactory() {
    }

    /**
     * Detect and return the most adapted Element propagator wrt to the values in table
     *
     * @param value  the result variable
     * @param table  the array of values
     * @param index  the index variable
     * @param offset the offset
     * @return an Element constraint
     */
    public static Constraint detect(IntVar value, int[] table, IntVar index, int offset) {
        int nbValues = (int) Arrays.stream(table).distinct().count();
        int n = table.length;
        if (nbValues == 1) { // all values from table are the same OR table only contains one value
            return buildElementMember(value, table, index, offset);
        }
        // bounded domain for result variable
        if (!value.hasEnumeratedDomain()) {
            return buildElementBC(value, table, index, offset);
        }
        // large tables
        if (n > 50) {
            // table reduction by merging same value cells
            if (nbValues * 10 < n && value.hasEnumeratedDomain() && index.hasEnumeratedDomain()) {
                return buildReducedElementAC(value, table, index, offset);
            } else {
                // bound counsistency on result variable (fast)
                return buildElementBC(value, table, index, offset);
            }
        }
        // classical element constraint
        return buildElementAC(value, table, index, offset);
    }

    /**
     * Creates an Element propagator optimized for bounded results (AC on index, BC on result)
     *
     * @param value the result variable
     * @param table the array of values
     * @param index the index variable
     * @param offset facultative offset for the index variable
     * @return an element constraint
     */
    public static Constraint buildElementMember(IntVar value, int[] table, IntVar index, int offset) {
        assert table[0] == table[table.length - 1];
        return new Constraint("FAKE_ELMT",
                new PropMember(index, new IntIterableRangeSet(offset, offset + table.length - 1), false),
                new PropEqualXC(value, table[0])
        );
    }

    /**
     * Creates an Element propagator optimized for bounded results (AC on index, BC on result)
     *
     * @param value the result variable
     * @param table the array of values
     * @param index the index variable
     * @param offset facultative offset for the index variable
     * @return an element constraint
     */
    public static Constraint buildElementBC(IntVar value, int[] table, IntVar index, int offset) {
        return new Constraint(ConstraintsName.ELEMENT, new PropElementBound(value, table, index, offset));
    }

    /**
     * Creates an Element propagator optimized for enumerated results (AC)
     *
     * @param value the result variable
     * @param table the array of values
     * @param index the index variable
     * @param offset facultative offset for the index variable
     * @return an element constraint
     */
    public static Constraint buildElementAC(IntVar value, int[] table, IntVar index, int offset) {
        return new Constraint(ConstraintsName.ELEMENT, new PropElement(value, table, index, offset));
    }

    /**
     * Creates an Element propagator optimized for large table using multiple times the same value (AC)
     *
     * @param value the result variable
     * @param table the array of values
     * @param index the index variable
     * @param offset facultative offset for the index variable
     * @return an element constraint
     */
    public static Constraint buildReducedElementAC(IntVar value, int[] table, IntVar index, int offset) {
        Model model = index.getModel();
        // offset management
        IntVar shiftedIndex = offset == 0 ? index : model.intView(1, index, -offset);
        // group indexes leading to the same value
        Map<Integer, IntIterableSet> indexesByValue = new HashMap<>();
        for (int i = 0; i < table.length; i++) {
            int val = table[i];
            if (!indexesByValue.containsKey(val)) {
                indexesByValue.put(val, new IntIterableBitSet());
            }
            indexesByValue.get(val).add(i);
        }
        int newSize = indexesByValue.size();
        int[] reducedTable = new int[newSize];
        IntIterableSet[] indexSets = new IntIterableSet[newSize];
        int idx = 0;
        for (Map.Entry<Integer, IntIterableSet> entry : indexesByValue.entrySet()) {
            reducedTable[idx] = entry.getKey();
            indexSets[idx] = entry.getValue();
            idx++;
        }
        // new index on reduced table
        IntVar reducedIndex = model.intVar(0, newSize - 1, false);
        // new element on the restricted size + element on the sets
        return new Constraint(ConstraintsName.ELEMENT,
                new PropElement(value, reducedTable, reducedIndex, 0),
                new PropElementIn(shiftedIndex, indexSets, reducedIndex));
    }
}
