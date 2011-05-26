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

import gnu.trove.TIntArrayList;

import java.util.BitSet;
import java.util.LinkedList;

/**Class which encapsulates Isthmus, Articulation Points (AP) and Connected Components (CC)
 * @author Jean-Guillaume Fages
 */
public class ConnectivityObject {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private LinkedList<TIntArrayList> connectedComponents;
	private BitSet articulationPoints;
	private TIntArrayList isthmus;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** Create a connectivity object that will only store CC */
	public ConnectivityObject() {
		connectedComponents = new LinkedList<TIntArrayList>();
		isthmus = new TIntArrayList();
		articulationPoints = new BitSet();
	}

	//***********************************************************************************
	// METHODS
	//***********************************************************************************

	/**
	 * create a new empty connected component
	 */
	public void newCC() {
		connectedComponents.addLast(new TIntArrayList());
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

	/**Add an isthmus
	 * @param arc (x,y) int encoding :  (x+1)*n+y with n the number of nodes of the graph
	 */
	public void addIsthmus(int arc) {
		isthmus.add(arc);
	}

	public String toString(){
		String s = "Connectivity object :";
		s += "\nConnected Components : "+connectedComponents.toString();
		s += "\nArticulation Points : "+articulationPoints.toString();
		s += "\nIsthmus : "+isthmus.toString();
		return s;
	}

	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************

	/**Get all connected components.
	 * @return connectedComponents a list of CC
	 */
	public LinkedList<TIntArrayList> getConnectedComponents() {
		return connectedComponents;
	}
	/**Get articulation points 
	 * @return articulationPoints 
	 */
	public BitSet getArticulationPoints() {
		return articulationPoints;
	}
	/**
	 * @return true iff i is an articulationPoint
	 */
	public boolean isArticulationPoints(int i) {
		return articulationPoints.get(i);
	}
	/**Get isthmus of the graph
	 * @return isthmus 
	 */
	public TIntArrayList getIsthmus() {
		return isthmus;
	}
}
