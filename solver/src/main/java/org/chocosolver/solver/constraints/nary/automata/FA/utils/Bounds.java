/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.automata.FA.utils;

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
    }

    private Bounds(int minValue, int minPrefered, int maxValue, int maxPrefered) {
        min = new MinMax();
        max = new MinMax();

        min.value = minValue;
        min.prefered = minPrefered;

        max.value = maxValue;
        max.prefered = maxPrefered;
    }

    public static Bounds makeBounds(int minValue, int minPrefered, int maxValue, int maxPrefered) {
        return new Bounds(minValue, minPrefered, maxValue, maxPrefered);
    }

    public static void main(String[] args) {
        Bounds a = new Bounds(0, 0, 0, 0);
        Bounds b = new Bounds(9, 9, 9, 9);

        System.out.println(b.min.prefered);
        System.out.println(a.min.prefered);
    }
}
