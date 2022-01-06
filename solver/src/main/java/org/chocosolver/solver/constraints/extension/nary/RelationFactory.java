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
import org.chocosolver.solver.variables.IntVar;

/**
 *
 * <p>
 * Project: choco.
 * @author Charles Prud'homme
 * @since 21/12/2015.
 */
class RelationFactory {


    /**
     * Make a large relation from <i>tuples</i> and an array of IntVar.
     * @param tuples list of tuples
     * @param vars array of IntVar
     * @return a large relation
     */
    public static LargeRelation makeLargeRelation(Tuples tuples, IntVar[] vars) {
        long totalSize = 1;
        for (int i = 0; i < vars.length && (int)totalSize == totalSize; i++) { // to prevent from long overflow
            totalSize *= vars[i].getRange();
        }
        if ((int)totalSize != totalSize) {
            return new TuplesVeryLargeTable(tuples, vars);
        }
        if (totalSize / 8 > 50 * 1024 * 1024) {
            return new TuplesLargeTable(tuples, vars);
        }
        return new TuplesTable(tuples, vars);
    }


    /**
     * Make iterable relation from <i>tuples</i> and an array of IntVar.
     * @param tuples list of tuples
     * @param vars array of IntVar
     * @return an iterable relation
     */
    public static IterTuplesTable makeIterableRelation(Tuples tuples, IntVar[] vars) {
        return new IterTuplesTable(tuples, vars);
    }

    /**
     * Make list-based relation from <i>tuples</i> and an array of IntVar.
     * @param tuples list of tuples
     * @param vars array of IntVar
     * @return a lsit-based relation
     */
    public static TuplesList makeListBasedRelation(Tuples tuples, IntVar[] vars) {
        return new TuplesList(tuples, vars);
    }

}
