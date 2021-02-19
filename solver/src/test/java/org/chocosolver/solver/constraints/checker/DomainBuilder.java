/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public class DomainBuilder {


    private static final int MIN_LOW_BND = -15;

    private static final int MAX_DOM_SIZE = 30;

    public static int[][] buildFullDomains(int nbVar, int minV, int maxV) {
        int sizeMax = maxV - minV + 1;
        int[] values = new int[sizeMax];
        for (int k = 0; k < sizeMax; k++) {
            values[k] = k + minV;
        }
        int[][] domains = new int[nbVar][];
        for (int v = 0; v < nbVar; v++) {
            domains[v] = values.clone();
        }
        return domains;
    }

    public static int[][] buildFullDomains(int nbVar, int size, int range, Random rand) {
        int[][] domains = new int[nbVar][];
        for (int v = 0; v < nbVar; v++) {
            domains[v] = new int[2];
            domains[v][0] = -rand.nextInt(range) + rand.nextInt(range);
            domains[v][1] = domains[v][0] + (size - 1);
        }
        return domains;
    }

    public static int[][] buildFullDomains(int nbVar, int minV, int maxV, Random r, double density, boolean homogeneous) {
        int sizeMax = maxV - minV + 1;
        int[] values = new int[sizeMax];
        for (int k = 0; k < sizeMax; k++) {
            values[k] = k + minV;
        }
        int size = Math.max(1, (int) (sizeMax * density));
        int[][] domains = new int[nbVar][];
        for (int v = 0; v < nbVar; v++) {
            int _size = size;
            if (!homogeneous) {
                _size = r.nextInt(size) + 1;  // 1<= _size <= size
            }
            domains[v] = cutAndSort(_size, randomPermutations(values, r));
        }
        return domains;
    }


    public static int[][] buildFullDomains2(int nbVar, int size, int range, Random rand, double density, boolean homogeneous) {
        int[] values = new int[size];
        int minV = -rand.nextInt(range) + rand.nextInt(range);
        for (int k = 0; k < size; k++) {
            values[k] = k + minV;
        }
        size = Math.max(1, (int) (size * density));
        int[][] domains = new int[nbVar][];
        for (int v = 0; v < nbVar; v++) {
            int _size = size;
            if (!homogeneous) {
                _size = rand.nextInt(size) + 1;  // 1<= _size <= size
            }
            domains[v] = cutAndSort(_size, randomPermutations(values, rand));
        }
        return domains;
    }

    private static int[] randomPermutations(int[] tab, Random r) {
        int l = tab.length;
        for (int i = 0; i < l; i++) {
            int j = r.nextInt(l);
            int tmp = tab[i];
            tab[i] = tab[j];
            tab[j] = tmp;
        }
        return tab;
    }

    private static int[] cutAndSort(int size, int[] tab) {
        int[] tmp = new int[size];
        System.arraycopy(tab, 0, tmp, 0, size);
        Arrays.sort(tmp);
        return tmp;
    }


    public static int[][] buildDomainsFromVar(IntVar[] vars) {
        int[][] domains = new int[vars.length][];
        for (int j = 0; j < vars.length; j++) {
            domains[j] = new int[vars[j].getDomainSize()];
            int k = 0;
            int ub = vars[j].getUB();
            for (int val = vars[j].getLB(); val <= ub; val = vars[j].nextValue(val)) {
                domains[j][k++] = val;
            }
        }
        return domains;
    }

    public static Integer makeInt(Random rnd) {
        return MIN_LOW_BND + rnd.nextInt(MAX_DOM_SIZE);
    }

    public static IntVar makeIntVar(Model model, Random rnd, int pos) {
        IntVar var;
        String name = "X_" + pos;
        switch (rnd.nextInt(3)) {
            default:
            case 0: // bounded
                int lb = rnd.nextInt(MAX_DOM_SIZE) - MIN_LOW_BND;
                int ub = rnd.nextInt(MAX_DOM_SIZE) - MIN_LOW_BND;
                if (lb < ub) {
                    var = model.intVar(name, lb, ub);
                } else {
                    var = model.intVar(name, ub, lb);
                }
                break;
            case 1: // enumerated
                int[] values = rnd.ints(1 + rnd.nextInt(MAX_DOM_SIZE - 1), MIN_LOW_BND, MIN_LOW_BND + MAX_DOM_SIZE).toArray();
                var = model.intVar(name, values);
                break;
            case 2: // boolean
                int v = rnd.nextInt(5);
                switch (v) {
                    case 0:
                        var = model.boolVar(name, false);
                        break;
                    case 1:
                        var = model.boolVar(name, true);
                        break;
                    default:
                        var = model.boolVar(name);
                }
                break;

        }
        return var;
    }

    public static BoolVar makeBoolVar(Model model, Random rnd, int pos) {
        BoolVar var;
        String name = "B_" + pos;
        int v = rnd.nextInt(5);
        switch (v) {
            case 0:
                var = model.boolVar(name, false);
                break;
            case 1:
                var = model.boolVar(name, true);
                break;
            default:
                var = model.boolVar(name);
        }
        return var;
    }

}
