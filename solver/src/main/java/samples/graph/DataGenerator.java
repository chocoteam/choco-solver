package samples.graph;

import java.util.BitSet;
import java.util.Random;

/**
 * @author Jean-Guillaume Fages
 * Class enabling to generate input graph random data
 */
public class DataGenerator {

	public static int seed = 0;
	/**Create an adjacency matrix randomly for tree : a connected digraph with at least one root
	 * @param nbNodes number of nodes in the expected graph data
	 * @param nbSuccsPerNode outdegree of nodes
	 * @return an adjacency matrix
	 */
	public static BitSet[] makeTreeData(int nbNodes, int nbSuccsPerNode){
		Random rd = new Random(seed);
		BitSet[] data = new BitSet[nbNodes];
		for(int h=0;h<nbNodes;h++){
			data[h] = new BitSet(nbNodes);
		}
		BitSet linked = new BitSet(nbNodes);
		// make the connected component (tree)
		data[0].set(0); // at least one root
		linked.set(0);
		for(int h=1;h<nbNodes;h++){
			int newOne = getIthClearBit(rd.nextInt(nbNodes-linked.cardinality()), linked);
			int extremity = getIthSetBit(rd.nextInt(linked.cardinality()), linked);
			if (data[newOne].get(extremity)){
				Exception e = new Exception("Cannot pick the same arc twice");
				e.printStackTrace();
				System.exit(0);
			}
			if (newOne==extremity){
				Exception e = new Exception("error in data generation");
				e.printStackTrace();
				System.exit(0);
			}
			data[newOne].set(extremity);
			linked.set(newOne);
		}
		// add other arcs
		int idx,num;
		for(int i=0;i<nbNodes;i++){
			for(int j=1;j<nbSuccsPerNode; j++){
				num = rd.nextInt(nbNodes-data[i].cardinality());
				idx = getIthClearBit(num, data[i]);
				data[i].set(idx);
			}
		}
		return data;
	}
	
	private static int getIthClearBit(int i, BitSet b){
		int idx=b.nextClearBit(0);
		while(i>0){
			i--;
			idx = b.nextClearBit(idx+1);
		}
		return idx;
	}
	private static int getIthSetBit(int i, BitSet b){
		int idx=b.nextSetBit(0);
		while(i>0){
			i--;
			idx = b.nextSetBit(idx+1);
		}
		return idx;
	}
}
