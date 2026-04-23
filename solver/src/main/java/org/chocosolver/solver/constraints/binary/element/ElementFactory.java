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
 * Created by cprudhom on 29/09/15.
 * Project: choco.
 */
public class ElementFactory {
    private ElementFactory() {
    }

    /**
     * Count the number of time the values in TABLE increase or decrease (ie, count picks and valleys).
     *
     * @param TABLE an array of values
     * @return the number of picks and valleys
     */
    private static int sawtooth(int[] TABLE) {
        int i = 0;
        while (i < TABLE.length - 1 && TABLE[i] == TABLE[i + 1]) {
            i++;
        }
        if (i == TABLE.length - 1) {
            return -1;
        }
        boolean up = TABLE[i] < TABLE[i + 1];
        int c = 0;
        i++;
        while (i < TABLE.length - 1) {
            if (up && TABLE[i] > TABLE[i + 1]) {
                c++;
                up = false;
            } else if (!up && TABLE[i] < TABLE[i + 1]) {
                c++;
                up = true;
            }
            i++;
        }
        return c;
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
        // first check the variables match
        int st = sawtooth(table);
        if (st == -1) { // all values from table are the same OR table only contains one value
            assert table[0] == table[table.length - 1];
            return new Constraint("FAKE_ELMT",
                    new PropMember(index, new IntIterableRangeSet(offset, offset + table.length - 1), false),
                    new PropEqualXC(value, table[0])
            );
        }
        int nbValues = (int) Arrays.stream(table).distinct().count();
        if (nbValues * 10 < table.length && value.hasEnumeratedDomain() && index.hasEnumeratedDomain()) {
            // regroup indexes leading to the same value to work on a smaller element constraint
            Map<Integer, IntIterableSet> indexesByValue = new HashMap<>();
            for (int i = 0; i < table.length; i++) {
                int val = table[i];
                if (!indexesByValue.containsKey(val)) {
                    indexesByValue.put(val, new IntIterableBitSet());
                }
                indexesByValue.get(val).add(i + offset);
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
            Model model = index.getModel();
            IntVar reducedIndex = model.intVar(0, newSize - 1, false);
            // new element on the restricted size + element on the sets
            return new Constraint(ConstraintsName.ELEMENT,
                    new PropElement(value, reducedTable, reducedIndex, 0),
                    new PropElementIn(index, indexSets, reducedIndex));
        }
        return new Constraint(ConstraintsName.ELEMENT, new PropElement(value, table, index, offset));
    }
}
