/*
 * Copyright (c) 1999-2012, Ecole des Mines de Nantes
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
package solver.constraints.nary.alldifferent;

import gnu.trove.map.hash.TIntIntHashMap;
import memory.IStateInt;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.graphOperations.connectivity.StrongConnectivityFinder;
import util.objects.graphs.DirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetType;

import java.util.BitSet;
import java.util.Random;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 * <p/>
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation
 * but has a good average behavior in practice
 *
 * SHOULD NOT BE USED ALONE (use BC in addition) because it is not always apply
 * <p/>
 * Runs incrementally for maintaining a matching
 * <p/>
 * Extra features:
 * - Probabilistic call
 * - Adaptive probability distribution
 *
 * ! redundant propagator!
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDiffAC_adaptive extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected int n, n2;
    protected DirectedGraph digraph;
    private IStateInt[] matching;
    private int[] nodeSCC;
    protected BitSet free;
    private StrongConnectivityFinder SCCfinder;
    // for augmenting matching (BFS)
    private int[] father;
    private BitSet in;
    private TIntIntHashMap map;
    int[] fifo;
	private Random rd;
	private int period;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * AllDifferent constraint for integer variables
     * enables to control the cardinality of the matching
     *
     * @param variables
     */
    public PropAllDiffAC_adaptive(IntVar[] variables, int seed) {
        super(variables, PropagatorPriority.QUADRATIC, true);
		rd = new Random(seed);
		period = 16;
        n = vars.length;
        matching = new IStateInt[n];
        for (int i = 0; i < n; i++) {
            matching[i] = environment.makeInt(-1);
        }
        map = new TIntIntHashMap();
        IntVar v;
        int ub;
        int idx = n;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int j = v.getLB(); j <= ub; j = v.nextValue(j)) {
                if (!map.containsKey(j)) {
                    map.put(j, idx);
                    idx++;
                }
            }
        }
        n2 = idx;
        fifo = new int[n2];
        digraph = new DirectedGraph(n2 + 1, SetType.BITSET, false);
        free = new BitSet(n2);
        father = new int[n2];
        in = new BitSet(n2);
        SCCfinder = new StrongConnectivityFinder(digraph);
    }

    //***********************************************************************************
    // PROPAGATION
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (n2 < n * 2) {
            contradiction(null, "not enough values");
        }
		period = Math.max(period,1);
		if((evtmask&EventType.FULL_PROPAGATION.mask)!=0 || rd.nextInt(period)==0){
			if(findMaximumMatching()){
				if(filter()){
					period --;
				}else{
					period ++;
				}
			}else{
				period = (period+1)/2;
				contradiction(vars[0],"no matching");
			}
		}
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        forcePropagate(EventType.CUSTOM_PROPAGATION);
    }

    //***********************************************************************************
    // INFO
    //***********************************************************************************

	@Override
    public ESat isEntailed() {
        int nbInst = 0;
        for (int i = 0; i < n; i++) {
            if (vars[i].isInstantiated()) {
                nbInst++;
                for (int j = i + 1; j < n; j++) {
                    if (vars[j].isInstantiated() && vars[i].getValue() == vars[j].getValue()) {
                        return ESat.FALSE;
                    }
                }
            }
        }
        if (nbInst == vars.length) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append("PropAllDiffAC_Random(");
        int i = 0;
        for (; i < Math.min(4, vars.length); i++) {
            st.append(vars[i].getName()).append(", ");
        }
        if (i < vars.length - 2) {
            st.append("...,");
        }
        st.append(vars[vars.length - 1].getName()).append(")");
        return st.toString();
    }

    //***********************************************************************************
    // Initialization
    //***********************************************************************************

    protected boolean findMaximumMatching() {
        for (int i = 0; i < n2; i++) {
            digraph.getSuccessorsOf(i).clear();
            digraph.getPredecessorsOf(i).clear();
        }
        free.set(0, n2);
        int k, ub;
        IntVar v;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            int mate = matching[i].get();
            for (k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                int j = map.get(k);
                if (mate == j) {
                    assert free.get(i) && free.get(j);
                    digraph.addArc(j, i);
                    free.clear(i);
                    free.clear(j);
                } else {
                    digraph.addArc(i, j);
                }
            }
        }
        for (int i = free.nextSetBit(0); i >= 0 && i < n; i = free.nextSetBit(i + 1)) {
            if(!tryToMatch(i))return false;
        }
        int p;
        for (int i = 0; i < n; i++) {
            p = digraph.getPredecessorsOf(i).getFirstElement();
            matching[i].set(p);
        }
		return true;
    }

    private boolean tryToMatch(int i) {
        int mate = augmentPath_BFS(i);
        if (mate != -1) {
            free.clear(mate);
            free.clear(i);
            int tmp = mate;
            while (tmp != i) {
                digraph.removeArc(father[tmp], tmp);
                digraph.addArc(tmp, father[tmp]);
                tmp = father[tmp];
            }
			return true;
        } else {
            return false;
        }
    }

    private int augmentPath_BFS(int root) {
        in.clear();
        int indexFirst = 0, indexLast = 0;
        fifo[indexLast++] = root;
        int x, y;
        ISet succs;
        while (indexFirst != indexLast) {
            x = fifo[indexFirst++];
            succs = digraph.getSuccessorsOf(x);
            for (y = succs.getFirstElement(); y >= 0; y = succs.getNextElement()) {
                if (!in.get(y)) {
                    father[y] = x;
                    fifo[indexLast++] = y;
                    in.set(y);
                    if (free.get(y)) {
                        return y;
                    }
                }
            }
        }
        return -1;
    }

    //***********************************************************************************
    // PRUNING
    //***********************************************************************************

    private void buildSCC() {
        if (n2 > n * 2) {
            digraph.desactivateNode(n2);
            digraph.activateNode(n2);
            for (int i = n; i < n2; i++) {
                if (free.get(i)) {
                    digraph.addArc(i, n2);
                } else {
                    digraph.addArc(n2, i);
                }
            }
        }
        SCCfinder.findAllSCC();
        nodeSCC = SCCfinder.getNodesSCC();
        digraph.desactivateNode(n2);
    }

    protected boolean filter() throws ContradictionException {
        buildSCC();
        int j, ub;
        IntVar v;
		boolean useful = false;
        for (int i = 0; i < n; i++) {
            v = vars[i];
            ub = v.getUB();
            for (int k = v.getLB(); k <= ub; k = v.nextValue(k)) {
                j = map.get(k);
                if (nodeSCC[i] != nodeSCC[j]) {
                    if (matching[i].get() == j) {
						useful = !v.isInstantiated();
                        v.instantiateTo(k, aCause);
                    } else {
                        v.removeValue(k, aCause);
                        digraph.removeArc(i, j);
						useful = true;
                    }
                }
            }
        }
		return useful;
    }
}
