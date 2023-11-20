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
     *
     * @return
     */
    public static Reason undef() {
        return UNDEF;
    }

    /**
     * Create a reason from a clause
     *
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
     *
     * @param ps other literals
     * @return a reason
     * @implSpec if ength of ps is strictly greater than 2,
     * then the literal at index 0 should be left empty for the asserting literal
     */
    public static Reason r(int... ps) {
        if (ps.length == 1) {
            return new Reason(null, 2, ps[0], 0, 0);
        } else if (ps.length == 2) {
            return new Reason(null, 3, ps[0], ps[1], 0);
        } else if (ps.length > 2) {
            return Reason.r(new MiniSat.Clause(ps));
        } else {
            return Reason.undef();
        }
    }

    public static Reason r(Reason r, int p) {
        switch (r.type) {
            case 0: {
                int[] ps = new int[r.cl.size() + 1];
                for (int i = 0; i < r.cl.size(); i++) {
                    ps[i] = r.cl._g(i);
                }
                ps[r.cl.size()] = p;
                return Reason.r(ps);
            }
            case 2:
                return Reason.r(r.d1, p);
            case 3: {
                int[] ps = new int[3];
                ps[0] = r.d1;
                ps[1] = r.d2;
                ps[2] = p;
                return Reason.r(ps);
            }
            default:
                return Reason.r(p);
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
                return "lits:" + d1 + " ∨ " + d2;
            default:
                return "undef";
        }
    }
}
