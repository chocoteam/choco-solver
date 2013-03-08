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
package sandbox;

import java.util.BitSet;

public class test {

    private static final int NUM_BITS = 64;
    //every other bit set
    private static long setMask = 0xAAAAAAAAAAAAAAAAl;

    private static long nativeBitTest() {
        long bits = 0;
        int found = 0;
        bits |= setMask;

        long start = System.currentTimeMillis();

        for (int bit = 0; bit < NUM_BITS; ++bit) {
            if (((bits >>> bit) & 0x1) == 0) {
                ++found;
            }
        }

        return System.currentTimeMillis() - start;
    }

    private static long bitsetBitTest() {
        BitSet bits = new BitSet(NUM_BITS);
        for (int bit = 1; bit < NUM_BITS; bit += 2) {
            bits.set(bit);
        }

        long start = System.currentTimeMillis();

        int found = 0;
        for (int i = bits.nextClearBit(0); i < NUM_BITS; i = bits
                .nextClearBit(i + 1)) {
            ++found;
        }

        return System.currentTimeMillis() - start;
    }

    public static void main(String[] args) {
        double totalNative, totalBitset = totalNative = 0;

        for (double i = 0; i < 1000000; ++i) {
            totalNative += nativeBitTest();
            totalBitset += bitsetBitTest();
        }

        System.out.println("Native: " + totalNative / 10000.0 + " BitSet: "
                + totalBitset / 10000.0);
        /*for (int k = 0; k < 20; k++) {
            IEnvironment env = new EnvironmentTrailing();
            int n = 9999999;
            S64BitSet bs = new S64BitSet(env, n);
            bs.set(0, n);
            long t = -System.nanoTime();
            for (int i = 0; i < n; i++) {
                bs.clear(i);
            }
            t += System.nanoTime();
            System.out.printf("%fms\n", t / 1000.0 / 1000.0);
        }

        for (int k = 0; k < 20; k++) {
            IEnvironment env = new EnvironmentTrailing();
            int n = 9999999;
            S64BitSet bs = new S64BitSet(env, n);
            bs.set(0, n);
            long t = -System.nanoTime();
            for (int i = 0; i < n /8 +1; i++) {
                bs.clear(i * 8, (i+1)*8);
            }
            t += System.nanoTime();
            System.out.printf("%fms\n", t / 1000.0 / 1000.0);
        }*/

    }
}
