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
package samples.pert;

import java.util.BitSet;
import java.util.Random;

/**
 * <br/>
 *
 * @author Xavier Lorca
 * @since 31/03/11
 */
public class GraphGenerator2 {

    int nbC;
    int nbS;
    int nbAd;
    Random rand;

    BitSet[] layers;
    BitSet[] allDiffLayers;
    int[][] graph;

    public GraphGenerator2(int nbC, int nbS, int nbAd, long seed) {
        this.nbC = nbC;
        this.nbS = nbS;
        this.nbAd = nbAd;
        this.rand = new Random(seed);
        if (nbC > nbS) {
            throw new RuntimeException("nbC > nbS");
        }
        if (nbAd > nbC - 2) {
            throw new RuntimeException("nbAd > nbC - 2");
        }
    }

    public void generate() {
        layers = new BitSet[nbC];
        BitSet available = new BitSet(nbS);

        available.set(0, nbS, true);
        for (int l = 0; l < layers.length - 1; l++) {
            layers[l] = new BitSet(nbS);
            layers[l].set(l, true);
            available.set(l, false);
        }
        layers[nbC - 1] = new BitSet(nbS);
        layers[nbC - 1].set(nbS - 1, true);
        available.set(nbS - 1, false);


        for (int i = available.nextSetBit(0); i >= 0; i = available.nextSetBit(i + 1)) {
            int layer = 1 + rand.nextInt(nbC - 2);
            layers[layer].set(i, true);
            available.set(i, false);
        }

        BitSet[] succs = new BitSet[nbS];
        for (int l = 0; l < nbS; l++) {
            succs[l] = new BitSet(nbS);
        }

        for (int l = 0; l < layers.length - 1; l++) {
            BitSet layer = layers[l];
            for (int i = layer.nextSetBit(0); i >= 0; i = layer.nextSetBit(i + 1)) {
                BitSet layer2 = layers[l + 1];
                for (int j = layer2.nextSetBit(0); j >= 0; j = layer2.nextSetBit(j + 1)) {
                    succs[i].set(j, true);
                }
            }
        }

        allDiffLayers = new BitSet[nbAd];
        BitSet activeLayers = new BitSet(nbC);
        activeLayers.set(1, nbC - 1, true);
        for (int i = 0; i < nbAd; i++) {
            allDiffLayers[i] = new BitSet(nbS);
            int k = rand.nextInt(activeLayers.cardinality());
            int v = activeLayers.nextSetBit(0);
            while (k > 0) {
                v = activeLayers.nextSetBit(v + 1);
                k--;
            }
            allDiffLayers[i].or(layers[v]);
            activeLayers.set(v, false);
        }


        graph = new int[nbS][nbS];

        // connect source to all nodes of first layer
        for (int i = layers[1].nextSetBit(0); i >= 0; i = layers[1].nextSetBit(i + 1)) {
            graph[0][i] = 1;
        }
        // connect all nodes of last layer to sink
        for (int i = layers[nbC - 2].nextSetBit(0); i >= 0; i = layers[nbC - 2].nextSetBit(i + 1)) {
            graph[i][nbS - 1] = 1;
        }

        // connections between layer i and layer i+1
        for (int i = 1; i < layers.length - 2; i++) {
            int ci = layers[i].cardinality();
            int ci_1 = layers[i + 1].cardinality();
            // nb_acrs = [nb_nodes(i),nb_nodes(i) * nb_nodes(i+1)]
            int nbArcs = ci;
            if (ci_1 > 1) {
                nbArcs += rand.nextInt((ci * ci_1) - ci);
            }
            for (int j = 0; j < nbArcs; j++) {
                int k1 = rand.nextInt(ci);
                int f = layers[i].nextSetBit(0);
                while (k1 > 0) {
                    f = layers[i].nextSetBit(f + 1);
                    k1--;
                }
                int k2 = rand.nextInt(succs[f].cardinality());
                int t = succs[f].nextSetBit(0);
                while (k2 > 0) {
                    t = succs[f].nextSetBit(t + 1);
                    k2--;
                }
                graph[f][t] = 1;
                succs[f].set(t, false);
                if (succs[f].isEmpty()) {
                    layers[i].set(f, false);
                    ci--;
                }
            }
        }

    }

    public BitSet[] getAllDiffLayers() {
        return allDiffLayers;
    }

    public int[][] getGraph() {
        return graph;
    }

    public static void main(String[] args) {
        GraphGenerator2 gg = new GraphGenerator2(5000, 10000, 2000, 29091981);
        gg.generate();
//        for (int i = 0; i < gg.graph.length; i++) {
//            System.out.printf("%s\n", Arrays.toString(gg.graph[i]));
//        }
//        System.out.printf("%s\n", gg.allDiffLayers);
    }

}

