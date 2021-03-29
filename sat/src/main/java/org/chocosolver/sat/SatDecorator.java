/*
 * This file is part of choco-sat, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 19/03/2021
 */
public class SatDecorator extends MiniSat {

    // store clauses dynamically added from outside
    public ArrayList<Clause> dynClauses = new ArrayList<>();


    /**
     * Add a clause during resolution
     *
     * @param ps clause to add
     */
    public void learnClause(int... ps) {
        Arrays.sort(ps);
        switch (ps.length) {
            case 0:
                ok_ = false;
                return;
            case 1:
                dynUncheckedEnqueue(ps[0]);
                ok_ = (propagate() == CR_Undef);
                return;
            default:
                Clause cr = new Clause(ps);
                dynClauses.add(cr);
                attachClause(cr);
                break;
        }
    }

    public void detachLearnt(int ci) {
        Clause cr = dynClauses.get(ci);
        detachClause(cr);
        dynClauses.remove(ci);
    }

    private void dynUncheckedEnqueue(int l) {
        touched_variables_.add(l);
    }

    public int nLearnt() {
        return dynClauses.size();
    }

}
