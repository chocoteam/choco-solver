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

package solver.constraints.propagators.nary.matching;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateInt;
import choco.kernel.memory.IStateIntVector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.variables.IntVar;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 30 nov. 2010
 */
public class MatchingStructure implements Serializable {

    protected final static Logger LOGGER = LoggerFactory.getLogger(MatchingStructure.class);

    public Node[] nodes;

    //Vector with the reverse matching.
    protected IStateIntVector refInverseMatch;
    // slots storing the graph as a matching
    protected int nbLeftVertices, nbRightVertices, nbVertices;
    protected int minValue = Integer.MIN_VALUE;
    protected int maxValue = Integer.MAX_VALUE;
    protected int source;
    protected IStateInt matchingSize;     // and its size = #{i | refMatch[i] != 0}
    protected int[] left2rightArc, right2leftArc; // storing the alternating forest (in the search for augmenting paths)
    protected IntQueue queue;

    // slots for algorithm computing the strongly connected components
    // temporary data structure: markers, iterators, ....
    protected int time = 0;               // a time counter
    protected int[] finishDate;           // finishDate[i] : value of time when the expansion of i was completed in DFS
    protected boolean[] seen;             // seen[i]=true <=> node i has been expanded in DFS
    protected int currentNode = -1;       // the current node in the second DFS exploration
    protected int currentComponent = -1;  // a counter used when building the solution
    // the solution
    protected int[] component;            // storing the solution: component[i] is the index of strong con. comp. oof i
    protected boolean[][] componentOrder; // componentOrder[i,j]=true <=> there exists an edge in the SCC graph from
    // component i to component j

    /**
     * Constructor
     *
     * @param vars        the graph, a left vextex per vars, a right vertex per domain value
     * @param nbLeft      number of left nodes, = vars.length
     * @param nbRight     number of right nodes, domain values of vars
     * @param environment memory management
     */
    public MatchingStructure(IntVar[] vars, int nbLeft, int nbRight, IEnvironment environment) {
        this.nbLeftVertices = nbLeft;
        this.nbRightVertices = nbRight;
        this.nbVertices = this.nbLeftVertices + this.nbRightVertices + 1;

        this.matchingSize = environment.makeInt(0);
        this.queue = new IntQueue(this.nbVertices - 1);
        this.left2rightArc = new int[this.nbLeftVertices];
        this.right2leftArc = new int[this.nbRightVertices];
        this.source = this.nbVertices - 1;
        this.finishDate = new int[this.nbVertices]; // Default value : 0
        this.seen = new boolean[this.nbVertices]; // Default value : false
        this.component = new int[this.nbVertices];
        for (int i = 0; i < component.length; i++) {
            component[i] = -1;
        }
        this.componentOrder = new boolean[this.nbVertices][this.nbVertices];
        for (int i = 0; i < this.nbVertices; i++) {
            this.componentOrder[i][i] = true;
        }
        this.refInverseMatch = environment.makeIntVector(this.nbRightVertices, -1);
        minValue = Integer.MAX_VALUE;
        maxValue = Integer.MIN_VALUE;
        nodes = new Node[vars.length];
        for (int i = 0; i < vars.length; i++) {
            nodes[i] = new Node(vars[i], environment);
            minValue = Math.min(vars[i].getLB(), minValue);
            maxValue = Math.max(vars[i].getUB(), maxValue);
        }
    }

    public int getMinValue() {
        return minValue;
    }

    /**
     * when an edge is definitely chosen in the bipartite assignment graph.
     *
     * @param i          idx src
     * @param val        value
     * @param propagator propagator who deals with this event
     * @throws solver.exception.ContradictionException
     *          fail
     */
    public boolean updateMatchingOnInstantiation(int i, int val, Propagator propagator) throws ContradictionException {
        int j = val - minValue;
        boolean effect = this.setMatch(i, j);
        nodes[i].forceEdge(val);
        for (int i2 = 0; i2 < this.nbLeftVertices; i2++) {
            if (i2 != i) {
                if (nodes[i2].remove(val, propagator)) {
                    effect |= this.deleteMatch(i2, j);
                }
            }
        }
        return effect;
    }

    protected boolean remove(int i, int j, Propagator propagator) throws ContradictionException {
        Node n = nodes[i];
        int val = j + minValue;
        if (n.var.getDomainSize() == 2 && n.var.contains(val)) {
            int vi = n.var.nextValue(val);
            if (vi == Integer.MAX_VALUE) {
                vi = n.var.previousValue(val);
            }
            n.var.instantiateTo(vi, propagator);
            n.forceEdge(vi);
            return updateMatchingOnInstantiation(i, vi, propagator);
        } else {
            deleteMatch(i, j);
            return n.remove(val, propagator);
        }
    }


    /**
     * when an edge is definitely removed from the bipartite assignment graph.
     *
     * @param i          idx src
     * @param j          idx dest
     * @param propagator propagator who deals with this event
     * @return if there is something to delete
     * @throws ContradictionException fail on removal
     */
    protected boolean updateMatchingOnRemoval(int i, int j, Propagator propagator) throws ContradictionException {
        return nodes[i].remove(j + this.minValue, propagator) && this.deleteMatch(i, j);
    }

    // ==============================================================
    //           GENERIC IMPLEMENTATION OF A BIPARTITE GRAPH
    // ==============================================================

    /**
     * Accessing the edges of the bipartite graph access from the left vertex set:
     * reading domains of modeling variables
     *
     * @param i the left vertex
     * @return the set of right vertices that can be matched to i
     */
    public int[] mayMatch(int i) {
        return nodes[i].edges(this.minValue);
    }

    /**
     * reverse, access from the right vertex set:
     * iterating over the variables (left vertex set) and reading their domains
     *
     * @param j the right vertex
     * @return the set of left vertices that can be matched to j
     */
    public int[] mayInverseMatch(int j) {
        int[] ret = new int[this.nbLeftVertices];
        int nb = 0;
        for (int i = 0; i < this.nbLeftVertices; i++) {
            if (nodes[i].contains(j + this.minValue)) {
                ret[nb++] = i;
            }
        }
        int[] ret2 = new int[nb];
        System.arraycopy(ret, 0, ret2, 0, nb);
        return ret2;
    }

    /**
     * @param i left vextex index
     * @return accessing the right vertex matched to i
     */
    public int match(int i) {
        return nodes[i].getRefMatch();
    }

    /**
     * @param i a left vertex
     * @return whether the flow from i to the sink may be increased
     */
    public boolean mayGrowFlowToSink(int i) {
        return this.match(i) == -1;
    }

    /**
     * @param j a right vertex
     * @param i a left vertex
     * @return whether the flow from j to i may be increased
     *         (meaning whether that the additional flow is able to arrive to j, we don't care yet
     *         whether it will be able to leave i)
     */
    public boolean mayGrowFlowBetween(int j, int i) {
        return this.match(i) != j;
    }


    // ==============================================================
    //            FINDING AN AUGMENTING PATH IN THE GRAPH
    // ==============================================================

    /**
     * First pass: use Ford & Fulkerson algorithm to compute a reference flow (assignment)
     * finds an augmenting path using a fifo queue
     *
     * @return 0 if none found, otherwise the end of the path
     */
    public int findAlternatingPath() {
        int eopath = -1;
        int n = this.nbLeftVertices;
        this.queue.init();
        if (this.queue.getSize() == 0) {
            for (int j = 0; j < this.nbRightVertices; j++) {
                // otherwise enqueue vertives of V2 whose upper bounds haven't been reached
                if (this.mayGrowFlowFromSource(j)) this.queue.push(j + n);
            }
        }
        while (this.queue.getSize() > 0) {
            int x = this.queue.pop();
            if (x >= n) { // if the dequeued vertex is in V1
                x -= n;
                boolean shouldBreak = false;
                for (int y : this.mayInverseMatch(x)) { // For each value y in mayInverseMatch(x)
                    if (this.mayGrowFlowBetween(x, y) && !this.queue.onceInQueue(y)) {
                        this.left2rightArc[y] = x;
                        if (this.mayGrowFlowToSink(y)) {
                            eopath = y;
                            shouldBreak = true;
                            break;
                        } else {
                            this.queue.push(y);
                        }
                    }
                }
                if (shouldBreak) break;
            } else {
                int y = this.match(x);
                if (!this.queue.onceInQueue(y + n)) {
                    this.right2leftArc[y] = x;
                    this.queue.push(y + n);
                }
            }
        }
        return eopath;
    }

    /**
     * augment the matching along one alternating path
     * note: throughout the following code, we assume (1 <= x <= c.nbLeftVertices), (1 <= y <= c.nbRightVertices)
     *
     * @param x a node
     */
    public void augment(int x) {
        int y = this.left2rightArc[x];
        while (!this.mayGrowFlowFromSource(y)) {
            this.putRefMatch(x, y);
            x = this.right2leftArc[y];
            assert (this.match(x) == y);
            y = this.left2rightArc[x];
            assert (y >= 0);
        }
        this.putRefMatch(x, y);
        this.increaseMatchingSize();
    }

    /**
     * keeps augmenting the flow until a maximal flow is reached
     *
     * @return false if contradiction with flow
     */
    public boolean augmentFlow() {
        int eopath = this.findAlternatingPath();
        int n1 = this.nbLeftVertices;
        if (this.matchingSize.get() < n1) {
            while (eopath >= 0) {
                this.augment(eopath);
                eopath = this.findAlternatingPath();
            }
            if (this.matchingSize.get() < n1) {
                // assert exist i, 0 <= i < n1, this.match(i) == 0
                return false;
            }
        }
        return true;
    }

    // ==============================================================
    //       FINDING STRONGLY CONNECTED COMPONENTS IN THE GRAPH
    // ==============================================================

    /**
     * initialize the graph data structure storing the SCC decomposition
     */
    public void initSCCGraph() {
        // erase the component partial order graph
        int nbc = this.getNbComponents();
        for (int i = 0; i < nbc; i++) {
            for (int j = 0; j < nbc; j++)
                if (i != j) {
                    this.componentOrder[i][j] = false;
                }
        }
        // erase the component graph
        for (int i = 0; i < this.nbVertices; i++) {
            this.component[i] = -1;
        }
        this.currentComponent = -1;
    }

    /**
     * adds a new vertex to the component graph (= a component = a set of s. connected vertices in the original graph)
     */
    public void addComponentVertex() {
        this.currentComponent++;
    }

    public int getNbComponents() {
        return currentComponent + 1;
    }

    /**
     * add an edge in the component graph between compi and compj:
     * componentOrder stores the transitive closure of that graph
     *
     * @param compi scc i
     * @param compj scc j
     */
    public void addComponentEdge(int compi, int compj) {
        if (!this.componentOrder[compi][compj]) {
            this.componentOrder[compi][compj] = true;
            for (int compj2 = 0; compj2 < compj; compj2++) {
                if (this.componentOrder[compj][compj2]) {
                    this.componentOrder[compi][compj2] = true;
                }
            }
        }
    }

    /**
     * seen[i] = false <=> color[i] = white (in book)
     * = true               % {gray, black}
     */
    public void firstPassDFS() {
        for (int i = 0; i < this.nbVertices; i++) {
            this.finishDate[i] = 0;
            this.seen[i] = false;
        }
        this.time = 0;
        for (int i = 0; i < this.nbVertices; i++) {
            this.firstDFSearch(i);
        }
    }

    /**
     * the first search explores (DFS) the reduced graph
     *
     * @param i starting node
     */
    public void firstDFSearch(int i) {
        if (!this.seen[i]) {
            this.time++;
            this.seen[i] = true;
            if (i < this.nbLeftVertices) {    // (i % c.leftVertices)
                // assert (this.match(i) != 0);
                this.firstDFSearch(this.match(i) + this.nbLeftVertices);
            } else if (i < this.source) {     // (i % c.rightVertices)
                for (int j : this.mayInverseMatch(i - this.nbLeftVertices)) { // for each j in mayInverseMatch...
                    if (this.match(j) != i - this.nbLeftVertices) {
                        this.firstDFSearch(j);
                    }
                }
                if (this.mayDiminishFlowFromSource(i - this.nbLeftVertices)) {
                    this.firstDFSearch(this.source);
                }
            } else {                          // (i = sc.source)
                for (int j = 0; j < this.nbRightVertices; j++) {
                    if (this.mayGrowFlowFromSource(j)) {
                        this.firstDFSearch(j + this.nbLeftVertices);
                    }
                }
            }
            this.time++;
            this.finishDate[i] = this.time;
        }
    }

    public void secondPassDFS() {
        this.initSCCGraph();
        while (true) {
            int maxf = 0;
            int rootOfComp = -1;
            for (int i = 0; i < this.nbVertices; i++) {
                if ((this.component[i] == -1) && (this.finishDate[i] > maxf)) {
                    maxf = this.finishDate[i];
                    rootOfComp = i;
                }
            }
            if (maxf > 0) {
                this.addComponentVertex();
                this.secondDFSearch(rootOfComp);
            } else
                return;
        }
    }

    /**
     * the second search explores (DFS) the inverse of the reduced graph
     *
     * @param i starting node
     */
    public void secondDFSearch(int i) {
        int compi = this.component[i];
        int curComp = this.currentComponent;
        if (compi == -1) {
            this.component[i] = curComp;
            this.currentNode = i;
            if (i < this.nbLeftVertices) {    // (i % c.leftVertices)
                for (int j : this.mayMatch(i)) {
                    if (this.match(i) != j) {
                        this.secondDFSearch(j + this.nbLeftVertices);
                    }
                }
            } else if (i < this.source) {     // (i % c.rightVertices)
                for (int j : this.mayInverseMatch(i - this.nbLeftVertices)) {
                    if (this.match(j) == i - this.nbLeftVertices) {
                        this.secondDFSearch(j);
                    }
                }
                if (this.mayGrowFlowFromSource(i - this.nbLeftVertices))
                    this.secondDFSearch(this.source);
            } else {                          // (i = sc.source)
                for (int j = 0; j < this.nbRightVertices; j++) {
                    if (this.mayDiminishFlowFromSource(j)) {
                        this.secondDFSearch(j + this.nbLeftVertices);
                    }
                }
            }
        } else if (compi < curComp) {
            // ajouter a la composante du sommet "pere" une arete vers la composante du sommet i
            this.addComponentEdge(curComp, compi);
        } else if (compi > curComp) {
            LOGGER.error("Unexpected strong connection component of higher index: {}, {}", new Object[]{compi, curComp});
        }
    }

    /**
     * remove arcs connecting two different strongly connected components
     * the event generated by the flow algorithm:
     * discovering that an edge is no longer valid, and posting this event
     * to the constraint strategy: since we are already achieving GAC consistency
     * in one single loop, there is no need to post a constAwake
     *
     * @param propagator propagator who remove edge in the bipartite graph
     * @throws ContradictionException fail
     */
    public void removeUselessEdges(Propagator propagator) throws ContradictionException {
        boolean updateMatching;
        do {
            updateMatching = false;
            if (this.matchingSize.get() < this.nbLeftVertices) {
                if (!this.augmentFlow()) {
                    ContradictionException.throwIt(propagator, null, "matching size < " + this.nbLeftVertices);
                }
            }
            refreshSCC();
            for (int i = 0; i < this.nbLeftVertices; i++) {
                for (int j : this.mayMatch(i)) {
                    if (this.match(i) != j) {
                        if (this.component[i] != this.component[j + this.nbLeftVertices]) {
                            updateMatching |= this.updateMatchingOnRemoval(i, j, propagator);
                        }
                    }
                }
            }
        } while (updateMatching);
    }

    public void refreshSCC() {
        this.firstPassDFS();
        this.secondPassDFS();
    }

    // ==============================================================
    //   Specific methods for bipartite matching
    // ==============================================================

    /**
     * Removing the arc i-j from the reference matching.
     *
     * @param i the left vertex
     * @param j the right vertex
     * @return false if there is nothing to delete
     */
    public boolean deleteMatch(final int i, final int j) {
        if (j == this.nodes[i].getRefMatch()) {
            this.nodes[i].setRefMatch(-1);
            this.refInverseMatch.set(j, -1);
            this.decreaseMatchingSize();
            return true;
        }
        return false;
    }

    /**
     * Adding the arc i-j in the reference matching.
     *
     * @param i the left vertex
     * @param j the right vertex
     * @return if has a side effect
     */
    public boolean setMatch(final int i, final int j) {
        boolean effect = false;
        int j0 = this.nodes[i].getRefMatch();
        int i0 = this.refInverseMatch.get(j);
        if (j0 != j) {
            // assert (i0 != i);
            if (j0 >= 0) {
                this.refInverseMatch.set(j0, -1);
                this.decreaseMatchingSize();
            }
            if (i0 >= 0) {
                this.nodes[i0].setRefMatch(-1);
                this.decreaseMatchingSize();
            }
            this.nodes[i].setRefMatch(j);
            this.refInverseMatch.set(j, i);
            this.increaseMatchingSize();
            effect = true;
        }
        return effect;
    }

    /**
     * Matching size has been decrease by 1.
     */
    public void decreaseMatchingSize() {
        this.matchingSize.set(this.matchingSize.get() - 1);
    }

    /**
     * Matching size has been increase by 1.
     */
    public void increaseMatchingSize() {
        this.matchingSize.set(this.matchingSize.get() + 1);
    }

    /**
     * Adding the arc i-j in the reference matching without any updates.
     *
     * @param i the left vertex
     * @param j the right vertex
     */
    public void putRefMatch(final int i, final int j) {
        this.nodes[i].setRefMatch(j);
        this.refInverseMatch.set(j, i);
    }

    /**
     * Checks if the flow can be decreased between source and a vertex.
     *
     * @param j the vertex
     * @return whether the flow from the source to j (a right vertex)
     *         may be decreased
     */
    public boolean mayDiminishFlowFromSource(final int j) {
        return this.refInverseMatch.get(j) != -1;
    }

    /**
     * Checks if the flow can be increased between source and a vertex.
     *
     * @param j the vertex
     * @return whether the flow from the source to j (a right vertex)
     *         may be increased
     */
    public boolean mayGrowFlowFromSource(final int j) {
        return this.refInverseMatch.get(j) == -1;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        for (Node n : nodes) {
            st.append(n.toString()).append('\n');
        }
        for (int i = 0; i < this.matchingSize.get(); i++) {
            st.append(i).append("->").append(this.nodes[i].getRefMatch() + this.minValue).append('\n');
        }
        return st.toString();
    }


    // ==============================================================
    //   QUEUE OF REACHED VERTICES WHEN FINDING AN AUGMENTING PATH
    // ==============================================================

    protected static class IntQueue implements Serializable {
        /**
         * Maximum size of the queue.
         */
        private final int maxSize;

        /**
         * Number of elements actually in the queue.
         */
        private int nbElts;

        /**
         * Last element pushed in the queue.
         */
        private int last;

        /**
         * Linked list of the values.
         */
        private final int[] contents;

        /**
         * States if the value is in the queue or not.
         */
        private final boolean[] onceInQueue;

        /**
         * Constructs a new queue with the specified maximal number of values.
         *
         * @param n Maximal size of the queue.
         */
        public IntQueue(int n) {
            maxSize = n;
            contents = new int[n];
            onceInQueue = new boolean[n];
            this.init();
        }


        /**
         * @return the size of the queue.
         */
        public int getSize() {
            return this.nbElts;
        }

        /**
         * Initializes the queue.
         */
        public void init() {
            this.nbElts = 0;
            for (int i = 0; i < this.maxSize; i++) {
                this.contents[i] = -1;
                this.onceInQueue[i] = false;
            }
        }

        /**
         * Adds a value in the queue
         *
         * @param val an integer value
         */
        public void push(int val) {
            // assert (val <= this.maxSize);
            this.onceInQueue[val] = true;
            if (this.contents[val] == -1) {
                if (this.nbElts == 0) {
                    this.contents[val] = val;
                } else {
                    this.contents[val] = this.contents[last];
                    this.contents[last] = val;
                }
                this.last = val;
                this.nbElts++;
            }
        }

        /**
         * @return the older value in the queue
         */
        public int pop() {
            int val = this.contents[this.last];
            this.nbElts--;
            this.contents[this.last] = this.contents[val];
            this.contents[val] = -1;
            return val;
        }

        /**
         * @param i integer
         * @return true if i is in the queue
         */
        public boolean onceInQueue(int i) {
            return this.onceInQueue[i];
        }
    }
}
