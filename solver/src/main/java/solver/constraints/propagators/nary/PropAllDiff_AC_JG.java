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
package solver.constraints.propagators.nary;

import choco.kernel.ESat;
import choco.kernel.common.util.procedure.IntProcedure;
import gnu.trove.list.array.TIntArrayList;
import solver.Solver;
import solver.constraints.Constraint;
import solver.constraints.propagators.GraphPropagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.recorders.fine.AbstractFineEventRecorder;
import solver.search.loop.AbstractSearchLoop;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.graph.GraphType;
import solver.variables.graph.INeighbors;
import solver.variables.graph.directedGraph.DirectedGraph;
import solver.variables.graph.directedGraph.StoredDirectedGraph;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedList;

/**
 * Propagator for AllDifferent AC constraint for integer variables
 *
 * Uses Regin algorithm
 * Runs in O(m.n) worst case time for the initial propagation and then in O(n+m) time
 * per arc removed from the support
 * HAS A LAZY FILTERING : the filtering is applied only once per search node in order to speed up the search
 * <p/>
 * Runs incrementally for maintaining a matching
 * <p/>
 *
 * @author Jean-Guillaume Fages
 */
public class PropAllDiff_AC_JG extends GraphPropagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n,n2;
	private IntVar[] vars;
	private DirectedGraph digraph;
	private int[] matching;
	private int[] nodeSCC;
	private BitSet free;
	private IntProcedure remProc;
	int matchingCardinality;
	long timestamp;
	// for augmenting matching
	int[] father;
	BitSet in;
	LinkedList<Integer> list;
	public final static boolean LAZY = true;
	private int idxVarInProp;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * AllDifferent constraint for integer variables
	 * enables to control the cardinality of the matching
	 * @param vars
	 * @param matchingCardinality
	 * @param sol
	 * @param constraint
	 */
	public PropAllDiff_AC_JG(IntVar[] vars, int matchingCardinality, Solver sol, Constraint constraint) {
		super(vars, sol, constraint, PropagatorPriority.QUADRATIC);
		n = vars.length;
		this.vars = vars;
		n2=2*n;
		this.matchingCardinality = matchingCardinality;
		matching = new int[n2];
		nodeSCC = new int[n2];
		digraph = new StoredDirectedGraph(solver.getEnvironment(),n2, GraphType.LINKED_LIST);
		free = new BitSet(n);
		matchingCardinality = n;
		remProc = new DirectedRemProc();
		father = new int[n2];
		in = new BitSet(n2);
		list = new LinkedList<Integer>();
		buildDigraph();
	}

	/**
	 * AllDifferent constraint for integer variables
	 * suppose that a perfect matching is exepcted
	 * @param vars
	 * @param constraint
	 * @param sol
	 */
	public PropAllDiff_AC_JG(IntVar[] vars, Constraint constraint, Solver sol) {
		this(vars,vars.length,sol,constraint);
	}

	//***********************************************************************************
	// Initialization
	//***********************************************************************************

	private void buildDigraph() {
		free.flip(0,n2);
		int j,ub;
		for(int i=0;i<n;i++){
			ub = vars[i].getUB();
			for(j=vars[i].getLB();j<=ub;j=vars[i].nextValue(j)){
				j+=n;
				if(free.get(i) && free.get(j)){
					digraph.addArc(j, i);
					free.clear(i);
					free.clear(j);
				}else{
					digraph.addArc(i,j);
				}
			}
		}
	}

	//***********************************************************************************
	// MATCHING
	//***********************************************************************************

	private void repairMatching() throws ContradictionException {
		for(int i=free.nextSetBit(0);i>=0 && i<n; i=free.nextSetBit(i+1)){
			tryToMatch(i);
		}
		int p;
		int cardinality = 0;
		for (int i=0;i<n;i++) {
			p = digraph.getPredecessorsOf(i).getFirstElement();
			if(p!=-1){
				cardinality++;
				matching[p]=i;
			}
			matching[i]=p;
		}
		if(cardinality<matchingCardinality){
			contradiction(vars[0],"");//TODO mettre autre chose que vars[0] !
		}
	}

	private void tryToMatch(int i) throws ContradictionException {
		int mate = augmentPath_BFS(i);
		if(mate!=-1){
			free.clear(mate);
			free.clear(i);
			int tmp = mate;
			while(tmp!=i){
				digraph.removeArc(father[tmp],tmp);
				digraph.addArc(tmp,father[tmp]);
				tmp = father[tmp];
			}
		}
	}

	private int augmentPath_BFS(int root){
		in.clear();
		list.clear();
		list.add(root);
		int x,y;
		INeighbors succs;
		while(!list.isEmpty()){
			x = list.removeFirst();
			succs = digraph.getSuccessorsOf(x);
			for(y=succs.getFirstElement();y>=0;y=succs.getNextElement()){
				if(!in.get(y)){
					father[y] = x;
					list.addLast(y);
					in.set(y);
					if(free.get(y)){
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
		ArrayList<TIntArrayList> allSCC = StrongConnectivityFinder.findAllSCCOf(digraph);
		int scc = 0;
		for (TIntArrayList in : allSCC) {
			for (int i = 0; i < in.size(); i++) {
				nodeSCC[in.get(i)] = scc;
			}
			scc++;
		}
	}

	private void filter() throws ContradictionException {
		buildSCC();
		int j,ub;
		for (int node = 0;node<n;node++) {
			ub = vars[node].getUB();
			for(j = vars[node].getLB();j<=ub;j=vars[node].nextValue(j)){
				if (nodeSCC[node] != nodeSCC[j+n]) {
					if (matching[node] == j+n && matching[j+n] == node) {
						vars[node].instantiateTo(j,this);
					} else {
						vars[node].removeValue(j, this);
					}
				}
			}
		}
	}

	//***********************************************************************************
	// PROPAGATION
	//***********************************************************************************

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		repairMatching();
		if(!LAZY){
			timestamp = AbstractSearchLoop.timeStamp-1;
		}
		if(timestamp!= AbstractSearchLoop.timeStamp){
			timestamp = AbstractSearchLoop.timeStamp;
			filter();
		}
	}

	@Override
	public void propagate(AbstractFineEventRecorder eventRecorder, int idxVarInProp, int mask) throws ContradictionException {
		free.clear();
		this.idxVarInProp = idxVarInProp;
		eventRecorder.getDeltaMonitor(vars[idxVarInProp]).forEach(remProc, EventType.REMOVEARC);
		forcePropagate(EventType.FULL_PROPAGATION);
	}

	//***********************************************************************************
	// INFO
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.REMOVE.mask+EventType.FULL_PROPAGATION.mask;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE; // le isSatisfied existe deja...
	}

	private class DirectedRemProc implements IntProcedure{
		public void execute(int i) throws ContradictionException {
			int from = idxVarInProp;
			int to   = i%n+n;
			if(digraph.arcExists(to,from)){
				free.set(to);
				free.set(from);
				digraph.removeArc(to, from);
			}
			if(digraph.arcExists(from,to)){
				digraph.removeArc(from,to);
			}
		}
	}
}
