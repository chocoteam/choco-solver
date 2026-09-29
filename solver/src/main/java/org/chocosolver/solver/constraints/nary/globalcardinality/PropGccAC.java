/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.globalcardinality.algo.AlgoGccAC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Arc-consistency propagator for the Global Cardinality Constraint (GCC), based on:
 * J.-C. Regin. "Generalized Arc Consistency for Global Cardinality Constraint." AAAI-96.
 * <p>
 * Meant to be posted alongside {@link PropFastGCC}, which is left in charge of tightening the
 * bounds of the cardinality variables and of the soundness/completeness of the constraint; this
 * propagator only brings arc-consistency on the decision variables.
 *
 * @author Charles Prud'homme
 */
public class PropGccAC extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int n2;
    private final int[] values;
    private final AlgoGccAC filter;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * @param decvars            array of decision variables
     * @param restrictedValues   array of restricted values
     * @param valueCardinalities array of cardinality variables, one per restricted value
     */
    public PropGccAC(IntVar[] decvars, int[] restrictedValues, IntVar[] valueCardinalities) {
        super(ArrayUtils.append(decvars, valueCardinalities), PropagatorPriority.QUADRATIC, false);
        this.values = restrictedValues;
        this.n = decvars.length;
        this.n2 = values.length;
        this.filter = new AlgoGccAC(this);
        filter.reset(decvars);
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        int gMin = Integer.MAX_VALUE;
        int gMax = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            gMin = Math.min(gMin, vars[i].getLB());
            gMax = Math.max(gMax, vars[i].getUB());
        }
        for (int v : values) {
            gMin = Math.min(gMin, v);
            gMax = Math.max(gMax, v);
        }
        int range = gMax - gMin + 1;
        int[] minOcc = new int[range];
        int[] maxOcc = new int[range];
        // values out of the restricted list are unconstrained: [0, n]
        Arrays.fill(maxOcc, n);
        for (int i = 0; i < n2; i++) {
            IntVar card = vars[n + i];
            int idx = values[i] - gMin;
            minOcc[idx] = card.getLB();
            maxOcc[idx] = card.getUB();
        }
        filter.filter(minOcc, maxOcc, gMin);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator, PropFastGCC already checks correctness
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropGccAC_(");
        int i = 0;
        for (; i < Math.min(4, vars.length); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("...,");
        }
        st.append(vars[vars.length - 1].getName()).append(")");
        return st.toString();
    }

}
