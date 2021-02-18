/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.expression.continuous.arithmetic.RealIntervalConstant;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.RealVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.objects.RealInterval;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.IntStream;

import static java.util.Arrays.stream;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @since 28/04/2016.
 */
public class VariableUtils {

    /**
     * @param vars array of variables
     * @return computes the bounds for the sum of <i>vars</i>
     */
    public static int[] boundsForAddition(IntVar... vars) {
        long[] bounds = new long[2];
        IntStream.range(0, vars.length).forEach(i -> {
                    bounds[0] += vars[i].getLB();
                    bounds[1] += vars[i].getUB();
                }
        );
        return new int[]{MathUtils.safeCast(bounds[0]), MathUtils.safeCast(bounds[1])};
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the sum of <i>vars</i>
     */
    public static int[] boundsForScalar(IntVar[] vars, int[] coeffs) {
        long[] bounds = new long[2];
        for (int i = 0; i < vars.length; i++) {
            int c = coeffs[i];
            if (c >= 0) {
                bounds[0] += vars[i].getLB() * coeffs[i];
                bounds[1] += vars[i].getUB() * coeffs[i];
            } else {
                bounds[0] += vars[i].getUB() * coeffs[i];
                bounds[1] += vars[i].getLB() * coeffs[i];
            }
        }
        return new int[]{MathUtils.safeCast(bounds[0]), MathUtils.safeCast(bounds[1])};
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the sum of <i>vars</i>
     */
    public static double[] boundsForAddition(RealVar... vars) {
        double[] bounds = new double[2];
        for (int i = 0; i < vars.length; i++) {
            bounds[0] += vars[i].getLB();
            bounds[1] += vars[i].getUB();
        }
        return bounds;
    }

    private static int[] bound(long... values) {
        return new int[]{
                MathUtils.safeCast(stream(values).min().getAsLong()),
                MathUtils.safeCast(stream(values).max().getAsLong())
        };
    }

    private static double[] bound(double... values) {
        return new double[]{stream(values).min().getAsDouble(), stream(values).max().getAsDouble()};
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x - y"
     */
    public static int[] boundsForSubstraction(IntVar x, IntVar y) {
        return new int[]{
                MathUtils.safeCast(x.getLB() - y.getUB()),
                MathUtils.safeCast(x.getUB() - y.getLB())};
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x - y"
     */
    public static double[] boundsForSubstraction(RealVar x, RealVar y) {
        RealInterval res = RealUtils.sub(x, y);
        return new double[]{res.getLB(), res.getUB()};
    }


    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x * y"
     */
    public static int[] boundsForMultiplication(IntVar x, IntVar y) {
        return bound(
                x.getLB() * y.getLB(),
                x.getLB() * y.getUB(),
                x.getUB() * y.getLB(),
                x.getUB() * y.getUB()
        );
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x * y"
     */
    public static double[] boundsForMultiplication(RealVar x, RealVar y) {
        RealInterval res = RealUtils.mul(x, y);
        return new double[]{res.getLB(), res.getUB()};
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x / y"
     */
    public static int[] boundsForDivision(IntVar x, IntVar y) {
        int lx = x.getLB();
        int ux = x.getUB();
        int ly = y.getLB();
        int uy = y.getUB();
        if (ly == 0) {
            ly++;
        }
        if (uy == 0) {
            uy--;
        }
        if (ly < 0 && 0 < uy) {
            ly = -1;
            uy = 1;
        }
        return bound(
                lx / ly,
                lx / uy,
                ux / ly,
                ux / uy
        );
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x / y"
     */
    public static double[] boundsForDivision(RealVar x, RealVar y) {
        RealInterval res = RealUtils.odiv(x, y);
        return new double[]{res.getLB(), res.getUB()};
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x % y"
     */
    public static int[] boundsForModulo(IntVar x, IntVar y) {
        long[] vals = new long[4];
        if (y.isInstantiatedTo(0)) {
            vals[0] = Integer.MIN_VALUE;
            vals[1] = Integer.MAX_VALUE;
            vals[2] = 0;
            vals[3] = 0;
        } else {
            int yl = y.getLB();
            int yu = y.getUB();
            vals[0] = 0;
            vals[1] = 0;
            vals[2] = 0;
            if (yl >= 0) {
                vals[3] = yu - 1;
            } else if (yu <= 0) {
                vals[3] = yl + 1;
            } else {
                vals[0] = Math.abs(yl) - 1;
                vals[1] = Math.abs(yu) - 1;
                vals[2] = -vals[0];
                vals[3] = -vals[1];
            }

        }
        return bound(vals);
    }

    /**
     * @param x a variable
     * @param y a variable (we consider that lb(y) >= 0
     * @return computes the bounds for "x ^ y"
     */
    public static int[] boundsForPow(IntVar x, IntVar y) {
        return bound(0, 1,
                MathUtils.pow(x.getLB(), y.getUB()),
                MathUtils.pow(x.getUB(), y.getUB()),
                MathUtils.pow(x.getLB() + 1, y.getUB()),
                MathUtils.pow(x.getUB() - 1, y.getUB()),
                MathUtils.pow(x.getLB(), Math.max(0, y.getUB() - 1)),
                MathUtils.pow(x.getUB(), Math.max(0, y.getUB() - 1)),
                MathUtils.pow(x.getLB() + 1, Math.max(0, y.getUB() - 1)),
                MathUtils.pow(x.getUB() - 1, Math.max(0, y.getUB() - 1))
        );
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "x ^ y"
     */
    public static double[] boundsForPow(RealVar x, RealVar y) {
        return bound(0,
                Math.pow(x.getLB(), y.getLB()),
                Math.pow(x.getLB(), y.getUB()),
                Math.pow(x.getUB(), y.getLB()),
                Math.pow(x.getUB(), y.getUB())
        );
    }

    /**
     * @param x a variable
     * @param y a constant
     * @return computes the bounds for "x ^ y"
     */
    public static double[] boundsForPow(RealVar x, double y) {
        if (y >= 0) {
            return bound(0,
                    Math.pow(x.getLB(), y),
                    Math.pow(x.getUB(), y));
        } else {
            // bounds for 1/x^y when x < 0
            double bounds[] = bound(0,
                    Math.pow(x.getLB(), Math.abs(y)),
                    Math.pow(x.getUB(), Math.abs(y)));
            RealInterval boundsDiv = RealUtils.odiv(new RealIntervalConstant(1.0, 1.0), new RealIntervalConstant(bounds[0], bounds[1]));
            return new double[]{boundsDiv.getLB(), boundsDiv.getUB()};
        }
    }

    /**
     * @param x a variable
     * @param y a variable
     * @return computes the bounds for "atan2(x , y)"
     */
    public static double[] boundsForAtan2(RealVar x, RealVar y) {
        return bound(
                Math.atan2(x.getLB(), y.getLB()),
                Math.atan2(x.getLB(), y.getUB()),
                Math.atan2(x.getUB(), y.getLB()),
                Math.atan2(x.getUB(), y.getUB())
        );
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the minimum among <i>vars</i>
     */
    public static int[] boundsForMinimum(IntVar... vars) {
        int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        for (int i = 0; i < vars.length; i++) {
            bounds[0] = Math.min(bounds[0], vars[i].getLB());
            bounds[1] = Math.min(bounds[1], vars[i].getUB());
        }
        return bounds;
    }


    /**
     * @param vars array of variables
     * @return computes the bounds for the minimum among <i>vars</i>
     */
    public static double[] boundsForMinimum(RealVar... vars) {
        double[] bounds = new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY};
        for (int i = 0; i < vars.length; i++) {
            bounds[0] = Math.min(bounds[0], vars[i].getLB());
            bounds[1] = Math.min(bounds[1], vars[i].getUB());
        }
        return bounds;
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the maximum among <i>vars</i>
     */
    public static int[] boundsForMaximum(IntVar... vars) {
        int[] bounds = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        for (int i = 0; i < vars.length; i++) {
            bounds[0] = Math.max(bounds[0], vars[i].getLB());
            bounds[1] = Math.max(bounds[1], vars[i].getUB());
        }
        return bounds;
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the maximum among <i>vars</i>
     */
    public static double[] boundsForMaximum(RealVar... vars) {
        double[] bounds = new double[]{Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY};
        for (int i = 0; i < vars.length; i++) {
            bounds[0] = Math.max(bounds[0], vars[i].getLB());
            bounds[1] = Math.max(bounds[1], vars[i].getUB());
        }
        return bounds;
    }

    /**
     * @param vars an array of variables
     * @return the variables' domain cardinality or {@link Integer#MAX_VALUE} if too large.
     */
    public static long domainCardinality(IntVar... vars) {
        long card = 1;
        for (int i = 0; i < vars.length && card < Integer.MAX_VALUE; i++) {
            card *= vars[i].getDomainSize();
        }
        return Math.min(Integer.MAX_VALUE, card);
    }

    /**
     * Compute the search space size
     *
     * @return search space size
     */
    public static double searchSpaceSize(IntVar[] vars) {
        double size = 1;
        for (int i = 0; i < vars.length && size > 0; i++) {
            size *= vars[i].getDomainSize();
        }
        if (size <= 0 || size == Double.POSITIVE_INFINITY) {
            size = Double.MAX_VALUE;
        }
        return size;
    }

    /**
         * Compute the search space size
         *
         * @return search space size
         */
        public static double searchSpaceSize(Iterator<IntVar> vars) {
            double size = 1;
            while (vars.hasNext()) {
                IntVar var = vars.next();
                size *= var.getDomainSize();
            }
            if (size <= 0 || size == Double.POSITIVE_INFINITY) {
                size = Double.MAX_VALUE;
            }
            return size;
        }

    /**
     * @param x an int variable
     * @param y another int variable
     * @return true if the two domains intersect
     */
    public static boolean intersect(IntVar x, IntVar y) {
        if (x.getLB() > y.getUB() || y.getLB() > x.getUB()) {
            return false;
        }
        if (x.hasEnumeratedDomain() && y.hasEnumeratedDomain()) {
            int ub = x.getUB();
            for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
                if (y.contains(val)) {
                    return true;
                }
            }
            return false;
        }
        return true;
    }

    /**
     * Transform an array of int to an array of fixed IntVar
     *
     * @param values array of ints
     * @param model  model to create IntVar
     * @return
     */
    public static IntVar[] toIntVar(Model model, int... values) {
        return Arrays.stream(values).mapToObj(model::intVar).toArray(IntVar[]::new);
    }

    /**
     * @param var a variable
     * @return <i>true</i> if the variable is a constant, based on its type,
     * <i>false</i> otherwise
     */
    public static boolean isConstant(Variable var) {
        return (var.getTypeAndKind() & Variable.CSTE) != 0;
    }

    /**
     * @param var a variable
     * @return <i>true</i> if the variable is a view, based on its type,
     * <i>false</i> otherwise
     */
    public static boolean isView(Variable var) {
        return (var.getTypeAndKind() & Variable.VIEW) != 0;
    }

    /**
     * @param var a variable
     * @return <i>true</i> if the variable is an int variable, based on its kind,
     * <i>false</i> otherwise
     */
    public static boolean isInt(Variable var) {
        return (var.getTypeAndKind() & Variable.INT) != 0;
    }

    /**
     * @param var a variable
     * @return <i>true</i> if the variable is a set variable, based on its kind,
     * <i>false</i> otherwise
     */
    public static boolean isSet(Variable var) {
        return (var.getTypeAndKind() & Variable.SET) != 0;
    }

    /**
     * @param var a variable
     * @return <i>true</i> if the variable is a real variable, based on its kind,
     * <i>false</i> otherwise
     */
    public static boolean isReal(Variable var) {
        return (var.getTypeAndKind() & Variable.REAL) != 0;
    }
}
