/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.connectivity;


import org.chocosolver.util.objects.graphs.IGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Class containing algorithms to find all connected components by performing one dfs
 * it uses Tarjan algorithm in a non recursive way and can be performed in O(M+N) time c.f. Gondrand Minoux
 *
 * SEE UGVarConnectivityHelper
 *
 * @author Jean-Guillaume Fages
 */
public class ConnectivityFinder {

    //***********************************************************************************
    // CONNECTED COMPONENTS ONLY
    //***********************************************************************************

    private int n;
    private IGraph graph;
    private int[] CCFirstNode, CCNextNode, nodeCC, p, fifo, sizeCC;
    private int nbCC, sizeMinCC, sizeMaxCC;

    /**
     * Create an object that can compute Connected Components (CC) of a graph g
     * Can also quickly tell whether g is biconnected or not (only for undirected graph)
     *
     * @param g graph
     */
    public ConnectivityFinder(IGraph g) {
        graph = g;
        n = g.getNbMaxNodes();
        p = new int[n];
        fifo = new int[n];
    }

    /**
     * get the number of CC in g
     * Beware you should call method findAllCC() first
     *
     * @return nbCC the number of CC in g
     */
    public int getNBCC() {
        return nbCC;
    }

    /**
     * Get the size (number of nodes) of the smallest CC in g.
     * Beware you should call method findAllCC() first.
     * @return sizeMinCC the size of the smallest CC in g.
     */
    public int getSizeMinCC() {
        return sizeMinCC;
    }

    /**
     * Get the size (number of nodes) of the largest CC in g.
     * Beware you should call method findAllCC() first.
     * @return sizeMaxCC the size of the largest CC in g.
     */
    public int getSizeMaxCC() {
        return sizeMaxCC;
    }

    /**
     * @return The size of the CCs as an int array.
     */
    public int[] getSizeCC() {
        return sizeCC;
    }

    public int[] getCCFirstNode() {
        return CCFirstNode;
    }

    public int[] getCCNextNode() {
        return CCNextNode;
    }

    public int[] getNodeCC() {
        return nodeCC;
    }

    /**
     * Find all connected components of graph by performing one dfs
     * Complexity : O(M+N) light and fast in practice
     */
    public void findAllCC() {
        if (nodeCC == null) {
            CCFirstNode = new int[n];
            CCNextNode = new int[n];
            nodeCC = new int[n];
            sizeCC = new int[n];
        }
        sizeMinCC = 0;
        sizeMaxCC = 0;
        ISet act = graph.getNodes();
        for (int i : act) {
            p[i] = -1;
        }
        for (int i = 0; i < CCFirstNode.length; i++) {
            CCFirstNode[i] = -1;
            sizeCC[i] = -1;
        }
        int cc = 0;
        for (int i : act) {
            if (p[i] == -1) {
                findCC(i, cc);
                if (sizeMinCC == 0 || sizeMinCC > sizeCC[cc]) {
                    sizeMinCC = sizeCC[cc];
                }
                if (sizeMaxCC < sizeCC[cc]) {
                    sizeMaxCC = sizeCC[cc];
                }
                cc++;
            }
        }
        nbCC = cc;
    }

    public void findArticulationPoints() {
        for (int cc = 0; cc < getNBCC(); cc++) {

        }
    }

    private void findCC(int start, int cc) {
        int first = 0;
        int last = 0;
        int size = 1;
        fifo[last++] = start;
        p[start] = start;
        add(start, cc);
        while (first < last) {
            int i = fifo[first++];
            for (int j : graph.getSuccessorsOf(i)) {
                if (p[j] == -1) {
                    p[j] = i;
                    add(j, cc);
                    size++;
                    fifo[last++] = j;
                }
            }
            if (graph.isDirected()) {
                for (int j : graph.getPredecessorsOf(i)) {
                    if (p[j] == -1) {
                        p[j] = i;
                        add(j, cc);
                        size++;
                        fifo[last++] = j;
                    }
                }
            }
        }
        sizeCC[cc] = size;
    }

    private void add(int node, int cc) {
        nodeCC[node] = cc;
        CCNextNode[node] = CCFirstNode[cc];
        CCFirstNode[cc] = node;
    }
}
