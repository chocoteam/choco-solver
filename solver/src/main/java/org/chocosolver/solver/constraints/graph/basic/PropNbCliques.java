/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.graph.basic;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.F;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.mis.MDRk;
import org.chocosolver.solver.constraints.nary.nvalue.amnv.rules.R;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.UndirectedGraphVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.BitSet;

/**
 * Propagator for the number of cliques in a graph
 *
 * @author Jean-Guillaume Fages
 */
public class PropNbCliques extends Propagator<Variable> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    private final UndirectedGraphVar g;
    private final UndirectedGraph support;
    private final IntVar[] nb;
    private final R[] rules;
    private final F heur;
    private int delta;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    public PropNbCliques(UndirectedGraphVar g, IntVar nb) {
        super(new Variable[]{g, nb}, PropagatorPriority.QUADRATIC, false);
        this.g = g;
        this.support = new UndirectedGraph(g.getNbMaxNodes(), SetType.BITSET, false);
        this.nb = new IntVar[]{nb};
        this.rules = new R[]{new Rcustom()};
        this.heur = new MDRk(support, 30);
    }

    //***********************************************************************************
    // METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        // reset
        int n = g.getNbMaxNodes();
        support.getNodes().clear();
        for (int i = 0; i < n; i++) {
            support.getNeighborsOf(i).clear();
        }
        ISet nodes = g.getMandatoryNodes();
        for (int i : nodes) {
            support.addNode(i);
        }
        for (int i : nodes) {
            ISet nei = g.getPotentialNeighborsOf(i);
            for (int j : nei) {
                if (i < j) {
                    support.addEdge(i, j);
                }
            }
        }
        delta = n - g.getMandatoryNodes().size();
        // algorithm
        heur.prepare();
        do {
            heur.computeMIS();
            for (R rule : rules) {
                rule.filter(nb, support, heur, this);
            }
        } while (heur.hasNextMIS());
    }

    @Override
    public ESat isEntailed() {
        return ESat.TRUE; // redundant propagator (in addition to transitivity and nbConnectedComponents
    }

    class Rcustom implements R {
        @Override
        public void filter(IntVar[] nbCliques, UndirectedGraph graph, F heur, Propagator aCause) throws ContradictionException {
            assert nbCliques.length == 1;
            int n = graph.getNbMaxNodes();
            BitSet mis = heur.getMIS();
            int LB = heur.getMIS().cardinality() - delta;
            nbCliques[0].updateLowerBound(LB, aCause);
            if (LB == nbCliques[0].getUB()) {
                ISet nei;
                for (int i = mis.nextClearBit(0); i >= 0 && i < n; i = mis.nextClearBit(i + 1)) {
                    int mate = -1;
                    nei = graph.getNeighborsOf(i);
                    for (int j : nei) {
                        if (mis.get(j)) {
                            if (mate == -1) {
                                mate = j;
                            } else if (mate >= 0) {
                                mate = -2;
                                break;
                            }
                        }
                    }
                    if (mate >= 0) {
                        g.enforceEdge(i, mate, aCause);
                    }
                }
            }
        }
    }
}
