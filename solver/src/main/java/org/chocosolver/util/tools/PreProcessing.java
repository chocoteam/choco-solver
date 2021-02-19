/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.binary.PropEqualX_Y;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;

import java.util.*;
import java.util.function.Predicate;

/**
 * This class contains various methods for applying pre-processing rules to a model.
 *
 * <p>The methods in this class are ignored
 * if the resolution has already started
 * ({@code model.getSolver().getSearchState() != SearchState.NEW}).
 *
 * @author Charles Prud'homme
 * @since 06/10/2020
 */
public class PreProcessing {

    private static final Predicate<Constraint> ARITHM = c -> c.getName().equals(ConstraintsName.ARITHM);
    private static final Predicate<Constraint> POSTED = c -> c.getStatus() == Constraint.Status.POSTED;
    private static final Predicate<Constraint> EQXY = c -> c.getPropagator(0) instanceof PropEqualX_Y;


    /**
     * Preprocess the model to detect integer (and boolean) equality constraint
     * and turn them into bigger arity constraint when possible.
     * <p>
     * If the resolution has already started ({@code model.getSolver().getSearchState() != SearchState.NEW}),
     * the method stops without applying any pre-processing.
     *
     * @param model the model to pre-process
     * @return the list of components. A component is a list of {@link IntVar} all equal,
     * it could be helpful to design a search strategy.
     * @implNote The model's constraints are iterated in order to filter constraint with the
     * following properties:
     * <li>
     *     <ul>ConstraintsName.ARITHM</ul>
     *     <ul>Constraint.Status.POSTED</ul>
     *     <ul>instanceof PropEqualX_Y</ul>
     * </li>
     * For each them, add two nodes and an edge in a {@link UndirectedGraph}.
     * Then, look for strongly connected components with a DFS and labelling operations.
     * Once found, old constraints are removed and new ones are posted instead.
     */
    public static List<List<IntVar>> detectIntEqualities(Model model) {
        // 1. if the search started, skip the call
        if (model.getSolver().getSearchState() != SearchState.NEW) {
            return Collections.emptyList();
        }
        // 2. get all integer variables and a map to their position in the array
        IntVar[] ivars = model.retrieveIntVars(true);
        int pos = 0;
        TIntIntHashMap id2pos = new TIntIntHashMap();
        for (IntVar i : ivars) {
            id2pos.put(i.getId(), pos++);
        }
        // 3. create a undirected graph to store equality constraints
        UndirectedGraph g = new UndirectedGraph(pos, SetType.LINKED_LIST, false);
        Arrays.stream(model.getCstrs())
                .filter(ARITHM.and(POSTED).and(EQXY))
                .map(c -> c.getPropagator(0))
                .forEach(p -> {
                    g.addNode(id2pos.get(p.getVar(0).getId()));
                    g.addNode(id2pos.get(p.getVar(1).getId()));
                    g.addEdge(id2pos.get(p.getVar(0).getId()),
                            id2pos.get(p.getVar(1).getId()));
                });
        // 4. Prepare graph DFS for all unlabelled nodes
        List<List<IntVar>> components = new ArrayList<>();
        BitSet visited = new BitSet(pos);
        for (int v : g.getNodes()) {
            if (!visited.get(v)) {
                ArrayList<IntVar> component = new ArrayList<>();
                scc(v, visited, g, component, ivars);
                assert component.size() > 1 : "found a SCC of size 1";
                components.add(component);
            }
        }
        // 5. Remove all equality constraints
        model.unpost(Arrays.stream(model.getCstrs())
                .filter(ARITHM.and(POSTED).and(EQXY))
                .toArray(Constraint[]::new));
        // 6 Post new equality constraints
        for (List<IntVar> component : components) {
            if (component.size() > 2) {
                model.allEqual(component.toArray(new IntVar[0])).post();
            } else if (component.size() == 2) {
                component.get(0).eq(component.get(1)).post();
            }
        }
        // 7. return the list of strongly connected component, to better design search strategy.
        return components;
    }

    /**
     * Depth-first search of {@code g} to get strongly connected component from a node {@code v}.
     * Stores the elements of the component in {@code component}.
     *
     * @param v         a unvisited node
     * @param visited   visited nodes
     * @param g         undirected graph
     * @param component list of {@link IntVar} in the component
     * @param ivars     list of variables
     * @implNote The undirected grap {@code g} labels nodes wrt to their position in {@code ivars}.
     * That's why we can call {@code component.add(ivars[v]);}.
     */
    private static void scc(int v, BitSet visited, UndirectedGraph g,
                            ArrayList<IntVar> component,
                            IntVar[] ivars) {
        component.add(ivars[v]);
        visited.set(v);
        for (int x : g.getNeighOf(v)) {
            if (!visited.get(x)) {
                scc(x, visited, g, component, ivars);
            }
        }
    }
}
