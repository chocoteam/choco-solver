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
package org.chocosolver.util.graphOperations.dominance;

/**
 * Class containing some Bit operations useful to LCA queries
 *
 * @author Jean-Guillaume Fages
 */
public class BitOperations {

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    // LCA typical operations

    /**
     * Get the lowest common ancestor of x and y in a complete binary tree
     *
     * @param x a node
     * @param y a node
     * @return the lowest common ancestor of x and y in a complete binary tree
     */
    public static int binaryLCA(int x, int y) {
        if (x == y) {
            return x;
        }
        int xor = x ^ y;
        int idx = getMaxExp(xor);
        if (idx == -1) {
            throw new UnsupportedOperationException();
        }
        return replaceBy1and0sFrom(x, idx);
    }

    /**
     * @param x   value/node
     * @param idx index to place the 1 followed by zeroes
     * @return a new int that values x until idx (excluded) and 1(000...) then
     */
    public static int replaceBy1and0sFrom(int x, int idx) {
        x = x >>> idx + 1;
        x = x << 1;
        x++;
        x = x << (idx);
        return x;
    }

    // Exponents getters

    /**
     * @param x a node
     * @return the index of the last 1-bit, starting from 0
     *         0 return -1 if no bit is set to 1
     */
    public static int getMaxExp(int x) {
        int exp = -1;
        while (x > 0) {
            exp++;
            x /= 2;
        }
        return exp;
    }

    /**
     * @param x node
     * @param j value
     * @return the index of the last 1-bit which is lower than j, starting from 0
     *         return -1 if no such bit exists
     */
    public static int getMaxExpBefore(int x, int j) {
        x %= pow(2, j);
        return getMaxExp(x);
    }

    /**
     * @param x a node
     * @return the index of the first 1-bit, starting from 0
     *         return -1 if no such bit exists
     */
    public static int getFirstExp(int x) {
        if (x == 0) {
            return -1;
        }
        int exp = 0;
        while (x % 2 == 0) {
            exp++;
            x /= 2;
        }
        return exp;
    }

    /**
     * @param x a node
     * @param y a node
     * @param i a value
     * @return the index of the first 1-bit in both x and y, which is greater or equal to i,
     *         return -1 if no such bit exists
     */
    public static int getFirstExpInBothXYfromI(int x, int y, int i) {
        x = x >>> i;
        y = y >>> i;
        if ((x & y) == 0) { // x and y have no 1-bit >=i in common
            return -1;
        }
        while (x % 2 == 0 || y % 2 == 0) {
            i++;
            x /= 2;
            y /= 2;
        }
        return i;
    }

    // power

    /**
     * @param x a node
     * @param pow must be >= 0
     * @return x^pow
     */
    public static int pow(int x, int pow) {
        if (pow < 0) {
            throw new UnsupportedOperationException();
        }
        if (pow == 0) {
            return 1;
        }
        pow--;
        int xp = x;
        while (pow > 0) {
            pow--;
            xp *= x;
        }
        return xp;
    }
}
