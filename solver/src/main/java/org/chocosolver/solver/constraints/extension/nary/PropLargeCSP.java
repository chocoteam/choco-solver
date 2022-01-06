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

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 08/06/11
 */
public abstract class PropLargeCSP<R extends LargeRelation> extends Propagator<IntVar> {

    protected final R relation;

    protected PropLargeCSP(IntVar[] vars, R relation) {
        this(vars, relation, true);
    }

    protected PropLargeCSP(IntVar[] vars, R relation, boolean reactToFineEvent) {
        super(vars, PropagatorPriority.QUADRATIC, reactToFineEvent);
        this.relation = relation;
    }

    public final R getRelation() {
        return relation;
    }

    @Override
    public ESat isEntailed() {
        if (isCompletelyInstantiated()) {
            int[] tuple = new int[vars.length];
            for (int i = 0; i < vars.length; i++) {
                tuple[i] = vars[i].getValue();
            }
            return ESat.eval(relation.isConsistent(tuple));
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("CSPLarge({");
        for (int i = 0; i < vars.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(vars[i]).append(", ");
        }
        sb.append("})");
        return sb.toString();
    }


    /**
     * Extract the tuples from this internal data structure
     * @return a tuples object
     */
    public Tuples extractTuples(){
        return relation.convert();
    }
}
