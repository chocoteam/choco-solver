/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 26/01/13
 * Time: 13:30
 */

package org.chocosolver.choco.checker.fmk;

import gnu.trove.list.array.TIntArrayList;

import java.util.Random;

import static org.chocosolver.choco.checker.DomainBuilder.buildFullDomains;

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
