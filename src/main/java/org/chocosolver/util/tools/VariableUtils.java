/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.util.tools;

import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
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

    private static int[] bound(long... values) {
        return new int[]{
                MathUtils.safeCast(stream(values).min().getAsLong()),
                MathUtils.safeCast(stream(values).max().getAsLong())
        };
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
     * @return computes the bounds for "x / y"
     */
    public static int[] boundsForDivision(IntVar x, IntVar y) {
        return bound(
                x.getLB() / y.getLB(),
                x.getLB() / y.getUB(),
                x.getUB() / y.getLB(),
                x.getUB() / y.getUB()
        );
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
            vals[1] = Integer.MIN_VALUE;
            vals[2] = Integer.MAX_VALUE;
            vals[3] = Integer.MAX_VALUE;
        } else {
            int yl = y.getLB();
            int yu = y.getUB();
            if (yl == 0) yl = 1;
            if (yu == 0) yu = 1;
            vals[0] = x.getLB() % yl;
            vals[1] = x.getLB() % yu;
            vals[2] = x.getUB() % yl;
            vals[3] = x.getUB() % yu;
        }
        return bound(vals);
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the minimum among <i>vars</i>
     */
    public static int[] boundsForMinimum(IntVar... vars) {
        int[] bounds = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};
        IntStream.range(0, vars.length).forEach(i -> {
                    bounds[0] = Math.min(bounds[0], vars[i].getLB());
                    bounds[1] = Math.min(bounds[1], vars[i].getUB());
                }
        );
        return bounds;
    }

    /**
     * @param vars array of variables
     * @return computes the bounds for the maximum among <i>vars</i>
     */
    public static int[] boundsForMaximum(IntVar... vars) {
        int[] bounds = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
        IntStream.range(0, vars.length).forEach(i -> {
                    bounds[0] = Math.max(bounds[0], vars[i].getLB());
                    bounds[1] = Math.max(bounds[1], vars[i].getUB());
                }
        );
        return bounds;
    }

    /**
     * @param vars an array of variables
     * @return the variables' domain cardinality
     */
    public static long domainCardinality(IntVar... vars) {
        return Arrays.stream(vars).mapToInt(IntVar::getDomainSize).asLongStream().reduce(1, (a, b) -> a * b);
    }

}
