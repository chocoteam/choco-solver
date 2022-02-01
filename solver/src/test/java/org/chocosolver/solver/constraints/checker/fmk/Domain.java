/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 26/01/13
 * Time: 13:30
 */

package org.chocosolver.solver.constraints.checker.fmk;

import gnu.trove.list.array.TIntArrayList;

import java.util.Random;

import static org.chocosolver.solver.constraints.checker.DomainBuilder.buildFullDomains;

/**
 * @author Jean-Guillaume Fages
 * @since 01/13
 */
public class Domain {
    int[] valsEnv;
    int[] valsKer;

    public Domain(int[] valsInDom) {
        this(valsInDom, null);
    }

    public Domain(int[] valsInEnv, int[] valsInKer) {
        valsEnv = valsInEnv;
        valsKer = valsInKer;
    }

    public int[] getIntDom() {
        return valsEnv;
    }

    public int[] getSetEnv() {
        return valsEnv;
    }

    public int[] getSetKer() {
        return valsKer;
    }

    public static Domain buildBoolDomain(Random r) {
        int d = r.nextInt(3);
        switch (d) {
            case 0:
                return new Domain(new int[]{0});
            case 1:
                return new Domain(new int[]{1});
            case 2:
                return new Domain(new int[]{0, 1});
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static Domain buildIntDomain(int lowerB, int ds, Random r, double density, boolean homogeneou) {
        return new Domain(buildFullDomains(1, lowerB, ds, r, density, homogeneou)[0]);
    }

    public static Domain buildSetDomain(int ds, Random r, double density, boolean homogeneou) {
        int[] env = buildFullDomains(1, 0, ds, r, density, homogeneou)[0];
        int nbK = r.nextInt(env.length);
        TIntArrayList l = new TIntArrayList(env);
        l.shuffle(r);
        int[] ker = new int[nbK];
        for (int i = 0; i < nbK; i++) {
            ker[i] = l.get(i);
        }
        return new Domain(env, ker);
    }
}
