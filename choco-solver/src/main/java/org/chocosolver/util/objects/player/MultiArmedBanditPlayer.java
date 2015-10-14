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
