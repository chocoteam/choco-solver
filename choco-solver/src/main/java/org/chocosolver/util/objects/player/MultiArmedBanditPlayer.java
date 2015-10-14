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
/**
 *
 */

/**
 * @author Amine Balafrej
 *
 */

package org.chocosolver.util.objects.player;

import java.io.PrintStream;
import java.util.Random;


public abstract class MultiArmedBanditPlayer {

    protected double[] scors;        // scors[i]==sum(R_i)
    protected double[] armsTemps;   // armsTemps[i]==m_i
    protected double Temps;            // m
    protected int armsOrder[];
    protected int nbArms;

    protected MultiArmedBanditPlayer(int nbArms, long seed) {
        this.nbArms = nbArms;
        scors = new double[nbArms];
        armsTemps = new double[nbArms];
        armsOrder = new int[nbArms];

        ////////////// Initialization ////////////////////////
        Temps = nbArms;
        for (int i = 0; i < nbArms; i++) {
            scors[i] = 0;
            armsTemps[i] = 1;
            armsOrder[i] = i;
        }
        shuffle(armsOrder, seed);
    }

    /**
     * @return retourne l'index du bras choisi
     */
    public abstract int chooseArm();

    /**
     * update raward of "arm"
     */
    public abstract void update(int arm, double reward);

    protected static void shuffle(int[] array, long seed) {
        Random rand = new Random(seed);
        int swapIndex;
        for (int i = array.length - 1; i > 0; i--) {
            swapIndex = rand.nextInt(i + 1);
            int temp = array[i];
            array[i] = array[swapIndex];
            array[swapIndex] = temp;
        }
    }

    public void printPourcentageArms(PrintStream out) {
        for (int i = 0; i < nbArms; i++) {
            out.print(String.format("%d: %.3f\t", armsOrder[i], (100 * (armsTemps[i] - 1) / (Temps - nbArms))));
        }
        out.println();
    }

}
