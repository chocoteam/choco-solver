/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.globalcardinality;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.globalcardinality.GlobalCardinality.Consistency;
import org.chocosolver.solver.constraints.nary.globalcardinality.algo.AlgoGccAC;
import org.chocosolver.solver.constraints.nary.globalcardinality.algo.AlgoGccBC;
import org.chocosolver.solver.constraints.nary.globalcardinality.algo.GccFilter;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.tools.ArrayUtils;

import java.util.Arrays;

/**
 * Consistency propagator for the Global Cardinality Constraint (GCC), enforcing either:
 * <ul>
 *     <li>{@link Consistency#BC}: C.-G. Quimper, P. van Beek, A. Lopez-Ortiz, A. Golynski, and
 *     S.B. Sadjad. "An efficient bounds consistency algorithm for the global cardinality
 *     constraint." CP-2003.</li>
 *     <li>{@link Consistency#AC}: J.-C. Regin. "Generalized Arc Consistency for Global
 *     Cardinality Constraint." AAAI-96.</li>
 * </ul>
 * The two levels only differ in which {@link GccFilter} drives {@link #propagate(int)}, at which
 * {@link PropagatorPriority}, and in which domain events wake this propagator up
 * ({@link #getPropagationConditions(int)}); everything else, including the construction of the
 * dense per-value occurrence bounds consumed by the filter, is shared.
 * <p>
 * Meant to be posted alongside {@link PropFastGCC}, which is left in charge of tightening the
 * bounds of the cardinality variables and of the soundness/completeness of the constraint; this
 * propagator only brings its consistency level on the decision variables.
 *
 * @author Charles Prud'homme
 */
public class PropGcc extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final int n;
    private final int n2;
    private final int[] values;
    private final GccFilter filter;
    private final Consistency consistency;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * @param decvars            array of decision variables
     * @param restrictedValues   array of restricted values
     * @param valueCardinalities array of cardinality variables, one per restricted value
     * @param consistency        consistency level to enforce, {@link Consistency#BC} or
     *                           {@link Consistency#AC}
     */
    public PropGcc(IntVar[] decvars, int[] restrictedValues, IntVar[] valueCardinalities,
                    Consistency consistency) {
        super(ArrayUtils.append(decvars, valueCardinalities), priorityOf(consistency), false);
        this.values = restrictedValues;
        this.n = decvars.length;
        this.n2 = values.length;
        this.consistency = consistency;
        this.filter = switch (consistency) {
            case BC -> new AlgoGccBC(this);
            case AC -> new AlgoGccAC(this);
            case DEFAULT -> throw new IllegalArgumentException(
                    "PropGcc only supports Consistency.BC or Consistency.AC, not DEFAULT " +
                            "(handled by PropFastGCC alone)");
        };
        filter.reset(decvars);
    }

    private static PropagatorPriority priorityOf(Consistency consistency) {
        // BC (Quimper et al.) is near-linear; AC (Regin) rebuilds/repairs a flow, quadratic-ish.
        return consistency == Consistency.BC ? PropagatorPriority.LINEAR : PropagatorPriority.QUADRATIC;
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
    public int getPropagationConditions(int vIdx) {
        // BC (Quimper et al.) only reasons on bounds; AC needs fine domain events to be sound
        // on enumerated domains, so it keeps the default (all events).
        return consistency == Consistency.BC ? IntEventType.boundAndInst() : super.getPropagationConditions(vIdx);
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator, PropFastGCC already checks correctness
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropGcc_").append(consistency).append("_(");
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
