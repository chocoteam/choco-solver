/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2023, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.sat;

import org.chocosolver.solver.ICause;

/**
 * A class to explain a propagation.
 * A reason is always associated with one or more literals.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/09/2023
 */
public class Reason implements ICause {

    private static final Reason UNDEF = new Reason(MiniSat.Clause.undef(), 0, -1, -1, -1);

    MiniSat.Clause cl;
    int type;
    int d1;
    int d2;
    int a;

    private Reason(MiniSat.Clause cl, int type, int d1, int d2, int a) {
        this.cl = cl;
        this.type = type;
        this.d1 = d1;
        this.d2 = d2;
        this.a = a;
    }

    /**
     * Create an undefined reason
     * @return
     */
    public static Reason undef() {
        return UNDEF;
    }

    /**
     * Create a reason from a clause
     * @param cl a clause
     * @return a reason
     */
    public static Reason r(MiniSat.Clause cl) {
        return new Reason(cl, 0, 0, 0, 0);
    }

    //public static Reason r0(int prop_id, int inf_id) {
    //    return new Reason(null, 1, inf_id, prop_id, 0);
    //}

    /**
     * Create a reason from one or more literals
     * @param p a literal
     * @param ps other literals
     * @return a reason
     */
    public static Reason r(int p, int... ps) {
        if (ps.length == 0) {
            return new Reason(null, 2, p, 0, 0);
        } else if (ps.length == 1) {
            return new Reason(null, 3, p, ps[0], 0);
        } else {
            throw new UnsupportedOperationException();
        }
    }


    @Override
    public String toString() {
        switch (type) {
            case 0:
                return "cl:" + cl.toString();
            //case 1:
            //return "r0(" + d1 + ", " + d2 + ")";
            case 2:
                return "lit:" + d1;
            case 3:
                return "lits:" + d1 + " /\\ " + d2;
            default:
                return "undef";
        }
    }
}
