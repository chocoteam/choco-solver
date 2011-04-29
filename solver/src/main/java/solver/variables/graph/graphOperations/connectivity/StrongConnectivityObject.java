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

/**Class which stores 
 * strongly connected components (SCC),
 * strong articulation points (SAP)
 * and strong bridges (SB)
 * @author Jean-Guillaume Fages
 */
public class StrongConnectivityObject {

	//***********************************************************************************
	// VARIABLES
	//***********************************************************************************

	private LinkedList<INeighbors> stronglyConnectedComponents;
	private LinkedList<int[]> strongBridges;
	private BitSet strongArticulationPoints;

	//***********************************************************************************
	// CONSTRUCTORS
	//***********************************************************************************

	/** Create a StrongConnectivityObject that will store 
	 * @param allSCC strongly connected components
	 * @param allSAP strong articulation points
	 * @param allSB  strong bridges
	 */
	public StrongConnectivityObject(LinkedList<INeighbors> allSCC,BitSet allSAP, LinkedList<int[]> allSB) {
		strongArticulationPoints = allSAP;
		strongBridges = allSB;
		stronglyConnectedComponents = allSCC;
	}
	
	//***********************************************************************************
	// METHODS
	//***********************************************************************************
	
	public String toString(){
		String s = "Strong connectivity object :";
		if(stronglyConnectedComponents != null){
			s += "\nSCC : ";
			for (INeighbors scc:stronglyConnectedComponents){
				s+="\n"+scc;
			}
		}else{
			s+="no SCC computed";
		}
		if(strongArticulationPoints != null){
			s+="\nSAP : "+strongArticulationPoints.toString();
		}else{
			s+="no SAP computed";
		}
		if(strongBridges != null){
			s+="\nSB : ";
			for(int[] arc:strongBridges){
				s+="("+arc[0]+","+arc[1]+"), ";
			}
		}else{
			s+="no SB computed";
		}return s;
	}
	
	//***********************************************************************************
	// ACCESSORS
	//***********************************************************************************
	
	/**Get all strongly connected components.
	 * @return strongConnectedComponents a list of SCC
	 */
	public LinkedList<INeighbors> getStronglyConnectedComponents() {
		return stronglyConnectedComponents;
	}
	/**Get all strong bridges
	 * @return strongBridges the list of strong bridges of the graph
	 */
	public LinkedList<int[]> getStrongBridges() {
		return strongBridges;
	}
	/**Get a bitset representing strong articulation points : 
	 * the bit i is true means that the node of index i is a strong articulation point
	 * @return strongArticulationPoints 
	 */
	public BitSet getStrongArticulationPoints() {
		return strongArticulationPoints;
	}
}
