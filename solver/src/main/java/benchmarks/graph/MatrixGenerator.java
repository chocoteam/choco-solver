package benchmarks.graph;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Random;

/**Class containing data generation methods
 * @author info */
public class MatrixGenerator {

	// --- Data generation
	/**Create an adjacency matrix considering some requirements 
	 * Do exactly nbNodes nodes and nbConnectedComp connected components
	 * but does not guaranty to have m arcs
	 * this is why there is an IntObj : to get back the real number of arcs
	 * 
	 * @param nbNodes expected number of nodes 
	 * @param m targeted number of arcs, but the real amount of arcs can be lower (if all CC are cliques) or greater (if m is too small to have the expected number of CC)
	 * @param nbConnectedComp expected number of connected components, 
	 * 			if(nbConnectedComp<=0) then the number of connected components is undefined
	 * @param oriented false iff the matrix must be symmetric, true otherwise
	 * @return an adjacency matrix 
	 */
	public static boolean[][] makeSGData(int nbNodes, IntObj m, int nbConnectedComp, boolean oriented){
		int nbArcDone = 0;
		int nbArcs = m.getVal();

		if (nbArcs>nbNodes*nbNodes){Exception e = new Exception("cannot make more than nbNodes^2 arcs");e.printStackTrace();System.exit(0);}
		if (nbConnectedComp>nbNodes){Exception e = new Exception("cannot make more connected component than the number of nodes");e.printStackTrace();System.exit(0);}

		boolean[][] matrix = new boolean[nbNodes][nbNodes];
		Random rd = new Random();
		// default CC number not specifyed
		if (nbConnectedComp <= 0){ 
			ArrayList<int[]> arcs = new ArrayList<int[]>(nbNodes*nbNodes);
			for (int i=0; i<nbNodes; i++){
				int k = 0;
				if (!oriented){
					k = i+1;
				}
				for (int j=k; j<nbNodes; j++){
					if (i!=j){
						arcs.add(new int[]{i,j});
					}
				}
			}
			Collections.shuffle(arcs);
			for (int k=0;k<nbArcs;k++){
				int[] arc = arcs.remove(arcs.size()-1);
				matrix[arc[0]][arc[1]] = true;
				if (!oriented){
					matrix[arc[1]][arc[0]] = true;
				}
			}
			// CC number is specifyed
		}else{
			int[] nodesComp = new int[nbNodes];
			LinkedList<Integer> allNodes = new LinkedList<Integer>();
			for (int i = 0; i<nbNodes; i++){
				allNodes.addFirst(i);
			}
			Collections.shuffle(allNodes);
			ArrayList<Integer>[] CC_UnlinkedNodes = new ArrayList[nbConnectedComp];
			ArrayList<Integer>[] CC_LinkedNodes = new ArrayList[nbConnectedComp];
			for (int cc = 0; cc<nbConnectedComp; cc++){
				CC_UnlinkedNodes[cc] = new ArrayList<Integer>(nbNodes/nbConnectedComp);
				CC_LinkedNodes[cc] = new ArrayList<Integer>(nbNodes/nbConnectedComp);
				int node = allNodes.removeFirst();
				nodesComp[node] = cc;
				CC_UnlinkedNodes[cc].add(node);
			}
			for (int i:allNodes){ // set which node is in which CC
				nodesComp[i] = rd.nextInt(nbConnectedComp);
				CC_UnlinkedNodes[nodesComp[i]].add(i);
			}
			allNodes.clear();
			for (int i = 0; i<nbConnectedComp; i++){ // provides randomness
				Collections.shuffle(CC_UnlinkedNodes[i],rd);
			}
			// make connected components
			for (int cc = 0; cc<nbConnectedComp; cc++){ // provides randomness
				CC_LinkedNodes[cc].add(CC_UnlinkedNodes[cc].remove(0));
				while (CC_UnlinkedNodes[cc].size()>0){
					int newOne = CC_UnlinkedNodes[cc].remove(0);
					int extremity = CC_LinkedNodes[cc].get(rd.nextInt(CC_LinkedNodes[cc].size()));
					if (matrix[newOne][extremity] || matrix[extremity][newOne]&&!oriented){
						Exception e = new Exception("Cannot pick the same arc twice");
						e.printStackTrace();
						System.exit(0);
					}
					if (newOne==extremity){
						Exception e = new Exception("error in data generation");
						e.printStackTrace();
						System.exit(0);
					}
					matrix[newOne][extremity] = true;
					if (!oriented){
						matrix[extremity][newOne] = true;
					}
					nbArcDone++;
					CC_LinkedNodes[cc].add(newOne);
				}
			}
			// add other arcs
			ArrayList<int[]> possibleArcs = new ArrayList<int[]>(nbNodes*nbNodes);
			for (int c = 0; c<nbConnectedComp; c++){
				for (int i=0; i<CC_LinkedNodes[c].size(); i++){
					int k = 0;
					if (!oriented){
						k = i+1;
					}
					for (int i2=k; i2<CC_LinkedNodes[c].size(); i2++){
						if (i!=i2 && !matrix[CC_LinkedNodes[c].get(i)][CC_LinkedNodes[c].get(i2)]){
							if (CC_LinkedNodes[c].get(i)==CC_LinkedNodes[c].get(i2)){
								Exception e = new Exception("error in data generation");
								e.printStackTrace();
								System.exit(0);
							}
							possibleArcs.add(new int[]{CC_LinkedNodes[c].get(i),CC_LinkedNodes[c].get(i2)});
						}
					}
				}
			}
			Collections.shuffle(possibleArcs);
			while (nbArcDone<nbArcs){
				if (possibleArcs.size()==0){
					String s = "";
					if (!oriented){s="non-";}
					System.err.println("Cannot make "+nbArcs+" arcs in a "+s+"oriented graph of "+nbNodes+" nodes and "+nbConnectedComp+" CC" +
							"\n the program will continue but only "+(nbArcDone)+" arcs are generated");
					m.setVal(nbArcDone);
					return matrix;
				}
				int[] arc = possibleArcs.remove(possibleArcs.size()-1);
				if (matrix[arc[0]][arc[1]] || (matrix[arc[1]][arc[0]]&&!oriented)){
					Exception e = new Exception("Cannot pick the same arc twice");
					e.printStackTrace();
					System.exit(0);
				}
				matrix[arc[0]][arc[1]] = true;
				if (!oriented){
					matrix[arc[1]][arc[0]] = true;
				}
				nbArcDone++;
			}
		}
		return matrix;
	}

	// --- Data generation
	public static boolean[][] makeConnectedData(int nbNodes, int nbSuccsPerNode){
		boolean[][] matrix = new boolean[nbNodes][nbNodes];
		Random rd = new Random(0);
		BitSet[] posa = new BitSet[nbNodes];
		for(int i=0;i<nbNodes;i++){
			for(int j=0;j<nbNodes; j++){
				posa[i].set(j);
			}
		}
		ArrayList<Integer> CC_UnlinkedNodes = new ArrayList<Integer>();
		ArrayList<Integer> CC_LinkedNodes = new ArrayList<Integer>();
		for (int i=0;i<nbNodes;i++){ 
			CC_UnlinkedNodes.add(i);
		}
		Collections.shuffle(CC_UnlinkedNodes);
		// make the connected component (tree)
		CC_LinkedNodes.add(CC_UnlinkedNodes.remove(0));
		while (CC_UnlinkedNodes.size()>0){
			int newOne = CC_UnlinkedNodes.remove(0);
			int extremity = CC_LinkedNodes.get(rd.nextInt(CC_LinkedNodes.size()));
			if (matrix[newOne][extremity]){
				Exception e = new Exception("Cannot pick the same arc twice");
				e.printStackTrace();
				System.exit(0);
			}
			if (newOne==extremity){
				Exception e = new Exception("error in data generation");
				e.printStackTrace();
				System.exit(0);
			}
			matrix[newOne][extremity] = true;
			posa[newOne].clear(extremity);
			CC_LinkedNodes.add(newOne);
		}
		// add other arcs
		int idx,num;
		for(int i=0;i<nbNodes;i++){
			for(int j=1;j<nbSuccsPerNode; j++){
				num = rd.nextInt(posa[i].cardinality());
				idx = posa[i].nextSetBit(0);
				while(num>0){
					num--;
					idx = posa[i].nextSetBit(idx+1);
				}
				matrix[i][idx] = true;
				posa[i].clear(idx);
			}
		}
		return matrix;
	}
}
