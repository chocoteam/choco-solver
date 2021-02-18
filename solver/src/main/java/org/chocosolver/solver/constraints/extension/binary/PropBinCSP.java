/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.extension.binary;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

/**
 * <br/>
 *
 * @author Charles Prud'homme, Hadrien Cambazard
 * @since 08/06/11
 */
public abstract class PropBinCSP extends Propagator<IntVar> {

    protected BinRelation relation;
    protected IntVar v0, v1;

    protected PropBinCSP(IntVar x, IntVar y, BinRelation relation) {
        super(ArrayUtils.toArray(x, y), PropagatorPriority.BINARY, true);
        this.relation = relation;
        this.v0 = x;
        this.v1 = y;
    }

    public final BinRelation getRelation() {
        return relation;
    }

    @Override
    public ESat isEntailed() {
        int nbCons = 0;

        int ub0 = vars[0].getUB();
        for (int val0 = vars[0].getLB(); val0 <= ub0; val0 = vars[0].nextValue(val0)) {
            int nbS = 0;
            int ub1 = vars[1].getUB();
            for (int val1 = vars[1].getLB(); val1 <= ub1; val1 = vars[1].nextValue(val1)) {
                if (relation.isConsistent(val0, val1)) {
                    nbS++;
                }
            }
            if (nbS > 0 && nbS < vars[1].getDomainSize()) {
                return ESat.UNDEFINED;
            }
            nbCons += nbS;

        }
        if (nbCons == 0) {
            return ESat.FALSE;
        } else if (nbCons == vars[0].getDomainSize() * vars[1].getDomainSize()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    /**
     * Extract the tuples from this internal data structure
     * @return a tuples object
     */
    public Tuples extractTuples(){
        return relation.convert();
    }
}
