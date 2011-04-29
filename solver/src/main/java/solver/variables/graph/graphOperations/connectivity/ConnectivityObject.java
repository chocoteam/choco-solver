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
