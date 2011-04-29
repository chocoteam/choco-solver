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

package solver.variables.graph.graphOperations.connectivity;

import java.util.BitSet;
import java.util.LinkedList;

import solver.variables.graph.INeighbors;
import solver.variables.graph.graphStructure.adjacencyList.IntLinkedList;

/**Class which encapsulates Articulation Points (AP) and Connected Components (CC)
 * @author Jean-Guillaume Fages
 */
public class ConnectivityObject {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private LinkedList<INeighbors> connectedComponents;
	private BitSet articulationPoints;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** Create a connectivity object that will only store CC */
	public ConnectivityObject() {
		connectedComponents = new LinkedList<INeighbors>();
		//TODO maybe a BitSet representation for dense graph
	}
	/** Create a connectivity object that will store CC and AP
	 * @param nb order of the considered graph
	 */
	public ConnectivityObject(int nb) {
		this();
		articulationPoints = new BitSet(nb);
	}
	
	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * create a new empty connected component
	 */
	public void newCC() {
		connectedComponents.addLast(new IntLinkedList());
	}

	/**Add a node to a connected component
	 * More precisely to the last created CC
	 * @param node node to add to the connected component
	 */
	public void addCCNode(int node) {
		connectedComponents.getLast().add(node);
	}

	/**Add an articulation point
	 * @param point node which is an articulation point
	 */
	public void addArticulationPoint(int point) {
		articulationPoints.set(point);
	}
	
	public String toString(){
		String s = "Connectivity object :";
		if (connectedComponents!=null){
			s += "\nCC : ";
			for (INeighbors cc:connectedComponents){
				s+="\n"+cc;
			}
		}else{
			s+= "\nno CC computed";
		}
		if (articulationPoints!=null){
			s += "\nAP : "+articulationPoints.toString();
		}else{
			s+= "\nno SAP computed";
		}
		return s;
	}
	
	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************
	
	/**Get all connected components.
	 * @return connectedComponents a list of CC
	 */
	public LinkedList<INeighbors> getConnectedComponents() {
		return connectedComponents;
	}
	/**Get a bitset representing articulation points : the bit i is true means that the node of index i is an articulation point
	 * WARNING : if the CConly algorithm has been performed then this method returns null
	 * @return articulationPoints 
	 */
	public BitSet getArticulationPoints() {
		if (articulationPoints==null){
			Exception e = new Exception("warning : try to get AP but the CConly algorithm has been performed so there is no information about AP");
			e.printStackTrace();
		}
		return articulationPoints;
	}
}
