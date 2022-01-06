/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.connectivity;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.GraphFactory;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by ezulkosk on 5/22/15.
 */
public class ConnectedTest {

    @Test(groups = "10s")
    public void testConnectedArticulationX() {
        Model model = new Model();
        int n = 6;
        // build model
        UndirectedGraph GLB = new UndirectedGraph(model,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(4);
        GUB.addEdge(0,1);
        GUB.addEdge(0,3);
        GUB.addEdge(1,2);
        GUB.addEdge(1,3);
        GUB.addEdge(3,4);
        GUB.addEdge(4,5);
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        model.connected(graph).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertFalse(GLB.getNodes().contains(1));
        Assert.assertTrue(GLB.getNodes().contains(3));
        while (model.getSolver().solve());
    }

    @Test(groups = "10s")
    public void testConnectedArticulation() {
        Model model = new Model();
        int n = 7;
        // build model
        UndirectedGraph GLB = new UndirectedGraph(model,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(5);
        GUB.addEdge(0,1);
        GUB.addEdge(0,4);
        GUB.addEdge(1,2);
        GUB.addEdge(1,3);
        GUB.addEdge(2,3);
        GUB.addEdge(1,4);
        GUB.addEdge(4,5);
        GUB.addEdge(4,6);
        GUB.addEdge(5,6);
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        model.connected(graph).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertFalse(GLB.getNodes().contains(1));
        Assert.assertTrue(GLB.getNodes().contains(4));
        while (model.getSolver().solve());
    }

    @Test(groups = "10s")
    public void testConnectedArticulation1() {
        Model model = new Model();
        int n = 4;
        // build model
        UndirectedGraph GLB = new UndirectedGraph(model,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(3);
        GUB.addEdge(0,1);
        GUB.addEdge(1,2);
        GLB.addEdge(0,3);
        GUB.addEdge(0,3);
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        model.connected(graph).post();
        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertFalse(GLB.getNodes().contains(1));
    }

    @Test(groups = "10s")
    public void testConnectedArticulation2() {
        Model model = new Model();
        int n = 4;
        // build model
        UndirectedGraph GLB = new UndirectedGraph(model,n, SetType.BIPARTITESET,false);
        UndirectedGraph GUB = new UndirectedGraph(model,n,SetType.BIPARTITESET,false);
        for(int i=0;i<n;i++)GUB.addNode(i);
        GLB.addNode(0);
        GLB.addNode(2);
        GLB.addNode(3);
        GUB.addEdge(0,1);
        GUB.addEdge(1,2);
        GLB.addEdge(0,3);
        GUB.addEdge(0,3);
        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        model.connected(graph).post();

        try {
            model.getSolver().propagate();
        } catch (ContradictionException e) {
            e.printStackTrace();
            Assert.assertFalse(true);
        }
        Assert.assertTrue(GLB.getNodes().contains(1));
    }

    @Test(groups = "10s")
    public void testChocoConnected() {
        Model model = new Model();
        // build model
        UndirectedGraph GLB = new UndirectedGraph(model,2, SetType.BITSET,false);
        UndirectedGraph GUB = new UndirectedGraph(model,2,SetType.BITSET,false);

        GLB.addNode(0);

        GUB.addNode(0);
        GUB.addNode(1);
        GUB.addEdge(0,1);

        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        Assert.assertEquals(model.connected(graph).isSatisfied(), ESat.UNDEFINED);

        model.connected(graph).post();

        while (model.getSolver().solve()){
            System.out.println(graph);
        }
    }

	@Test(groups = "10s")
	public void testChocoConnectedEmpty() {
		Model model = new Model();
		// build model
		UndirectedGraph GLB = new UndirectedGraph(model, 2, SetType.BITSET,false);
		UndirectedGraph GUB = new UndirectedGraph(model, 2,SetType.BITSET,false);

		// if one wants a graph with >= 2 nodes he should use the node number constraint
		// connected only focuses on the graph structure to prevent two nodes not to be connected
		// if there is 0 or only 1 node, the constraint is therefore not violated
		UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

		Assert.assertEquals(model.connected(graph).isSatisfied(), ESat.TRUE);
		model.connected(graph).post();
		Assert.assertTrue(model.getSolver().solve());
	}

	@Test(groups = "10s")
	public void testChocoConnectedSingle() {
		Model model = new Model();
		// build model
		UndirectedGraph GLB = new UndirectedGraph(model, 2, SetType.BITSET,false);
		UndirectedGraph GUB = new UndirectedGraph(model, 2,SetType.BITSET,false);
		GLB.addNode(0);
		GUB.addNode(0);
		GUB.addNode(1);
		GUB.addEdge(0,1);

		UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

		Assert.assertEquals(model.connected(graph).isSatisfied(), ESat.UNDEFINED);
		model.connected(graph).post();
		while (model.getSolver().solve());
		Assert.assertTrue(model.getSolver().getSolutionCount() == 2);
	}

	@Test(groups = "10s")
	public void testChocoConnectedNot() {
		Model model = new Model();
		// build model
		UndirectedGraph GLB = new UndirectedGraph(model, 3, SetType.BITSET,false);
		UndirectedGraph GUB = new UndirectedGraph(model, 3,SetType.BITSET,false);
		GLB.addNode(0);
		GUB.addNode(0);
		GUB.addNode(1);
		GUB.addNode(2);
		GUB.addEdge(0,1);

		UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

		model.nbNodes(graph, model.intVar(3)).post();

		Assert.assertEquals(model.connected(graph).isSatisfied(), ESat.UNDEFINED);
		model.connected(graph).post();
		while (model.getSolver().solve());
		model.getSolver().printStatistics();
		Assert.assertTrue(model.getSolver().getSolutionCount() == 0);
	}

    @Test(groups = "10s")
    public void testChocoConnectedPA3() throws ContradictionException {
        Model m = new Model();
        UndirectedGraph LB = new UndirectedGraph(m, 3, SetType.BITSET, false);
        UndirectedGraph UB = new UndirectedGraph(m, 3, SetType.BITSET, false);
		LB.addNode(0);
		LB.addNode(2);
		UB.addNode(0);
		UB.addNode(1);
		UB.addNode(2);
		UB.addEdge(0, 1);
		UB.addEdge(1, 2);
        UndirectedGraphVar g = m.graphVar("g", LB, UB);

        m.connected(g).post();
		Solver s = m.getSolver();

		s.propagate();
		Assert.assertTrue(g.getMandatoryNodes().size() == 3);
		Assert.assertTrue(g.isInstantiated());

		while (m.getSolver().solve());
		Assert.assertTrue(s.getSolutionCount() == 1);
	}

	@Test(groups = "10s")
	public void testChocoConnectedIsthme() throws ContradictionException {
		Model m = new Model();
		UndirectedGraph LB = new UndirectedGraph(m, 3, SetType.BITSET, false);
		UndirectedGraph UB = new UndirectedGraph(m, 3, SetType.BITSET, false);
		UB.addNode(0);
		UB.addNode(1);
		UB.addNode(2);
		UB.addEdge(0, 1);
		UB.addEdge(1, 2);
		UndirectedGraphVar g = m.graphVar("g", LB, UB);
		m.nbNodes(g, m.intVar(3)).post();

		m.connected(g).post();
		Solver s = m.getSolver();

		s.propagate();
		Assert.assertTrue(g.getMandatoryNodes().size() == 3);
		Assert.assertTrue(g.isInstantiated());

		while (m.getSolver().solve());
		Assert.assertTrue(s.getSolutionCount() == 1);
	}

	@Test
	public void testReif() throws ContradictionException {
		Model m = new Model();
		int nb = 3;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);

		for (int i : new int[] { 0,1,2 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 1,2);
		add_neighbors(GUB, 1, 2);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		BoolVar isConnected = m.connected(graph).reify();
		IntVar nbNodes = m.intVar(0, 20);
		m.nbNodes(graph, nbNodes).post();
		m.arithm(isConnected,"+", nbNodes,"=",3).post();

		m.getSolver().propagate();
		Assert.assertTrue(!isConnected.isInstantiated());
		while (m.getSolver().solve());
		Assert.assertTrue(m.getSolver().getSolutionCount() == 7);
	}

	@Test
	public void testAP() throws ContradictionException {
		Model m = new Model();
		int nb = 33;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 10, 29 })
			GLB.addNode(i);
		for (int i : new int[] { 10, 12, 28, 29, 31, 32 })
			GUB.addNode(i);

		add_neighbors(GUB, 10, 28, 31);
		add_neighbors(GUB, 12, 29, 31, 32);
		add_neighbors(GUB, 28, 10, 31);
		add_neighbors(GUB, 29, 12, 31);
		add_neighbors(GUB, 31, 10, 12, 28, 29);
		add_neighbors(GUB, 32, 12);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);

		m.connected(graph).post();

		m.getSolver().propagate();

		Assert.assertTrue(graph.getMandatoryNodes().contains(31));
		Assert.assertTrue(graph.getMandatoryNodes().size()==3);
	}

	@Test
	public void testAPMini() throws ContradictionException {
		Model m = new Model();
		int nb = 4;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 2 })
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.connected(graph).post();

		m.getSolver().propagate();
		Assert.assertTrue(graph.getMandatoryNodes().contains(3));
		Assert.assertTrue(graph.getMandatoryNodes().size()==3);
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void testAPMiniNot() throws ContradictionException {
		Model m = new Model();
		int nb = 4;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 2 })
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.connected(graph).post();

		m.getSolver().propagate();
		Assert.assertTrue(graph.getMandatoryNodes().size()==1);
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void testAPMiniIsthma() throws ContradictionException {
		Model m = new Model();
		int nb = 4;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 3})
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3 })
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.connected(graph).post();

		m.getSolver().propagate();
		Assert.assertTrue(graph.getMandatoryNeighborsOf(0).contains(3));
		Assert.assertTrue(graph.getMandatoryNodes().size()==2);
		Assert.assertTrue(m.getSolver().solve());
	}

	@Test
	public void test2CC() throws ContradictionException {
		Model m = new Model();
		int nb = 6;

		UndirectedGraph GLB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		UndirectedGraph GUB = new UndirectedGraph(m, nb, SetType.BITSET, false);
		for (int i : new int[] { 0, 3})
			GLB.addNode(i);
		for (int i : new int[] { 0, 1, 2, 3, 4, 5})
			GUB.addNode(i);

		add_neighbors(GUB, 0, 3);
		add_neighbors(GUB, 1, 2, 3);
		add_neighbors(GUB, 2, 3);
		add_neighbors(GUB, 4, 5);

		UndirectedGraphVar graph = m.graphVar("G", GLB, GUB);
		m.connected(graph).post();

		m.getSolver().propagate();
		Assert.assertTrue(graph.getMandatoryNeighborsOf(0).contains(3));
		Assert.assertTrue(graph.getMandatoryNodes().size()==2);
		Assert.assertTrue(!graph.getPotentialNodes().contains(4));
		Assert.assertTrue(!graph.getPotentialNodes().contains(5));
		Assert.assertTrue(m.getSolver().solve());
	}

    private static void add_neighbors(UndirectedGraph g, int x, int... list) {
        for (int y : list)
            g.addEdge(x, y);
    }

    @Test(groups = "10s")
    public void testChocoConnected4() {
        Model model = new Model();
        // build model
        UndirectedGraph GLB = new UndirectedGraph(model, 2, SetType.BITSET,false);
        UndirectedGraph GUB = new UndirectedGraph(model, 2,SetType.BITSET,false);

        GUB.addNode(0);
        GUB.addNode(1);

        UndirectedGraphVar graph = model.graphVar("G", GLB, GUB);

        Assert.assertEquals(model.connected(graph).isSatisfied(), ESat.UNDEFINED);
    }

	@Test(groups="10s", timeOut=60000)
	public void generateTest() {
		// Generate solutions with filtering
		Model model = new Model();
		int n = 7;
		UndirectedGraph LB = GraphFactory.makeStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET);
		UndirectedGraph UB = GraphFactory.makeCompleteStoredUndirectedGraph(model, n, SetType.BITSET, SetType.BITSET, false);
		UndirectedGraphVar g = model.graphVar("g", LB, UB);
		model.connected(g).post();
		model.nbEdges(g, model.intVar(4)).post();
		while (model.getSolver().solve()) {}
		// Generate solutions with checker
		Model model2 = new Model();
		UndirectedGraph LB2 = GraphFactory.makeStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET);
		UndirectedGraph UB2 = GraphFactory.makeCompleteStoredUndirectedGraph(model2, n, SetType.BITSET, SetType.BITSET, false);
		UndirectedGraphVar g2 = model2.graphVar("g", LB2, UB2);
		model2.nbEdges(g2, model2.intVar(4)).post();
		Constraint cons = model2.connected(g2);
		int count = 0;
		while (model2.getSolver().solve()) {
			if (cons.isSatisfied() == ESat.TRUE) {
				count++;
			}
		}
		Assert.assertEquals(model.getSolver().getSolutionCount(), count);
	}
}
