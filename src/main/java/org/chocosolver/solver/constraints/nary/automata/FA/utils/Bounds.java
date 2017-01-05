/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA.utils;

import org.chocosolver.solver.constraints.nary.automata.penalty.IPenaltyFunction;

/**
 * Created by IntelliJ IDEA.
 * User: julien
 * Date: Nov 23, 2010
 * Time: 11:12:10 AM
 */
public class Bounds {

    public MinMax min, max;

    public class MinMax {
        public int value = Integer.MIN_VALUE;
        public int prefered = Integer.MIN_VALUE;
        public IPenaltyFunction penalty = null;


    }

    private Bounds(int minValue, int minPrefered, IPenaltyFunction minPenaltyFunction,
                   int maxValue, int maxPrefered, IPenaltyFunction maxPenaltyFunction) {
        min = new MinMax();
        max = new MinMax();

        min.value = minValue;
        min.prefered = minPrefered;
        min.penalty = minPenaltyFunction;

        max.value = maxValue;
        max.prefered = maxPrefered;
        max.penalty = maxPenaltyFunction;

    }

    public static Bounds makeBounds(int minValue, int minPrefered, IPenaltyFunction minPenaltyFunction,
                                    int maxValue, int maxPrefered, IPenaltyFunction maxPenaltyFunction) {
        return new Bounds(minValue, minPrefered, minPenaltyFunction, maxValue, maxPrefered, maxPenaltyFunction);
    }

    public static Bounds makeMinBounds(int minValue, int minPrefered, IPenaltyFunction minPenaltyFunction) {
        return new Bounds(minValue, minPrefered, minPenaltyFunction, Integer.MIN_VALUE, Integer.MIN_VALUE, null);
    }

    public static Bounds makeMaxBounds(int maxValue, int maxPrefered, IPenaltyFunction maxPenaltyFunction) {
        return new Bounds(Integer.MIN_VALUE, Integer.MIN_VALUE, null, maxValue, maxPrefered, maxPenaltyFunction);
    }


    public static void main(String[] args) {
        Bounds a = new Bounds(0, 0, null, 0, 0, null);

        Bounds b = new Bounds(9, 9, null, 9, 9, null);


        System.out.println(b.min.prefered);
        System.out.println(a.min.prefered);


    }


}
