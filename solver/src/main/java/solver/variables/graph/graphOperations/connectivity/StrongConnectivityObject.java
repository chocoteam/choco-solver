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
