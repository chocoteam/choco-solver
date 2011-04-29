package solver.variables.graph.undirectedGraph;

import org.testng.annotations.Test;
import solver.variables.graph.GraphType;
import solver.variables.graph.graphOperations.connectivity.ConnectivityFinder;
import solver.variables.graph.graphOperations.coupling.BipartiteMaxCardMatching;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 10 févr. 2011
 */
public class UndirectedGraphTest {

    @Test(groups = "1s")
    public void testUndirectedGraph() {
        int order = 4;
        boolean[][] data = {    {false,true,true,true},
                                {true,false,true,false},
                                {true,true,false,true},
                                {true,false,true,false}    };

        System.out.println("creation");
        UndirectedGraph g1 = new UndirectedGraph(order,data, GraphType.SPARSE);
        UndirectedGraph g2 = new UndirectedGraph(order,data,GraphType.DENSE);

        System.out.println(g1);
        System.out.println(g2);
        System.out.println("-----------------");
        System.out.println(ConnectivityFinder.findAllCCandAP(g2));
        System.out.println("card of max bipartite matching : "+BipartiteMaxCardMatching.maxCardBipartiteMatching(g1).size());

//        System.out.println("enforecNode");
//        g1.enforceNode(0,null);
//        g2.enforceNode(0,null);
//
//        System.out.println(g1);
//        System.out.println(g2);
//        System.out.println("-----------------");
//
//        System.out.println("enforceNext");
//        g1.enforceNext(0,3,null);
//        g2.enforceNext(0,3,null);
//
//        System.out.println(g1);
//        System.out.println(g2);
//        System.out.println("-----------------");
//
//        System.out.println("removeNode");
//        g1.removeNode(2,null);
//        g2.removeNode(2,null);
//
//        System.out.println(g1);
//        System.out.println(g2);
//        System.out.println("-----------------");
//
//        System.out.println("removeNext");
//        g1.removeNext(1,0,null);
//        g2.removeNext(1,0,null);
//
//        System.out.println(g1);
//        System.out.println(g2);
//        System.out.println("-----------------");

    }

}
