/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.graphOperations.connectivity;


import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.util.objects.graphs.IGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.ISetIterator;

import java.util.Iterator;

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

    private final int n;
    private final IGraph graph;
    private int[] CCFirstNode;
    private int[] CCNextNode;
    private int[] nodeCC;
    private final int[] p;
    private final int[] fifo;
    private int[] sizeCC;
    private int nbCC, sizeMinCC, sizeMaxCC;
    // biconnection
    private int[] numOfNode, nodeOfNum, inf;
    private final Iterator<Integer>[] iterators;
    TIntArrayList articulations;

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
        iterators = new Iterator[n];
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


    /**
     * Test biconnectivity (i.e. connected with no articulation point and no bridge)
     * only for undirected graphs
     *
     * @return true iff g is biconnected
     */
    public boolean isBiconnected() {
        assert (!graph.isDirected());
        if (graph.getNodes().size() <= 1) {
            return false;
        }
        if (inf == null) {
            nodeOfNum = new int[n];
            numOfNode = new int[n];
            inf = new int[n];
        }
        ISet act = graph.getNodes();
        for (int i : act) {
            inf[i] = Integer.MAX_VALUE;
            p[i] = -1;
            iterators[i] = graph.getSuccessorsOf(i).iterator();
        }
        //algo
        int start = act.iterator().next();
        int i = start;
        int k = 0;
        numOfNode[start] = k;
        nodeOfNum[k] = start;
        p[start] = start;
        int j, q;
        int nbRootChildren = 0;
        while (true) {
            if (iterators[i].hasNext()) {
                j = iterators[i].next();
                if (p[j] == -1) {
                    p[j] = i;
                    if (i == start) {
                        nbRootChildren++;
                        if (nbRootChildren > 1) {
                            return false;// ARTICULATION POINT DETECTED
                        }
                    }
                    i = j;
                    k++;
                    numOfNode[i] = k;
                    nodeOfNum[k] = i;
                    inf[i] = numOfNode[i];
                } else if (p[i] != j) {
                    inf[i] = Math.min(inf[i], numOfNode[j]);
                }
            } else {
                if (i == start) {
                    return k >= act.size() - 1;
                }
                q = inf[i];
                i = p[i];
                inf[i] = Math.min(q, inf[i]);
                if (q >= numOfNode[i] && i != start) {
                    return false;
                } // ARTICULATION POINT DETECTED
            }
        }
    }

    /**
     * Computes articulation points of the graph (must be connected)
     * @return the list of articulation points
     */
    public TIntArrayList getArticulationPoints() {
        if (articulations == null) articulations = new TIntArrayList();
        if (inf == null) {
            nodeOfNum = new int[n];
            numOfNode = new int[n];
            inf = new int[n];
        }
        articulations.clear();
        ISet act = graph.getNodes();
        ISetIterator iter = act.iterator();
        while (iter.hasNext()) {
            int i = iter.next();
            inf[i] = Integer.MAX_VALUE;
            p[i] = -1;
            iterators[i] = graph.getSuccessorsOf(i).iterator();
        }
        //algo
        int start = act.iterator().next();
        int i = start;
        int k = 0;
        numOfNode[start] = k;
        nodeOfNum[k] = start;
        p[start] = start;
        int j, q;
        int nbRootChildren = 0;
        while (true) {
            if (iterators[i].hasNext()) {
                j = iterators[i].next();
                if (p[j] == -1) {
                    p[j] = i;
                    if (i == start) {
                        nbRootChildren++;
                        if (nbRootChildren > 1) {
                            articulations.add(i); // ARTICULATION POINT DETECTED
                        }
                    }
                    i = j;
                    k++;
                    numOfNode[i] = k;
                    nodeOfNum[k] = i;
                    inf[i] = numOfNode[i];
                } else if (p[i] != j) {
                    inf[i] = Math.min(inf[i], numOfNode[j]);
                }
            } else {
                if (i == start) {
                    if (k < act.size() - 1) {
                        throw new UnsupportedOperationException("disconnected graph");
                    }
                    return articulations;
                }
                q = inf[i];
                i = p[i];
                inf[i] = Math.min(q, inf[i]);
                if (q >= numOfNode[i] && i != start) {
                    articulations.add(i); // ARTICULATION POINT DETECTED
                }
            }
        }
    }
}
