/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.util.objects.graphs;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A Multi-valued Decision Diagram (MDD for short) to store
 * <p>
 * Created by cprudhom on 30/10/14.
 */
public class MultivaluedDecisionDiagram  {

    /**
     * The terminal node. An extreme (likely unused) value is set
     */
    public static final int TERMINAL = -1;

    /**
     * Express "no edge" information
     */
    public static final int EMPTY = 0;

    /**
     * Store the number of variables
     */
    private final int nbLayers;
    /**
     * Initial domain size
     */
    private final int[] sizes;
    /**
     * Initial domain offset
     */
    private final int[] offsets;

    /**
     * Store all possible edges for the i^th node, 'EMPTY' otherwise
     */
    private int[] mdd;

    /**
     * Point to the first clear cell in 'mdds'
     */
    private int nextFreeCell;

    private final boolean compactOnce;
    private final boolean sortTuples;

    // TEMPORARY DATA STRUCTURE, PREFIX WITH "_", CLEARED AFTER USAGE
    private TIntIntHashMap _nodesToRemove; // store the nodes to remove and the size of each node
    private ArrayList<int[]>[][] _identicalNodes; // store child nodes of a node
    private TIntArrayList[][] _nodeId; // store node id per layer and nb of mdds
    private int _removedCells; // define the number of cells erased by the compaction
    private int[] _pos;


    private static int[][] flattenDomain(IntVar[] VARIABLES) {
        int[][] FLATDOM = new int[VARIABLES.length][];
        for (int i = 0; i < VARIABLES.length; i++) {
            int lb = VARIABLES[i].getLB();
            int ub = VARIABLES[i].getUB();
            int size = ub - lb + 1;
            FLATDOM[i] = new int[size];
            for (int j = 0; j < size; j++) {
                FLATDOM[i][j] = j + lb;
            }
        }
        return FLATDOM;
    }

    /**
     * Create an MDD based on an array of flatten domains and a set of tuples
     *
     * @param VARIABLES array of flatten domains
     * @param TUPLES    set of (allowed) tuples
     */
    public MultivaluedDecisionDiagram(IntVar[] VARIABLES, Tuples TUPLES) {
        this(flattenDomain(VARIABLES), TUPLES);
    }

    /**
     * Create an MDD based on an array of flatten domains and a set of tuples
     *
     * @param VARIABLES   array of flatten domains
     * @param TUPLES      set of (allowed) tuples
     * @param compactOnce set to true to compact the MDD after having added all the TUPLES, set to false to try to compact the MDD after each tuple addition
     * @param sortTuple   set to true to sort the TUPLES in increasing order before adding them
     */
    public MultivaluedDecisionDiagram(IntVar[] VARIABLES, Tuples TUPLES, boolean compactOnce, boolean sortTuple) {
        this(flattenDomain(VARIABLES), TUPLES, compactOnce, sortTuple);
    }

    /**
     * Create an MDD based on an array of flatten domains and a set of tuples
     *
     * @param FLATDOM array of flatten domains
     * @param TUPLES  set of (allowed) tuples
     */
    public MultivaluedDecisionDiagram(int[][] FLATDOM, Tuples TUPLES) {
        this(FLATDOM, TUPLES, true, false);
    }

    /**
     * Create an MDD based on an array of flatten domains and a set of tuples
     *
     * @param FLATDOM     array of flatten domains
     * @param TUPLES      set of (allowed) tuples
     * @param compactOnce set to true to compact the MDD after having added all the TUPLES, set to false to try to compact the MDD after each tuple addition
     * @param sortTuple   set to true to sort the TUPLES in increasing order before adding them
     */
    public MultivaluedDecisionDiagram(int[][] FLATDOM, Tuples TUPLES, boolean compactOnce, boolean sortTuple) {
        this.nbLayers = FLATDOM.length;
        this.offsets = new int[nbLayers];
        this.sizes = new int[nbLayers];
        this.compactOnce = compactOnce;
        this.sortTuples = sortTuple;
        int maxDom = 0;
        for (int i = 0; i < nbLayers; i++) {
            offsets[i] = FLATDOM[i][0];
            sizes[i] = FLATDOM[i][FLATDOM[i].length - 1] - FLATDOM[i][0] + 1;
            if (maxDom < sizes[i]) {
                maxDom = sizes[i];
            }
        }
        mdd = new int[nbLayers * maxDom];
        init(TUPLES);
    }

    @SuppressWarnings("unchecked")
    private void init(Tuples TUPLES) {
        nextFreeCell = sizes[0];
        _pos = new int[nbLayers];

        _nodesToRemove = new TIntIntHashMap(16, .5f, -1, -1);
        _identicalNodes = new ArrayList[nbLayers][];
        _nodeId = new TIntArrayList[nbLayers][];

        // Then add tuples
        if (TUPLES.nbTuples() > 0) {
            if (addTuples(TUPLES) && compactOnce) { // compact at the end, or not
                compact();
            }
        }
    }

    /**
     * Add all tuples within the MDD
     *
     * @param TUPLES tuples to add
     */
    public boolean addTuples(Tuples TUPLES) {
        if (sortTuples) TUPLES.sort();
        boolean atLeastOne = false;
        for (int t = 0; t < TUPLES.nbTuples(); t++) {
            atLeastOne |= addTuple(TUPLES.get(t));
        }
        return atLeastOne;
    }

    /**
     * Add a tuple to the MDD
     *
     * @param TUPLE tuple to add
     */
    public boolean addTuple(int[] TUPLE) {
        for (int i = 0; i < nbLayers; i++) {
            // if the tuple is out of declared domain
            if(TUPLE[i] < offsets[i] || TUPLE[i] >= offsets[i] + sizes[i]){
                return false;
            }
            // get the position of the value relatively to the offset of each variable
            _pos[i] = TUPLE[i] - offsets[i];
        }
        int p = 0;
        for (int i = 0; i < nbLayers; i++) {
            p += _pos[i];
            ensureCapacity(p + sizes[i]);
            if (mdd[p] == EMPTY) {
                if (i + 1 == nbLayers) { // if this is the last variable => terminal node
                    mdd[p] = TERMINAL;
                } else { // otherwise, create an edge to a new location, stated by nextFreeCell
                    p = mdd[p] = nextFreeCell;
                    nextFreeCell += sizes[i + 1];
                }
            } else { // if the child already exists
                p = mdd[p];
            }
        }
        if (!compactOnce) { // compact during the addition or not
            compact();
        }
        return true;
    }

    /**
     * Ensure all data structure are correctly sized.
     *
     * @param nsize new size expected
     */
    private void ensureCapacity(int nsize) {
        if (nsize > mdd.length) {
            int[] _mdd = mdd;
            mdd = new int[nsize * 3 / 2 + 1];
            System.arraycopy(_mdd, 0, mdd, 0, _mdd.length);
        }
    }


    /**
     * Compact the MDD by removing equivalent branches
     */
    @SuppressWarnings("unchecked")
    private void compact() {
        _nodesToRemove.clear();
        for (int i = 0; i < nbLayers; i++) {
            _identicalNodes[i] = new ArrayList[sizes[i]];
            _nodeId[i] = new TIntArrayList[sizes[i]];
        }
        _removedCells = 0;
        detectIsomorphism(0, 0);
        deleteIsomorphism();
    }

    /**
     * For a given node related to a given variable (defined by layer), evaluate if two or more mdds are strictly equal.
     *
     * @param node  node in the MDD
     * @param layer rank of the variable
     * @return the node id
     */
    private int detectIsomorphism(int node, int layer) {
        int[] nodeChild = new int[sizes[layer]];
        int nbChild = -1;
        for (int i = 0; i < sizes[layer]; i++) {
            switch (mdd[node + i]) {
                case EMPTY: // nothing to be done
                    break;
                case TERMINAL: // a terminal node
                    nbChild++;
                    nodeChild[i] = TERMINAL;
                    break;
                default: // a non terminal node
                    nbChild++;
                    mdd[node + i] = nodeChild[i] = detectIsomorphism(mdd[node + i], layer + 1);
                    break;
            }
        }
        boolean known = false;
        if (_identicalNodes[layer][nbChild] == null) {
            _identicalNodes[layer][nbChild] = new ArrayList<>();
            _nodeId[layer][nbChild] = new TIntArrayList();
        } else {
            for (int j = _identicalNodes[layer][nbChild].size() - 1; j >= 0; j--) {
                int[] currentNode = _identicalNodes[layer][nbChild].get(j);
                boolean found = _nodeId[layer][nbChild].get(j) != node;  // deal with previously analyzed nodes
                known |= !found;
                for (int i = currentNode.length - 1; i >= 0 && found; i--) {
                    if (currentNode[i] != nodeChild[i]) {
                        found = false;
                    }
                }
                if (found) {
                    int insert = _nodesToRemove.put(node, sizes[layer]);
                    if (insert == -1) {
                        _removedCells += sizes[layer];
                    }
                    return _nodeId[layer][nbChild].get(j);
                }
            }
        }
        if (!known) {
            _nodeId[layer][nbChild].add(node);
            _identicalNodes[layer][nbChild].add(nodeChild);
        }

        return node;
    }

    /**
     * Prune the dead branch of the MDD, based on nodeToRemove.
     */
    private void deleteIsomorphism() {
        int[] compacted = new int[nextFreeCell - _removedCells];

        int[] nodes = new int[_nodesToRemove.size() + 1];
        int[] gains = new int[_nodesToRemove.size() + 1];
        int idx = 1;
        if (_nodesToRemove.isEmpty()) {
            // If no equality detected, simply resize the array
            System.arraycopy(mdd, 0, compacted, 0, nextFreeCell);
            mdd = compacted;
        } else {
            int to, from = 0;
            int gain = 0;
            int[] keys = _nodesToRemove.keys();
            Arrays.sort(keys);
            // otherwise, iterate over nodes to remove
            for (int k : keys) {
                // node to remove
                nodes[idx] = k;
                to = nodes[idx] - nodes[idx - 1] - gain;
                System.arraycopy(mdd, nodes[idx - 1] + gain, compacted, from, to);
                from += to;
                gain = _nodesToRemove.get(k);
                gains[idx] = gains[idx - 1] + gain;
                idx++;
            }
            System.arraycopy(mdd, nodes[idx - 1] + gain, compacted, from, compacted.length - from);

            for (int i = 0; i < compacted.length; i++) {
                if (compacted[i] > EMPTY) {
                    compacted[i] -= gains[searchClosest(nodes, compacted[i])];
                }
            }
            mdd = compacted;
            nextFreeCell -= _removedCells;
        }
    }

    protected int searchClosest(int[] a, int key) {
        int low = 0;
        int high = a.length - 1;
        int mid = (low + high) >> 1;

        while (low + 1 < high) {
            if (a[mid] > key) {
                high = mid;
            } else {
                low = mid;
            }
            mid = (low + high) >> 1;
        }
        if (a[high] <= key) {
            return high;
        } else {
            return low;
        }
    }

    /**
     * Return the diagram (not a copy) of the MDD
     */
    public int[] getDiagram() {
        return mdd;
    }

    /**
     * Return the initial domain size of the variable in layer
     *
     * @param layer index of the variable
     * @return original domain size
     */
    public int getNodeSize(int layer) {
        return sizes[layer];
    }

    /**
     * Return the initial LB of the variable in layer
     *
     * @param layer index of the variable
     * @return original offset
     */
    public int getOffset(int layer) {
        return offsets[layer];
    }

    /**
     * Return the edge valued in the k^th cell of the diagram
     *
     * @param k index of the cell
     */
    public int getEdge(int k) {
        return mdd[k];
    }

    /**
     * Return true is PATH has a support in the mdd
     *
     * @param PATH array of value
     * @return true if PATH is valid
     */
    public boolean exists(int... PATH) {
        if (PATH.length == nbLayers) {
            int p = 0;
            for (int i = 0; i < nbLayers; i++) {
                p += PATH[i] - offsets[i];
                if (p >= mdd.length || mdd[p] == EMPTY) {
                    return false;
                } else if (i + 1 == nbLayers) { // if this is the last variable => terminal node
                    return mdd[p] == TERMINAL;
                } else {
                    p = mdd[p];
                }
            }
        }
        return false;
    }
}
