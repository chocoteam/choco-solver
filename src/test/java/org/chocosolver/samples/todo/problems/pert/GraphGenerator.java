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
package org.chocosolver.samples.todo.problems.pert;

import java.util.BitSet;
import java.util.Random;

/**
 * <br/>
 *
 * @author Xavier Lorca
 * @since 31/03/11
 */
public class GraphGenerator {

    int n;
    int nbPrec;
    int nbNeq;
    Random rand;
    int idx;
    int[] dfsTree;

    //static Pair pair = new Pair(0, 0);

    // nbPrec < (n^2-n)/2
    public GraphGenerator(int n, int nbPrec, int nbNeq, Random rand) {
        this.n = n;
        this.nbPrec = nbPrec;
        if (nbPrec > ((n * n) - n) / 2) {
            throw new UnsupportedOperationException();
        }
        this.nbNeq = nbNeq;
        this.rand = rand;
    }

    // graph restreint aux contraintes de type 1 est acyclique
    // si graph[i][j] = 1 alors var(i) < var(j)
    // si graph[i][j] = 2 alors var(i) =/= var(j)
    public int[][] generate() {
        int[][] graph = new int[n][n];

        BitSet precs = new BitSet();
        for (int i = 0; i < n - 1; i++) {
            for (int j = i + 1; j < n; j++) {
                int ij = i * n + j;
                precs.set(ij, true);

            }
        }


        // on bouche jusqu'a la limite pour creer les contraintes 1
        while (nbPrec > 0) {
            //Pair p = precs.get(rand.nextInt(precs.size()));
            int ij = precs.nextSetBit(0);
            int index = rand.nextInt(precs.cardinality());
            while (index > 0) {
                ij = precs.nextSetBit(ij + 1);
                index--;
            }
            assert (ij > -1);
            graph[ij / n][ij % n] = 1;
            precs.set(ij, false);
            nbPrec--;
        }

        // on assure que chaque sommet i > 0 a un pred
        for (int i = 1; i < n; i++) {
            int k = 0;
            while (k < n && graph[k][i] != 1) {
                k++;
            }
            if (k == n) {
                graph[0][i] = 1;
                precs.set(i, false);
            }
        }

        // on assure que chaque sommet i < n-1 a un succ
        for (int i = 0; i < n - 1; i++) {
            int k = i + 1;
            while (k < n && graph[i][k] != 1) {
                k++;
            }
            if (k == n) {
                graph[i][n - 1] = 1;
                precs.set((i * n) + n - 1, false);
            }
        }

        // on bouche jusqu'a la limite pour creer les contraintes 2
        BitSet neqs = new BitSet();
        neqs.or(precs);
        while (nbNeq > 0 && neqs.cardinality() > 0) {
            int ij = neqs.nextSetBit(0);
            int index = rand.nextInt(neqs.cardinality());
            while (index > 0) {
                ij = neqs.nextSetBit(ij + 1);
                index--;
            }
            assert (ij > -1);
            graph[ij / n][ij % n] = 2;
            neqs.set(ij, false);
            nbNeq--;
        }
        computeTRfromScratch(graph);
        return graph;
    }

    private void computeTRfromScratch(int[][] graph) {
        dfsTree = new int[n];
        for (int i = 0; i < n; i++) {
            int[][] num = new int[n][2];
            for (int j = 0; j < n; j++) {
                num[j][0] = -1;
                num[j][1] = -1;
            }
            idx = 0;
            for (int k = 0; k < n; k++) {
                dfsTree[k] = -1;
            }
            dfs(i, i, num, graph);
        }
    }

    private int[][] dfs(int root, int u, int[][] num, int[][] trgraph) {
        num[u][0] = idx++;
        for (int v = 0; v < n; v++) {
            if (trgraph[u][v] == 1) {
                if (num[v][0] == -1) {
                    dfsTree[v] = u;
                    num = dfs(root, v, num, trgraph);
                } else {
                    if (num[u][1] == -1 && num[u][0] > num[v][0]) {
                        int w = dfsTree[v];
                        if (w == root) { // (w,v) is a transitive arc in the dfs tree
                            trgraph[w][v] = 0;
                        }
                    }
                    // (u,v) is a transitive arc in a specific branch of the dfs tree
                    if (num[v][1] != -1 && num[u][0] < num[v][0]) {
                        trgraph[u][v] = 0;
                    }
                }
            }
        }
        num[u][1] = idx++;
        return num;
    }

    public static void main(String[] args) {
        int n = 5;
        int nbPrecs = 10;
        int nbNeqs = 0;
        System.out.println("******* " + nbPrecs + " ********");
        Random rand = new Random(0);
        long t = System.currentTimeMillis();
        GraphGenerator gen = new GraphGenerator(n, nbPrecs, nbNeqs, rand);
        int[][] graph = gen.generate();
        t = System.currentTimeMillis() - t;
        System.out.println("time : " + t);
        for (int i = 0; i < n; i++) {
            System.out.print(i + ": ");
            for (int j = 0; j < n; j++) {
                if (graph[i][j] == 1) {
                    System.out.print(j + " ");
                }
                if (graph[i][j] == 2) {
                    System.out.print("(" + j + ")" + " ");
                }
            }
            System.out.println();
        }//*/
    }

}

