/**
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

/**
 * Created by IntelliJ IDEA.
 * User: Jean-Guillaume Fages
 * Date: 16/11/11
 * Time: 10:42
 */

package solver.constraints.propagators.nary.circuit;

import solver.constraints.propagators.Propagator;
import solver.constraints.propagators.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.IntVar;
import util.ESat;
import util.graphOperations.connectivity.StrongConnectivityFinder;
import util.objects.graphs.DirectedGraph;
import util.objects.setDataStructures.ISet;
import util.objects.setDataStructures.SetFactory;
import util.objects.setDataStructures.SetType;

import java.util.Random;

/**
 * Filters circuit based on strongly connected components
 * (see )
 * @author Jean-Guillaume Fages
 * @since 2013
 */
public class PropCircuitSCC extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n;
	private DirectedGraph support;
	private StrongConnectivityFinder SCCfinder;
	private DirectedGraph G_R;
	private int[] sccOf;
	private ISet[] mates;
	// proba
	private Random rd;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/**
	 * Maintain incrementally the reduced graph and strongly connected components of a directed graph variable
	 * Ensures that the reduced graph is a Hamiltonian path
	 * BEWARE REQUIRES A UNIQUE SOURCE AND A UNIQUE SINK
	 *
	 * @param succs
	 */
	public PropCircuitSCC(IntVar[] succs) {
		super(succs, PropagatorPriority.LINEAR);
		n = vars.length;
		support = new DirectedGraph(n+1,SetType.LINKED_LIST,true);
		G_R = new DirectedGraph(n+1,SetType.LINKED_LIST,true);
		SCCfinder = new StrongConnectivityFinder(support);
		sccOf = new int[n+1];
		mates = new ISet[n+1];
		for(int i=0;i<n;i++){
			mates[i] = SetFactory.makeLinkedList(false);
		}
		rd = new Random(0);
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	public void propagate(int vIdx, int mask) throws ContradictionException {
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if(rd.nextBoolean()){
			filterFromSource(rd.nextInt(n));
		}else{
			if(n<30){
				for(int i=0;i<n;i++){
					filterFromSource(i);
				}
			}else{
				for(int i=0;i<30;i++){
					filterFromSource(rd.nextInt(n));
				}
			}
		}
	}

	private int visit(int node, int last, int source) throws ContradictionException {
		if (node == -1) {
			contradiction(vars[0], "G_R disconnected");
		}
		if (node == last) {
			return 1;
		}
		int next = -1;
		ISet succs = G_R.getSuccessorsOf(node);
		for (int x = succs.getFirstElement(); x >= 0; x = succs.getNextElement()) {
			if (G_R.getPredecessorsOf(x).getSize() == 1) {
				if (next != -1) {
					return 0;
				}
				next = x;
			} else {
				G_R.removeArc(node, x);
			}
		}
		succs = mates[node];
		int from, to;
		for (int e = succs.getFirstElement(); e >= 0; e = succs.getNextElement()) {
			to = e % n;
			if (sccOf[to] != next) {
				from = e / n - 1;
				if(to==n){
					to=source;
				}
				vars[from].removeValue(to,aCause);
				mates[node].remove(e);
			}
		}
		return visit(next, last,source) + 1;
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;// redundant propagator
	}

	public void rebuild(int source) {
		for(int i=0;i<=n;i++){
			mates[i].clear();
			support.getSuccessorsOf(i).clear();
			support.getPredecessorsOf(i).clear();
			G_R.getPredecessorsOf(i).clear();
			G_R.getSuccessorsOf(i).clear();
		}
		G_R.getActiveNodes().clear();
		for(int i=0;i<n;i++){
			IntVar v = vars[i];
			int lb = v.getLB();
			int ub = v.getUB();
			for(int j=lb;j<=ub;j=v.nextValue(j)){
				if(j==source){
					support.addArc(i,n);
				}else{
					support.addArc(i,j);
				}
			}
		}
	}

	public void filterFromSource(int source) throws ContradictionException {
		rebuild(source);
		SCCfinder.findAllSCC();
		// filter
		int n_R = SCCfinder.getNbSCC();
		int j;
		for (int i = 0; i < n_R; i++) {
			G_R.getActiveNodes().add(i);
			j = SCCfinder.getSCCFirstNode(i);
			while (j != -1) {
				sccOf[j]=i;
				j = SCCfinder.getNextNode(j);
			}
		}
		ISet succs;
		int x;
		for (int i = 0; i < n; i++) {
			x = sccOf[i];
			succs = support.getSuccessorsOf(i);
			for (j = succs.getFirstElement(); j >= 0; j = succs.getNextElement()) {
				if (x != sccOf[j]) {
					G_R.addArc(x, sccOf[j]);
					mates[x].add((i + 1) * n + j);
				}
			}
		}
		int first = -1;
		int last = -1;
		for (int i = 0; i < n_R; i++) {
			if (G_R.getPredecessorsOf(i).isEmpty()) {
				first = i;
			}
			if (G_R.getSuccessorsOf(i).isEmpty()) {
				last = i;
			}
		}
		if (first == -1 || last == -1 || first == last) {
			contradiction(vars[0], "");
		}
		if (visit(first, last, source) != n_R) {
			contradiction(vars[0], "");
		}
		int to, arc;
		for (int i = 0; i < n; i++) {
			if(vars[i].instantiated()){
				to = vars[i].getValue();
				x = sccOf[i];
				if(to==source){
					to = n;
				}
				if (to != -1 && sccOf[to] != x && mates[x].getSize() > 1) {
					arc = (i + 1) * n + to;
					for (int a = mates[x].getFirstElement(); a >= 0; a = mates[x].getNextElement()) {
						if (a != arc) {
							int val = a%n;
							if(val==n){
								val = source;
							}
							vars[a/n-1].removeValue(val,aCause);
						}
					}
					mates[x].clear();
					mates[x].add(arc);
				}
			}
		}
	}
}