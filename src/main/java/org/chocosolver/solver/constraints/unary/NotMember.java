/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.unary;

import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26 nov. 2010
 */
public class NotMember extends Constraint {

    private final IntVar var;
    private final int[] values;
    private final int lb, ub;

    // for JSON
    @SuppressWarnings("WeakerAccess")
    protected NotMember(IntVar var, int lb, int ub, int[] values, Propagator prop) {
        super(ConstraintsName.NOTMEMBER, prop);
        this.var = var;
        this.values = values;
        this.lb = lb;
        this.ub = ub;
    }

    public NotMember(IntVar var, int[] values) {
        this(var, 0, 0, values, new PropNotMember(var, new IntIterableRangeSet(values)));
    }

    public NotMember(IntVar var, int lowerbound, int upperbound) {
        this(var, lowerbound, upperbound, null, new PropNotMember(var, new IntIterableRangeSet(lowerbound, upperbound)));
    }

    @Override
    public Constraint makeOpposite() {
        if (values == null) {
            return new Member(var, lb, ub);
        } else {
            return new Member(var, values);
        }
    }
}
