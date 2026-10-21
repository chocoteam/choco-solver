/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Settings;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.variables.IntVar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;

/**
 * A variant of a {@link ModelSpec}: a rewriting of the spec, applied once, whose result is instantiated by as many
 * workers as needed (see {@link ModelSpec#with(Variant)}).
 * <p>
 * A variant may change:
 * <ul>
 *     <li>the header: {@link #seed(long)}, {@link #settings(Settings)};</li>
 *     <li>the calls to the factories, e.g. the consistency of {@code allDifferent} ({@link #allDifferent(String)})
 *     or the algorithm of {@code table} ({@link #tableAlgorithm(String)}), or any call ({@link #mapCalls});</li>
 *     <li>the constraints: redundant constraints or symmetry breaking ({@link #extend(Consumer)}), removals
 *     ({@link #unpostIf(Predicate)});</li>
 *     <li>the search ({@link #search(SearchDecl)}).</li>
 * </ul>
 * Variants compose with {@link #andThen(Variant)}.
 *
 * @author Charles Prud'homme
 */
@FunctionalInterface
public interface Variant {

    /**
     * The variant which changes nothing: a carbon copy.
     */
    Variant IDENTITY = spec -> spec;

    /**
     * @param spec a spec
     * @return the rewritten spec
     */
    ModelSpec apply(ModelSpec spec);

    /**
     * @return a variant which applies this one, then <i>next</i>
     */
    default Variant andThen(Variant next) {
        Objects.requireNonNull(next);
        return spec -> next.apply(apply(spec));
    }

    //***********************************************************************************
    // HEADER AND SEARCH
    //***********************************************************************************

    /**
     * Change the seed: propagators drawing random numbers and randomized search strategies behave differently.
     * Note that it may also change the encoding of expressions which are sampled ({@code ReExpression#embody}).
     * The seeds set during the construction ({@link Step.Seed}) are removed, otherwise they would override this one.
     */
    static Variant seed(long seed) {
        return spec -> spec.withHeader(spec.header().withSeed(seed))
                .withSteps(spec.steps().stream().filter(s -> !(s instanceof Step.Seed)).toList());
    }

    /**
     * Change the settings (e.g., enable views or LCG), which may change the way constraints are built.
     */
    static Variant settings(Settings settings) {
        Objects.requireNonNull(settings);
        return spec -> spec.withHeader(spec.header().withSettings(settings));
    }

    /**
     * Change the search.
     */
    static Variant search(SearchDecl search) {
        return spec -> spec.withSearch(search);
    }

    //***********************************************************************************
    // CALLS
    //***********************************************************************************

    /**
     * Rewrite each call to a factory method.
     *
     * @param rewriter returns the call to replay instead (possibly the same), which must define the same
     *                 identifiers
     */
    static Variant mapCalls(UnaryOperator<Step.Call> rewriter) {
        Objects.requireNonNull(rewriter);
        return spec -> {
            List<Step> steps = new ArrayList<>(spec.steps().size());
            for (Step s : spec.steps()) {
                steps.add(s instanceof Step.Call c ? rewriter.apply(c) : s);
            }
            return spec.withSteps(steps);
        };
    }

    /**
     * Set the consistency of every {@code allDifferent} constraint.
     *
     * @param consistency among "DEFAULT", "AC", "AC_REGIN", "AC_ZHANG", "BC", "FC", "NEQS"
     */
    static Variant allDifferent(String consistency) {
        return withOption("allDifferent", consistency, IntVar[].class);
    }

    /**
     * Set the algorithm of every table constraint of arity greater than 2.
     *
     * @param algo e.g. "CT+", "STR2+", "GAC3rm", "MDD+", "FC" (see {@code IIntConstraintFactory#table})
     */
    static Variant tableAlgorithm(String algo) {
        return withOption("table", algo, IntVar[].class, Tuples.class);
    }

    /**
     * Set the algorithm of every binary table constraint.
     *
     * @param algo e.g. "CT+", "STR2+", "AC3rm", "AC2001", "FC" (see {@code IIntConstraintFactory#table})
     */
    static Variant binaryTableAlgorithm(String algo) {
        return withOption("table", algo, IntVar.class, IntVar.class, Tuples.class);
    }

    /**
     * Rewrite the calls to a factory method which exists with and without a trailing {@code String} option (e.g.
     * {@code allDifferent(IntVar[])} and {@code allDifferent(IntVar[], String)}) so that they all use
     * <i>option</i>.
     */
    private static Variant withOption(String name, String option, Class<?>... base) {
        Objects.requireNonNull(option);
        FactoryMethod without = FactoryMethod.of(name, base);
        Class<?>[] withTypes = Arrays.copyOf(base, base.length + 1);
        withTypes[base.length] = String.class;
        FactoryMethod with = FactoryMethod.of(name, withTypes);
        Arg opt = new Arg.Const(option);
        return mapCalls(c -> {
            boolean hasOption = c.method().equals(with);
            if (!hasOption && !c.method().equals(without)) {
                return c;
            }
            Arg[] args = c.rawArgs();
            Arg[] a = Arrays.copyOf(args, hasOption ? args.length : args.length + 1);
            a[a.length - 1] = opt;
            return new Step.Call(c.rawOut(), with, a);
        });
    }

    //***********************************************************************************
    // CONSTRAINTS
    //***********************************************************************************

    /**
     * Add steps at the end of the spec, e.g. redundant constraints or symmetry breaking constraints.
     * The builder knows the identifiers of the spec (see {@link Recorder#idsOf} to get those of the variables of a
     * recorded model).
     */
    static Variant extend(Consumer<SpecBuilder> extension) {
        Objects.requireNonNull(extension);
        return spec -> {
            SpecBuilder b = SpecBuilder.from(spec);
            extension.accept(b);
            return b.build();
        };
    }

    /**
     * Do not post the constraints built by the calls which satisfy <i>filter</i> (the calls are still replayed,
     * with their side effects, e.g. intermediate variables).
     */
    static Variant unpostIf(Predicate<Step.Call> filter) {
        Objects.requireNonNull(filter);
        return spec -> {
            BitSet removed = new BitSet();
            for (Step s : spec.steps()) {
                if (s instanceof Step.Call c && filter.test(c)) {
                    s.forEachDef(removed::set);
                }
            }
            List<Step> steps = new ArrayList<>(spec.steps().size());
            for (Step s : spec.steps()) {
                // a constraint no longer posted cannot be unposted either
                if (s instanceof Step.Post p) {
                    int[] kept = Arrays.stream(p.rawCstrs()).filter(c -> !removed.get(c)).toArray();
                    if (kept.length > 0) {
                        steps.add(new Step.Post(kept));
                    }
                } else if (s instanceof Step.Unpost u) {
                    int[] kept = Arrays.stream(u.rawCstrs()).filter(c -> !removed.get(c)).toArray();
                    if (kept.length > 0) {
                        steps.add(new Step.Unpost(kept));
                    }
                } else {
                    steps.add(s);
                }
            }
            return spec.withSteps(steps);
        };
    }
}
