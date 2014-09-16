/*
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package samples.graph.input;

import gnu.trove.list.array.TIntArrayList;

import java.util.BitSet;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 20/12/11
 */
public class GraphGenerator {

    public enum InitialProperty {
        HamiltonianCircuit, Tree, None
    }

    Random rand;
    int size;
    boolean[][] graph;

    /**
     * insatnce of graph generator
     *
     * @param size number of nodes in the directed graph generated
     * @param seed uniform random distribution from a given integer
     * @param prop property insured by the generator
     */
    public GraphGenerator(int size, long seed, InitialProperty prop) {
        this.size = size;
        this.rand = new Random(seed);
        this.graph = new boolean[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.graph[i][j] = false;
            }
        }
        switch (prop) {
            case HamiltonianCircuit:
                this.generateInitialHamiltonianCircuit();
                break;
            case Tree:
                this.generateInitialTree();
                break;
            default:
                break;
        }
    }

    /**
     * randomly generate a boolean matrix representing a directed graph
     *
     * @param density arc ratio among all the possible
     * @return a boolean matrix
     */
    public boolean[][] arcBasedGenerator(double density) {
        // on ajoute des arcs
        int nb = (int) (density * ((size * size) - size));
        int cur = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    cur++;
                }
            }
        }
        nb = nb - cur;
        while (nb > 0) {
            int i = rand.nextInt(size);
            BitSet seti = new BitSet(size);
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    seti.set(j, false);
                } else {
                    seti.set(j, true);
                }
            }
            seti.set(i, false);
            int y = rand.nextInt(size);
            int j = seti.nextSetBit(y);
            if (j == -1) {
                j = seti.nextSetBit(0);
            }
            if (j != -1) {
                seti.set(j, false);
                graph[i][j] = true;
                nb--;
            }
        }
        return this.graph;
    }

    /**
     * randomly generate a boolean matrix representing a directed graph
     *
     * @param nb number of neighbor for each node, necessarily < size
     * @return a boolean matrix
     */
    public boolean[][] neighborBasedGenerator(int nb) {
        // on ajoute des arcs: exactement nb pour chaque sommet
        for (int i = 0; i < size; i++) {
            int ni = 0;
            BitSet seti = new BitSet(size);
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) {
                    seti.set(j, false);
                    ni++;
                } else {
                    seti.set(j, true);
                }
            }
            seti.set(i, false);
            int ti = nb - ni;
            while (ti > 0) {
                int y = rand.nextInt(size);
                int j = seti.nextSetBit(y);
                if (j == -1) {
                    j = seti.nextSetBit(0);
                }
                seti.set(j, false);
                graph[i][j] = true;
                ti--;
            }
        }
        return this.graph;
    }

    public String toString() {
        String g = "";
        for (int i = 0; i < size; i++) {
            g += i + ":";
            for (int j = 0; j < size; j++) {
                if (graph[i][j]) g += j + " ";
            }
            g += "\n";
        }
        return g;
    }

    /**
     * Provide an initial Hamiltonian circuit in graph
     */
    private void generateInitialHamiltonianCircuit() {
//        //int[] perm = new int[size];
//        BitSet nodes = new BitSet(size);
//        for (int i = 0; i < size; i++) {
//            nodes.set(i, true);
//        }
//        int start = rand.nextInt(size);
//        int i = start;
//        nodes.set(i, false);
//        int j;
//        do {
//            int idj = rand.nextInt(size);
//            j = nodes.nextSetBit(idj);
//            if (j == -1) {
//                j = nodes.nextSetBit(0);
//            }
//            if (j != -1) {
//                nodes.set(j, false);
//                graph[i][j] = true;
//                i = j;
//            }
//        } while (j != -1);
//        graph[i][start] = true;
        TIntArrayList nodes = new TIntArrayList(size);
        for (int i = 0; i < size; i++) {
            nodes.add(i);
        }
        nodes.shuffle(rand);
        for (int i = 0; i < size - 1; i++) {
            graph[nodes.get(i)][nodes.get(i + 1)] = true;
        }
        graph[nodes.get(size - 1)][nodes.get(0)] = true;
    }

    /**
     * Provide an initial tree in graph
     */
    private void generateInitialTree() {
        BitSet notIn = new BitSet(size);
        BitSet in = new BitSet(size);
        for (int i = 0; i < size; i++) {
            notIn.set(i, true);
            in.set(i, false);
        }
        while (notIn.cardinality() > 0) {
            int i = pickOneTrue(notIn);
            notIn.set(i, false);
            // relier i a un sommet de in quelconque.
            int j;
            int sj = rand.nextInt(size);
            j = in.nextSetBit(sj);
            if (j == -1) {
                j = in.nextSetBit(0);
            }
            // cas du premier sommet ajoute dans in
            if (j > -1) {
                // pas de pbs car i et j ne peuvent pas etre tous deux dans in
                this.graph[i][j] = true;
            }
            in.set(i, true);
        }
    }

    private int pickOneTrue(BitSet tab) {
        int start = rand.nextInt(tab.length());
        int i = tab.nextSetBit(start);
        if (i == -1) {
            i = tab.nextSetBit(0);
        }
        return i;
    }

    public static void main(String[] args) {
        int n = 4;
        int neighbor = 2;
        double density = 0.5;
        int seed = 0;
        InitialProperty prop = InitialProperty.HamiltonianCircuit;
        GraphGenerator gen = new GraphGenerator(n, seed, prop);
        gen.arcBasedGenerator(density);
        System.out.println(gen.toString());
        prop = InitialProperty.Tree;
        gen = new GraphGenerator(n, seed, prop);
        gen.neighborBasedGenerator(neighbor);
        System.out.println(gen.toString());
    }

}
