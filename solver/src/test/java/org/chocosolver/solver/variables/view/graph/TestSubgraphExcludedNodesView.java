package org.chocosolver.solver.variables.view.graph;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.variables.SetVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test class for SubgraphExcludedNodesView
 *
 * @author Dimitri Justeau-Allaire
 * @since 31/03/2021
 */
public class TestSubgraphExcludedNodesView {

    @Test(groups="1s", timeOut=60000)
    public void testInstantiateAndGenerate() {
        Model m = new Model();
        int n = 5;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        UndirectedGraphVar g2 = m.subgraphExcludedNodesView(g, SetFactory.makeConstantSet(new int[] {0, 4}));
        Assert.assertEquals(g2.getMandatoryNodes().size(), 0);
        Assert.assertEquals(g2.getPotentialNodes().size(), 3);
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(0));
            Assert.assertTrue(!g2.getValue().containsNode(4));
            for (int i = 1; i < 4; i++) {
                if (g.getValue().containsNode(i)) {
                    Assert.assertTrue(g2.getValue().containsNode(i));
                    for (int j : g.getValue().getNeighborsOf(i)) {
                        if (j != 0 && j != 4) {
                            Assert.assertTrue(g2.getValue().containsEdge(i, j));
                        }
                    }
                }
            }
        }
    }

    @Test(groups="1s", timeOut=60000)
    public void testConstrained() {
        Model m = new Model();
        int n = 7;
        UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET);
        UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(m, n, SetType.BITSET, SetType.BITSET, false);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);
        SetVar nodesG = m.graphNodeSetView(g);
        m.member(0, nodesG).post();
        ISet excluded = SetFactory.makeConstantSet(new int[] {0, 4});
        UndirectedGraphVar g2 = m.subgraphExcludedNodesView(g, excluded);
        m.nbNodes(g2, m.intVar(1, 4)).post();
        m.connected(g2).post();
        m.getSolver().setSearch(Search.graphVarLexSearch(g));
        while (m.getSolver().solve()) {
            Assert.assertTrue(!g2.getValue().containsNode(0));
            Assert.assertTrue(g.getValue().containsNode(0));
            Assert.assertTrue(!g2.getValue().containsNode(4));
            Assert.assertTrue(g2.getValue().getNodes().size() >= 1 && g2.getValue().getNodes().size() <= 3);
            for (int i = 0; i < n; i++) {
                if (!excluded.contains(i)) {
                    if (g.getValue().containsNode(i)) {
                        Assert.assertTrue(g2.getValue().containsNode(i));
                        for (int j : g.getValue().getNeighborsOf(i)) {
                            if (!excluded.contains(j)) {
                                Assert.assertTrue(g2.getValue().containsEdge(i, j));
                            }
                        }
                    }
                }
            }
        }
    }
}
