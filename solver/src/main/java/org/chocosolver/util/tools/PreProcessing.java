/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.tools;

import gnu.trove.map.hash.TIntIntHashMap;
import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.ConstraintsName;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropEqualX_Y;
import org.chocosolver.solver.constraints.binary.PropNotEqualX_Y;
import org.chocosolver.solver.constraints.nary.alldifferent.AllDifferent;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.SearchState;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.graphs.UndirectedGraph;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.jgrapht.Graph;
import org.jgrapht.alg.clique.BronKerboschCliqueFinder;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
    private static final Predicate<Constraint> NEQXY = c -> c.getPropagator(0) instanceof PropNotEqualX_Y;


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
        // 3. create an undirected graph to store equality constraints
        UndirectedGraph g = new UndirectedGraph(pos, SetType.LINKED_LIST, false);
        int[] nbEqs = {0, 0};
        Arrays.stream(model.getCstrs())
                .filter(ARITHM.and(POSTED).and(EQXY))
                .forEach(c -> {
                    Propagator<?> p = c.getPropagator(0);
                    g.addNode(id2pos.get(p.getVar(0).getId()));
                    g.addNode(id2pos.get(p.getVar(1).getId()));
                    g.addEdge(id2pos.get(p.getVar(0).getId()),
                            id2pos.get(p.getVar(1).getId()));
                    model.unpost(c);
                    nbEqs[0]++;
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
        // 5 Post new equality constraints
        int nbAllEqs = 0;
        for (List<IntVar> component : components) {
            if (component.size() > 2) {
                model.allEqual(component.toArray(new IntVar[0])).post();
                nbAllEqs++;
                nbEqs[1] += component.size();
            } else if (component.size() == 2) {
                component.get(0).eq(component.get(1)).post();
            }
        }
        model.getSolver().log().white()
                .printf("%d 'eq' constraint%s found\n> Replace %d of them with %d 'allEqual' constraint%s\n",
                nbEqs[0], nbEqs[0]>1?"s":"", nbEqs[1], nbAllEqs, nbAllEqs>1?"s":"");
        // 6. return the list of strongly connected component, to better design search strategy.
        return components;
    }

    /**
     * Preprocess the model to detect integer (and boolean) equality constraint
     * and turn them into bigger arity constraint when possible.
     * <p>
     * If the resolution has already started ({@code model.getSolver().getSearchState() != SearchState.NEW}),
     * the method stops without applying any pre-processing.
     *
     * @param model the model to pre-process
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
    public static void detectIntInequalities(Model model) {
        // 1. if the search started, skip the call
        if (model.getSolver().getSearchState() != SearchState.NEW) {
            return;
        }
        // 2. get all integer variables and a map to their position in the array
        IntVar[] ivars = model.retrieveIntVars(true);
        int pos = 0;
        TIntIntHashMap id2pos = new TIntIntHashMap();
        for (IntVar i : ivars) {
            id2pos.put(i.getId(), pos++);
        }
        Graph<Integer, DefaultEdge> cliques = new SimpleGraph<>(DefaultEdge.class);
        // 3. create an undirected graph to store equality constraints
        int[] nbNqs = {0, 0};
        Arrays.stream(model.getCstrs())
                .filter(ARITHM.and(POSTED).and(NEQXY))
                .forEach(c -> {
                    Propagator<?> p = c.getPropagator(0);
                    cliques.addVertex(id2pos.get(p.getVar(0).getId()));
                    cliques.addVertex(id2pos.get(p.getVar(1).getId()));
                    cliques.addEdge(id2pos.get(p.getVar(0).getId()), id2pos.get(p.getVar(1).getId()));
                    model.unpost(c);
                    nbNqs[0]++;
                });
        // 5. Post new equality constraints
        BronKerboschCliqueFinder<Integer, DefaultEdge> max = new BronKerboschCliqueFinder<>(cliques);
        int nbAlldiff = 0;
        for (Set<Integer> cl : max) {
            Integer[] clique = cl.toArray(new Integer[0]);
            if (clique.length == 1)
                continue;
            IntVar[] diff = Arrays.stream(clique)
                    .mapToInt(i -> i)
                    .mapToObj(i -> ivars[i])
                    .toArray(IntVar[]::new);
            model.allDifferent(diff).post();
            nbAlldiff++;
            nbNqs[1] += diff.length;
        }
        model.getSolver().log().white()
                .printf("%d 'neq' constraint%s found\n> Replace %d of them with %d 'allDifferent' constraint%s\n",
                        nbNqs[0], nbNqs[0] > 1 ? "s" : "", nbNqs[1], nbAlldiff, nbAlldiff > 1 ? "s" : "");
        if (nbAlldiff > 0){
            impliedConstraints(model);
        }
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
        for (int x : g.getNeighborsOf(v)) {
            if (!visited.get(x)) {
                scc(x, visited, g, component, ivars);
            }
        }
    }

    /**
     * This method adds constraints implied by AllDifferent constraints.
     * For each of them, it finds two assignments of the variables
     * using the minimum cost maximum flow formulation and a sum constraint on both sides.
     *
     * @param model the model to pre-process
     */
    public static void impliedConstraints(Model model) {
        // Min Cost Max Flow formulation
        List<AllDifferent> cstrs = Arrays.stream(model.getCstrs())
                .filter(c -> c instanceof AllDifferent)
                .map(c -> (AllDifferent) c)
                .collect(Collectors.toList());
        IntIterableRangeSet values = new IntIterableRangeSet();
        int nbImplied = 0;
        for (AllDifferent c : cstrs) {
            values.clear();
            //noinspection unchecked
            IntVar[] vars = ((Propagator<IntVar>) c.getPropagator(0)).getVars();
            for (IntVar v : vars) {
                values.addAll(v);
            }
            int n = c.getPropagator(0).getNbVars();
            int lb = values.stream().limit(n).sum();
            int ub = values.stream().skip(values.size() - n).sum();
            model.sum(vars, ">=", lb).post();
            model.sum(vars, "<=", ub).post();
            nbImplied += 2; // two constraints posted
        }
        model.getSolver().log().white()
                .printf("%d 'allDifferent' constraint%s found\n> Add %d implied constraints\n",
                        cstrs.size(), cstrs.size() > 1 ? "s" : "", nbImplied);
    }


    /**
     * This method is called after the initial propagation and before the search loop starts.
     * It sequentially applies Singleton Arc Consistency on every combination of (variable, value).
     * If a combination leads to inconsistency, the value is removed from the domain of the variable.
     * The method ends when the time limit is reached or when all combination have been checked.
     *
     * @implSpec A first propagation must have been done before calling this method.
     */
    public static void sac(Model m, long timeLimitInMS) {
        if (!m.getSolver().getEngine().isInitialized()) {
            try {
                m.getSolver().propagate();
            } catch (ContradictionException e) {
                throw new SolverException("Preprocessing failed");

            }
        }
        if (timeLimitInMS > 0 && m.getSettings().warnUser()) {
            m.getSolver().log().white().printf("Running SAC step (%dms).\n", timeLimitInMS);
        }
        long tl = System.currentTimeMillis() + timeLimitInMS;
        IntVar[] ivars = m.retrieveIntVars(true);
        Arrays.sort(ivars, (v, w) -> {
            int d = v.getNbProps() - w.getNbProps();
            if (d == 0) {
                d = w.getDomainSize() - v.getDomainSize();
                if (d == 0) {
                    d = v.getId() - w.getId();
                }
            }
            return d;
        });
        long before = Arrays.stream(ivars)
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        boolean hasChanged = true;
        loop:
        while (hasChanged) {
            hasChanged = false;
            for (int i = 0; i < ivars.length; i++) {
                IntVar v = ivars[i];
                if (!v.isInstantiated()) { // if the variable is not instantiated
                    DisposableValueIterator it = v.getValueIterator(true);
                    while (it.hasNext()) {
                        if (timeLimitInMS > 0 && System.currentTimeMillis() > tl) {
                            break loop;
                        }
                        int a = it.next();
                        m.getSolver().pushTrail();
                        boolean noSupport = hasNoSupport(m, v, a);
                        m.getSolver().cancelTrail();
                        if (noSupport) {
                            try {
                                hasChanged |= v.removeValue(a, Cause.Null);
                                m.getSolver().getEngine().propagate();
                                /*if (m.getSettings().warnUser()) {
                                    m.getSolver().log().white().printf("Preprocessing removed value %d from %s\n", a, v.getName());
                                }*/
                            } catch (ContradictionException e) {
                                throw new SolverException("Preprocessing failed");
                            }
                        }
                    }
                    it.dispose();
                }
            }
        }
        long after = Arrays.stream(ivars)
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        if (before - after > 0 && m.getSettings().warnUser()) {
            m.getSolver().log().white().printf("Preprocessing reduces the search space by approximately %.2f%%\n", (before - after) * 100. / before);
        }
    }

    /**
     * It sequentially applies Singleton Arc Consistency on bounds of each integer variable.
     * If a value leads to inconsistency, it is removed from the domain of the variable.
     * The method ends when the time limit is reached or when all combination have been checked.
     *
     * @implSpec A first propagation must have been done before calling this method.
     */
    public static void sacBound(Model m, long timeLimitInMS) {
        if (!m.getSolver().getEngine().isInitialized()) {
            try {
                m.getSolver().propagate();
            } catch (ContradictionException e) {
                throw new SolverException("Preprocessing failed");

            }
        }
        if (timeLimitInMS > 0 && m.getSettings().warnUser()) {
            m.getSolver().log().white().printf("Running SACBound step (%dms).\n", timeLimitInMS);
        }
        long tl = System.currentTimeMillis() + timeLimitInMS;
        IntVar[] ivars = m.retrieveIntVars(true);
        long before = Arrays.stream(ivars)
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        Arrays.sort(ivars, (v, w) -> {
            int d = v.getNbProps() - w.getNbProps();
            if (d == 0) {
                d = w.getDomainSize() - v.getDomainSize();
                if (d == 0) {
                    d = v.getId() - w.getId();
                }
            }
            return d;
        });
        boolean hasChanged = true;
        loop:
        while (hasChanged) {
            hasChanged = false;
            for (int i = 0; i < ivars.length; i++) {
                IntVar v = ivars[i];
                int lb = v.getLB();
                int ub = v.getUB();
                int val = lb;
                while (val <= ub) {
                    if (System.currentTimeMillis() > tl) {
                        break loop;
                    }
                    m.getSolver().pushTrail();
                    boolean noSupport = hasNoSupport(m, v, val);
                    m.getSolver().cancelTrail();
                    if (noSupport) {
                        try {
                            // try to permanently remove the value
                            hasChanged |= v.removeValue(val, Cause.Null);
                            m.getSolver().getEngine().propagate();
                            /*if (m.getSettings().warnUser()) {
                                m.getSolver().log().white().printf("Preprocessing removed value %d from %s\n", val, v.getName());
                            }*/
                        } catch (ContradictionException e) {
                            throw new SolverException("Preprocessing failed");
                        }
                    } else break;
                    val = v.getLB();
                }
                while (val >= lb) {
                    if (System.currentTimeMillis() > tl) {
                        break loop;
                    }
                    m.getSolver().pushTrail();
                    boolean noSupport = hasNoSupport(m, v, val);
                    m.getSolver().cancelTrail();
                    if (noSupport) {
                        try {
                            // try to permanently remove the value
                            hasChanged |= v.removeValue(val, Cause.Null);
                            m.getSolver().getEngine().propagate();
                            if (m.getSettings().warnUser()) {
                                m.getSolver().log().white().printf("Preprocessing removed value %d from %s\n", val, v.getName());
                            }
                        } catch (ContradictionException e) {
                            throw new SolverException("Preprocessing failed");
                        }
                    } else break;
                    val = v.getUB();
                }
            }
        }
        long after = Arrays.stream(ivars)
                .mapToLong(VariableUtils::domainCardinality)
                .sum();
        if (before - after > 0 && m.getSettings().warnUser()) {
            m.getSolver().log().white().printf("Preprocessing reduces the search space by approximately %.2f%%\n", (before - after) * 100. / before);
        }
    }

    /**
     * Check if a value is supported by the variable.
     *
     * @param m   the model
     * @param var the variable
     * @param val the value to check
     * @return true if the value is not supported by the variable, false otherwise
     */
    private static boolean hasNoSupport(Model m, IntVar var, int val) {
        try {
            var.instantiateTo(val, Cause.Null);
            m.getSolver().getEngine().propagate();
            return false;
        } catch (ContradictionException e) {
            m.getSolver().getEngine().flush();
            return true;
        }
    }
}
