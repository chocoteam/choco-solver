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

import java.util.BitSet;
import java.util.Random;

/**
 * Filters subcircuit based on strongly connected components
 * @author Jean-Guillaume Fages
 */
public class PropSubCircuitSCC extends Propagator<IntVar> {

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
	private BitSet mandSCC;
	private int[] possibleSources;
	private final int NB_MAX_ITER = 15;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public PropSubCircuitSCC(IntVar[] succs, int offSet) {
		super(succs, PropagatorPriority.LINEAR);
		this.offSet = offSet;
		n = vars.length;
		n2 = n+1;
		support = new DirectedGraph(n2,SetType.LINKED_LIST,true);
		G_R = new DirectedGraph(n2,SetType.LINKED_LIST,false);
		SCCfinder = new StrongConnectivityFinder(support);
		mates = new ISet[n2];
		for(int i=0;i<n2;i++){
			mates[i] = SetFactory.makeLinkedList(false);
		}
		rd = new Random(0);
		mandSCC = new BitSet(n2);
		possibleSources = new int[n];
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	@Override
	public int getPropagationConditions(int vIdx) {
		return EventType.INT_ALL_MASK();
	}

	@Override
	public ESat isEntailed() {
		return ESat.TRUE;// redundant propagator
	}

	public void propagate(int vIdx, int mask) throws ContradictionException {
		forcePropagate(EventType.CUSTOM_PROPAGATION);
	}

	@Override
	public void propagate(int evtmask) throws ContradictionException {
		int size = 0;
		for(int i=0;i<n;i++){
			if(!vars[i].contains(i+offSet)){
				possibleSources[size++] = i;
			}
		}
		if(size>0){
			if(rd.nextBoolean()){
				filterFromSource(possibleSources[rd.nextInt(size)]);
			}else{
				if(size<NB_MAX_ITER){
					for(int i=0;i<size;i++){
						filterFromSource(possibleSources[i]);
					}
				}else{
					for(int i=0;i<NB_MAX_ITER;i++){
						filterFromSource(possibleSources[rd.nextInt(size)]);
					}
				}
			}
		}
	}

	public void filterFromSource(int source) throws ContradictionException {
		assert (!vars[source].contains(source+offSet));
		// reset data structures
		rebuild(source);
		int first = sccOf[source];
		int last = sccOf[n];
		int n_R = SCCfinder.getNbSCC();
		// forces variables that cannot connect source to n, to be loops
		for(int i=0;i<n_R;i++){
			if(i!=first && G_R.getPredecessorsOf(i).isEmpty()){
				makeLoops(source,i,false);
			}
			else if(i!=last && G_R.getSuccessorsOf(i).isEmpty()){
				makeLoops(source,i,true);
			}
		}
		// additional filter (based on instantiated arcs)
		filterFromInst(source);
		// ad hoc filtering rules
		checkSCCLink();
	}

	public void rebuild(int source) {
		mandSCC.clear();
		for(int i=0;i<n2;i++){
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
			G_R.getActiveNodes().add(i);
		}
		sccOf = SCCfinder.getNodesSCC();
		ISet succs;
		int x;
		for (int i = 0; i < n; i++) {
			x = sccOf[i];
			if(!vars[i].contains(i+offSet)){
				mandSCC.set(x);
			}
			succs = support.getSuccessorsOf(i);
			for (int j = succs.getFirstElement(); j >= 0; j = succs.getNextElement()) {
				if (x != sccOf[j]) {
					G_R.addArc(x, sccOf[j]);
					mates[x].add((i + 1) * n2 + j);
				}
			}
		}
	}

	private void makeLoops(int source, int cc, boolean sink) throws ContradictionException {
		if(cc==sccOf[source]){
			if(sink){
				contradiction(vars[0],"");
			}else{
				return;
			}
		}
		if(cc==sccOf[n]){
			if(sink){
				return;
			}else{
				contradiction(vars[0],"");
			}
		}
//		if((sink && cc==sccOf[source]) || (cc==sccOf[n] && !sink)){
//			contradiction(vars[0],"");
//		}
		for(int i=SCCfinder.getSCCFirstNode(cc); i>=0; i=SCCfinder.getNextNode(i)){
			vars[i].instantiateTo(i+offSet,aCause);
		}
		mates[cc].clear();
		if(sink){
			ISet ps = G_R.getPredecessorsOf(cc);
			for(int p=ps.getFirstElement();p>=0;p=ps.getNextElement()){
				G_R.removeArc(p,cc);
				if(G_R.getSuccessorsOf(p).isEmpty()){
					makeLoops(source,p,sink);
				}
			}
		}else{
			ISet ss = G_R.getSuccessorsOf(cc);
			for(int s=ss.getFirstElement();s>=0;s=ss.getNextElement()){
				G_R.removeArc(cc,s);
				if(G_R.getPredecessorsOf(s).isEmpty()){
					makeLoops(source,s,sink);
				}
			}
		}
	}

	private void filterFromInst(int source) throws ContradictionException {
		int to, arc, x;
		for (int i = 0; i < n; i++) {
			if(vars[i].instantiated()){
				to = vars[i].getValue()-offSet;
				x = sccOf[i];
				if(to==source){
					to = n;
				}
				if (to != -1 && sccOf[to] != x && mates[x].getSize() > 1) {
					arc = (i + 1) * n2 + to;
					for (int a = mates[x].getFirstElement(); a >= 0; a = mates[x].getNextElement()) {
						if (a != arc) {
							int val = a%n2;
							if(val==n){
								val = source;
							}
							vars[a/n2-1].removeValue(val+offSet,aCause);
						}
					}
					mates[x].clear();
					mates[x].add(arc);
				}
			}
		}
	}

	private void checkSCCLink() throws ContradictionException {
		for (int sccFrom=G_R.getActiveNodes().getFirstElement(); sccFrom>=0; sccFrom=G_R.getActiveNodes().getNextElement()) {
			int door = -1;
			for (int i = mates[sccFrom].getFirstElement(); i >= 0; i = mates[sccFrom].getNextElement()) {
				if(door == -1){
					door = i/n2-1;
				}else if(door!=i/n2-1){
					return;
				}
			}
			if(door>=0){
				int lb = vars[door].getLB();
				int ub = vars[door].getUB();
				for(int v=lb;v<=ub;v=vars[door].nextValue(v)){
					if(sccOf[v-offSet]==sccFrom){
						if(v-offSet!=door || mandSCC.get(sccFrom)){
							vars[door].removeValue(v,aCause);
						}
					}
				}
			}
		}
	}
}