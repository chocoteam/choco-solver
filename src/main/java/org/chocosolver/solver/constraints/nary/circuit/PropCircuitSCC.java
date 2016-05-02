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
package org.chocosolver.solver.constraints.nary.circuit;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.graphOperations.connectivity.StrongConnectivityFinder;
import org.chocosolver.util.objects.graphs.DirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.Random;

/**
 * Filters circuit based on strongly connected components
 * (see the TechReport "Improving the Asymmetric TSP by considering graph structure", Fages & Lorca, 2012)
 * @author Jean-Guillaume Fages
 */
public class PropCircuitSCC extends Propagator<IntVar> {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private int n,n2;
	private DirectedGraph support;
	private StrongConnectivityFinder SCCfinder;
	private DirectedGraph G_R;
	private int[] sccOf;
	private ISet[] mates;
	// proba
	private Random rd;
	private int offSet;
	private CircuitConf conf;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropCircuitSCC(IntVar[] succs, int offSet, CircuitConf conf) {
		super(succs, PropagatorPriority.LINEAR, false);
		this.offSet = offSet;
		n = vars.length;
		n2 = n+1;
		support = new DirectedGraph(n2,SetType.BITSET,true);
		G_R = new DirectedGraph(n2,SetType.LINKED_LIST,false);
		SCCfinder = new StrongConnectivityFinder(support);
		mates = new ISet[n2];
		for(int i=0;i<n2;i++){
			mates[i] = SetFactory.makeLinkedList();
		}
		this.conf = conf;
		if(conf==CircuitConf.RD){ 
			rd = new Random(0);
		}
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;// redundant propagator
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		if (PropagatorEventType.isFullPropagation(evtmask)) {
			for (int i = 0; i < n; i++) {
				vars[i].updateBounds(offSet, n - 1 + offSet, this);
			}
		}
		switch (conf){
			case FIRST:
				filterFromSource(0);break;
			default:
			case RD:
				filterFromSource(rd.nextInt(n));break;
			case ALL:
				for (int i = 0; i < n; i++) {
					filterFromSource(i);
				}break;
		}
	}

	public void filterFromSource(int source) throws ContradictionException {
		// reset data structures
		rebuild(source);
		// find path endpoints
		int first = -1;
		int last = -1;
		int n_R = SCCfinder.getNbSCC();
		for (int i = 0; i < n_R; i++) {
			if (G_R.getPredOf(i).isEmpty()) {
				if(first!=-1){
					fails();
				}
				first = i;
			}
			if (G_R.getSuccOf(i).isEmpty()) {
				if(last!=-1){
					fails();
				}
				last = i;
			}
		}
		if (first == -1 || last == -1 || first == last) {
			fails();
		}
		// compute hamiltonian path and filter skipping arcs
		if (visit(first, last, source) != n_R) {
			fails();
		}
		// additional filter (based on instantiated arcs)
		filterFromInst(source);
		// ad hoc filtering rules
		for (int i=0; i<n_R; i++) {
			checkSCCLink(i);
		}
	}

	public void rebuild(int source) {
		for(int i=0;i<n2;i++){
			mates[i].clear();
			support.getSuccOf(i).clear();
			support.getPredOf(i).clear();
			G_R.getPredOf(i).clear();
			G_R.getSuccOf(i).clear();
		}
		G_R.getNodes().clear();
		for(int i=0;i<n;i++){
			IntVar v = vars[i];
			int lb = v.getLB();
			int ub = v.getUB();
			for(int j=lb;j<=ub;j=v.nextValue(j)){
				if(j-offSet==source){
					support.addArc(i,n);
				}else{
					support.addArc(i,j-offSet);
				}
			}
		}
		SCCfinder.findAllSCC();
		int n_R = SCCfinder.getNbSCC();
		for (int i = 0; i < n_R; i++) {
			G_R.getNodes().add(i);
		}
		sccOf = SCCfinder.getNodesSCC();
		ISet succs;
		int x;
		for (int i = 0; i < n; i++) {
			x = sccOf[i];
			succs = support.getSuccOf(i);
			for (int j :succs) {
				if (x != sccOf[j]) {
					G_R.addArc(x, sccOf[j]);
					mates[x].add((i + 1) * n2 + j);
				}
			}
		}
	}

	private int visit(int node, int last, int source) throws ContradictionException {
		if (node == -1) {
			fails();
		}
		if (node == last) {
			return 1;
		}
		int next = -1;
		ISet succs = G_R.getSuccOf(node);
		for(int x:succs){
			if (G_R.getPredOf(x).getSize() == 1) {
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
		for (int e:succs) {
			to = e % n2;
			if (sccOf[to] != next) {
				from = e / n2 - 1;
				if(to==n){
					to=source;
				}
				vars[from].removeValue(to+offSet, this);
				mates[node].remove(e);
			}
		}
		return visit(next, last,source) + 1;
	}

	private void filterFromInst(int source) throws ContradictionException {
		int to, arc, x;
		for (int i = 0; i < n; i++) {
			if(vars[i].isInstantiated()){
				to = vars[i].getValue()-offSet;
				x = sccOf[i];
				if(to==source){
					to = n;
				}
				if (to != -1 && sccOf[to] != x && mates[x].getSize() > 1) {
					arc = (i + 1) * n2 + to;
					for (int a:mates[x]) {
						if (a != arc) {
							int val = a%n2;
							if(val==n){
								val = source;
							}
							vars[a/n2-1].removeValue(val+offSet, this);
						}
					}
					mates[x].clear();
					mates[x].add(arc);
				}
			}
		}
	}

	private void checkSCCLink(int sccFrom) throws ContradictionException {
		int inDoor = -1;
		int outDoor = -1;
		for (int i : mates[sccFrom]) {
			if(inDoor==-1){
				inDoor = i%n2;
			}else if (inDoor!=i%n2){
				inDoor = -2;
			}
			if(outDoor==-1){
				outDoor = i/n2-1;
			}else if (outDoor!=i/n2-1){
				outDoor = -2;
			}
		}
		if (inDoor>=0) {
			forceInDoor(inDoor);
		}
		if (outDoor>=0) {
			forceOutDoor(outDoor);
			// If 1 in and 1 out and |scc| > 2 then forbid in->out
			// Is only 1 in ?
			if (G_R.getPredOf(sccFrom).iterator().hasNext()) {
				int in = -1;
				int p = G_R.getPredOf(sccFrom).iterator().next();
				for (int i : mates[p]) {
					if (in == -1) {
						in = i % n2;
					} else if (in != i % n2) {
						return;
					}
				}
				assert (in!=-1);
				assert (sccOf[in] == sccFrom);
				// Is in->out possible?
				if(vars[in].contains(outDoor+offSet)){
					// Is |scc| > 2 ?
					int size = 0;
					for(int i=SCCfinder.getSCCFirstNode(sccFrom); i>=0 && size<3;i=SCCfinder.getNextNode(i)){
						size++;
					}
					if(size>2){
						vars[in].removeValue(outDoor+offSet, this);
					}
				}
			}
		}
	}

	private void forceInDoor(int x) throws ContradictionException {
		int sx = sccOf[x];
		for(int i=0; i<n; i++){
			if(sccOf[i]==sx){
				vars[i].removeValue(x+offSet, this);
			}
		}
	}

	private void forceOutDoor(int x) throws ContradictionException {
		int sx = sccOf[x];
		int lb = vars[x].getLB();
		int ub = vars[x].getUB();
		for(int v=lb;v<=ub;v=vars[x].nextValue(v)){
			if(sccOf[v-offSet]==sx){
				vars[x].removeValue(v, this);
			}
		}
	}

}
