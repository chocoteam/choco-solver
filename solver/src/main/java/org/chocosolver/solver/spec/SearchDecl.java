/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.SearchParams;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.variables.IntVar;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * The search of a {@link ModelSpec}: configures the solver of each instance, once all the steps are replayed.
 * <p>
 * A search declaration is shared by all the instances, possibly configured concurrently: it must be stateless and
 * must only use the objects of the instance, obtained from the {@link Resolver} (never objects of another model,
 * which is checked by assertions when the strategy is set).
 * <p>
 * Search strategies cannot be journalized: they are declared, either from the catalog of this interface
 * (heuristics and identifiers of variables), or as a function of the model or of the {@link Resolver}.
 *
 * @author Charles Prud'homme
 */
@FunctionalInterface
public interface SearchDecl {

    /**
     * Configure the solver of an instance.
     *
     * @param resolver maps the identifiers of the spec to the objects of the instance
     */
    void configure(Resolver resolver);

    /**
     * @return a declaration which applies this one, then <i>next</i>
     */
    default SearchDecl andThen(SearchDecl next) {
        Objects.requireNonNull(next);
        return r -> {
            configure(r);
            next.configure(r);
        };
    }

    /**
     * A declaration based on the model only, e.g. {@code SearchDecl.of(Search::defaultSearch)} or
     * {@code SearchDecl.of(BlackBoxConfigurator.forCOP()::make)}: the model of each instance holds the same
     * variables, groups, hooks and objective as the recorded one.
     *
     * @param configurer configures a model; must be stateless
     */
    static SearchDecl of(Consumer<Model> configurer) {
        Objects.requireNonNull(configurer);
        return r -> configurer.accept(r.model());
    }

    /**
     * An integer search strategy over the variables identified by <i>ids</i>.
     *
     * @param varSel variable selection
     * @param valSel value selection
     * @param ids    identifiers of the decision variables, in order
     */
    static SearchDecl intVarSearch(SearchParams.VarSelConf varSel, SearchParams.ValSelConf valSel, int... ids) {
        Objects.requireNonNull(varSel);
        Objects.requireNonNull(valSel);
        int[] vids = ids.clone();
        return r -> {
            IntVar[] vars = r.intVars(vids);
            r.model().getSolver().setSearch(varSel.make().apply(vars, valSel.make().apply(r.model())));
        };
    }

    /**
     * Add a restart policy.
     *
     * @param conf the restart policy
     */
    static SearchDecl restarts(SearchParams.ResConf conf) {
        Objects.requireNonNull(conf);
        return r -> {
            Solver solver = r.model().getSolver();
            solver.addRestarter(conf.make().apply(solver));
        };
    }

    /**
     * Wrap the current search strategy with last conflict.
     *
     * @param k number of conflicting variables to remember
     */
    static SearchDecl lastConflict(int k) {
        return r -> {
            Solver solver = r.model().getSolver();
            AbstractStrategy<?> current = solver.getSearch();
            if (current == null) {
                throw new SpecException("lastConflict requires a search strategy to wrap");
            }
            solver.setSearch(Search.lastConflict(current, k));
        };
    }
}
