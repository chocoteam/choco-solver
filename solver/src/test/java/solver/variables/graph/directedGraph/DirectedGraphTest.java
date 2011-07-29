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

import org.testng.annotations.Test;
import solver.variables.graph.GraphType;
import solver.variables.graph.graphOperations.connectivity.StrongConnectivityFinder;
import solver.variables.graph.graphOperations.coupling.BipartiteMaxCardMatching;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 11 févr. 2011
 */
public class DirectedGraphTest {

    @Test(groups = "1s")
    public void testDirectedGraph() {
        int order = 4;
        boolean[][] data = {    {false,true,true,false},
                                {true,false,true,false},
                                {false,false,false,true},
                                {true,false,false,false}    };

        System.out.println("creation");
        DirectedGraph g1 = new DirectedGraph(order,data, GraphType.LINKED_LIST);
        DirectedGraph g2 = new DirectedGraph(order,data,GraphType.MATRIX);

        System.out.println(g1);
        System.out.println(g2);
        System.out.println("-----------------");
        
//        System.out.println(ConnectivityFinder.findAllCCandAP(g2));
//        System.out.println(StrongConnectivityFinder.findAll(g1));
//        System.out.println(BipartiteMaxCardMatching.maxCardBipartiteMatching(g1).size());

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
