/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package solver.search.strategy.enumerations;

import solver.variables.IntVar;

import java.util.Arrays;
import java.util.Random;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 1 oct. 2010
 */
public class DomainBuilder {

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

}
