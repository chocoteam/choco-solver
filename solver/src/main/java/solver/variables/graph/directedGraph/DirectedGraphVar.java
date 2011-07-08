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

package solver.variables.graph.directedGraph;

import solver.ICause;
import solver.Solver;
import solver.exception.ContradictionException;
import solver.variables.EventType;
import solver.variables.graph.GraphType;
import solver.variables.graph.GraphVar;
import solver.variables.graph.IActiveNodes;
import solver.variables.graph.INeighbors;

import java.util.BitSet;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created by IntelliJ IDEA.
 * User: chameau, Jean-Guillaume Fages
 * Date: 7 févr. 2011
 */
public class DirectedGraphVar extends GraphVar<StoredDirectedGraph> {

	////////////////////////////////// GRAPH PART ///////////////////////////////////////
	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	public static long seed = 1;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	public DirectedGraphVar(Solver solver, BitSet[] data, GraphType typeEnv, GraphType typeKer) {
		super(solver);
		envelop = new StoredDirectedGraph(environment, data, typeEnv);
		kernel = new StoredDirectedGraph(environment, data.length, typeKer);
		kernel.getActiveNodes().clear();
	}
	public DirectedGraphVar(Solver solver, BitSet[] data, GraphType type) {
		this(solver,data,type,type);
	}
	public DirectedGraphVar(Solver solver, int nbNodes, GraphType typeEnv, GraphType typeKer) {
		super(solver);
		envelop = new StoredDirectedGraph(environment, nbNodes, typeEnv);
		kernel = new StoredDirectedGraph(environment, nbNodes, typeKer);
		kernel.getActiveNodes().clear();
	}
	public DirectedGraphVar(Solver solver, int nbNodes, GraphType type) {
		this(solver,nbNodes,type,type);
	}


	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	public DirectedGraphVar(Solver solver, int n, GraphType dense, String options) {
		this(solver,n,dense);
		if(options.equals("clique")){
			for (int i=0;i<n;i++){
				for(int j = 0;j<n ; j++){
					envelop.addArc(i, j);
				}
			}
		}
	}

	@Override
	public boolean instantiated() {
		for(int i=envelop.activeIdx.getFirstElement(); i>=0; i=envelop.activeIdx.getNextElement()){
			if(!kernel.activeIdx.isActive(i)){
				return false;
			}
			for(int j=envelop.getSuccessorsOf(i).getFirstElement();j>=0; j=envelop.getSuccessorsOf(i).getNextElement()){
				if(!kernel.arcExists(i, j)){
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean removeArc(int x, int y, ICause cause) throws ContradictionException {
		if(kernel.arcExists(x, y)){
			this.contradiction(cause, "remove mandatory arc");
			return false;
		}
		if (envelop.removeArc(x, y)){
			if (reactOnModification){
				delta.getArcRemovalDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
			}
			EventType e = EventType.REMOVEARC;
			notifyPropagators(e, cause);
			if(getEnvelopGraph().getPredecessorsOf(x).neighborhoodSize()==0 && getEnvelopGraph().getSuccessorsOf(x).neighborhoodSize()==0){
				removeNode(x, null);
			}
			if(getEnvelopGraph().getPredecessorsOf(y).neighborhoodSize()==0 && getEnvelopGraph().getSuccessorsOf(y).neighborhoodSize()==0){
				removeNode(y, null);
			}
			return true;
		}return false;
	}
	@Override
	public boolean enforceArc(int x, int y, ICause cause) throws ContradictionException {
		enforceNode(x, cause);
		enforceNode(y, cause);
		if(envelop.arcExists(x, y)){
			if (kernel.addArc(x, y)){
				if (reactOnModification){
					delta.getArcEnforcingDelta().add((x+1)*getEnvelopGraph().getNbNodes()+y);
				}
				EventType e = EventType.ENFORCEARC;
				notifyPropagators(e, cause);
				return true;
			}return false;
		}
		this.contradiction(cause, "enforce arc which is not in the domain");
		return false;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	@Override
	public StoredDirectedGraph getKernelGraph() {
		return kernel;
	}

	@Override
	public StoredDirectedGraph getEnvelopGraph() {
		return envelop;
	} 

	///////////////////////////////////////////////////////////////////////

	/**UGLY
	 * @return a randomly choosen arc 
	 */
	public int nextArc() {
		return NodesThenArcsRd();
	}
	private int NodesThenArcsLex() {
		INeighbors nei;
		int n = getEnvelopGraph().getNbNodes();
		IActiveNodes env = getEnvelopGraph().getActiveNodes();
		if(getEnvelopOrder()!=getKernelOrder()){
			for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
				if(!getKernelGraph().getActiveNodes().isActive(i)){
					return i;
				}
			}
		}
		for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
			if(envelop.successors[i].neighborhoodSize() != kernel.successors[i].neighborhoodSize()){
				nei = envelop.getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
					if (!kernel.arcExists(i, j)){
						return (i+1)*n+j;
					}
				}
			}
		}
		return -1;
	}
	private int NodesThenArcsRd() {
		INeighbors nei;
		int n = getEnvelopGraph().getNbNodes();
		IActiveNodes env = getEnvelopGraph().getActiveNodes();
		Random rd = new Random(seed);
		int delta = getEnvelopOrder()-getKernelOrder();
		if(delta!=0){
			delta = rd.nextInt(delta);
			for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
				if(!getKernelGraph().getActiveNodes().isActive(i)){
					if(delta == 0){
						return i;
					}else{
						delta--;
					}
				}
			}
		}
		for (int i=env.getFirstElement();i>=0;i=env.getNextElement()){
			delta = envelop.successors[i].neighborhoodSize() - kernel.successors[i].neighborhoodSize();
			if(delta != 0){
				nei = envelop.getSuccessorsOf(i);
				delta = rd.nextInt(delta);
				for (int j=nei.getFirstElement();j>=0;j=nei.getNextElement()){
					if(!getKernelGraph().arcExists(i, j)){
						if(delta == 0){
							return (i+1)*n+j;
						}else{
							delta--;
						}
					}
				}
			}
		}
		return -1;
	}
	private int nextArcRandom() {
		int n = getEnvelopGraph().getNbNodes();
		LinkedList<Integer> arcs = new LinkedList<Integer>();
		INeighbors nei;
		IActiveNodes act = getEnvelopGraph().getActiveNodes();
		for (int i=act.getFirstElement();i>=0;i=act.getNextElement()){
			if(envelop.successors[i].neighborhoodSize() != kernel.successors[i].neighborhoodSize()){
				nei = envelop.getSuccessorsOf(i);
				for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
					if (!kernel.arcExists(i, j)){
						arcs.addFirst((i+1)*n+j);
					}
				}
			}
		}
		if(arcs.size()==0)return -1;
		Random rd = new Random(seed);
		return arcs.get(rd.nextInt(arcs.size()));
	}
	private int nextArcRandom2() {
		INeighbors nei;
		int n = getEnvelopGraph().getNbNodes();
		LinkedList<Integer> arcs = new LinkedList<Integer>();
		IActiveNodes act = getEnvelopGraph().getActiveNodes();
		for (int i=act.getFirstElement();i>=0;i=act.getNextElement()){
			if(envelop.successors[i].neighborhoodSize() != kernel.successors[i].neighborhoodSize()){
				if(kernel.successors[i].neighborhoodSize()>0){
					throw new UnsupportedOperationException("error in 1-succ filtering");
				}
				arcs.add(i);
			}
		}
		if(arcs.size()==0)return -1;
		Random rd = new Random(seed);
		int node = arcs.get(rd.nextInt(arcs.size()));
		arcs.clear();
		nei = envelop.getSuccessorsOf(node);
		for(int j=nei.getFirstElement();j>=0; j=nei.getNextElement()){
			if (!kernel.arcExists(node, j)){
				arcs.addFirst((node+1)*n+j);
			}
		}
		return arcs.get(rd.nextInt(arcs.size()));
	}
}
