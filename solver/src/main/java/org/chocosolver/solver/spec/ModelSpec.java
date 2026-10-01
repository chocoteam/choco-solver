/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Settings;

import java.util.BitSet;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * An immutable description of a model: a {@link Header} and an ordered journal of {@link Step}s.
 * <p>
 * Each call to {@link #instantiate()} builds a new, independent {@link Model}. Distinct instances can be built and
 * solved concurrently, in distinct threads: they only share immutable data (settings, frozen tuples, ...).
 * <p>
 * Replaying the same spec twice produces two models with identical structure (same variables, same constraints,
 * in the same order), hence identical search when the search is deterministic.
 *
 * @param header what must be set on a model before replaying the steps
 * @param steps  the steps, replayed in order
 * @param search the search, applied once the steps are replayed; null for the default search
 * @author Charles Prud'homme
 */
public record ModelSpec(Header header, List<Step> steps, SearchDecl search) {

    /**
     * @param header what must be set on a model before replaying the steps
     * @param steps  the steps, replayed in order
     * @param search the search, applied once the steps are replayed; null for the default search
     */
    public ModelSpec {
        Objects.requireNonNull(header);
        steps = List.copyOf(steps);
        checkIdentifiers(steps);
    }

    /**
     * A spec with the default search.
     */
    public ModelSpec(Header header, List<Step> steps) {
        this(header, steps, null);
    }

    /**
     * @return this spec with another search
     */
    public ModelSpec withSearch(SearchDecl search) {
        return new ModelSpec(header, steps, search);
    }

    /**
     * @return this spec with another header
     */
    public ModelSpec withHeader(Header header) {
        return new ModelSpec(header, steps, search);
    }

    /**
     * @return this spec with other steps
     */
    public ModelSpec withSteps(List<Step> steps) {
        return new ModelSpec(header, steps, search);
    }

    /**
     * @return the spec rewritten by <i>variant</i>
     */
    public ModelSpec with(Variant variant) {
        return variant.apply(this);
    }

    /**
     * Build a new model from a variant of this spec. When several models are built from the same variant, prefer
     * {@code with(variant)} once, then {@link #instantiate()} on the result.
     *
     * @return the mapping from the identifiers to the objects of the new model ({@link Resolver#model()})
     */
    public Resolver instantiate(Variant variant) {
        return with(variant).instantiate();
    }

    /**
     * Record the construction of a model.
     *
     * @param name     name of the model
     * @param settings settings of the model
     * @param builder  builds the model
     * @return the spec of the model
     * @throws SpecException if some operations could not be journalized
     * @see RecordingModel
     */
    public static ModelSpec record(String name, Settings settings, Consumer<Model> builder) {
        RecordingModel model = new RecordingModel(name, settings);
        builder.accept(model);
        return model.snapshot();
    }

    /**
     * Build a new model from this spec.
     *
     * @return the mapping from the identifiers to the objects of the new model ({@link Resolver#model()})
     * @throws SpecException if a step cannot be replayed
     */
    public Resolver instantiate() {
        Resolver resolver = new Replayer(header).replay(steps);
        if (search != null) {
            search.configure(resolver);
        }
        return resolver;
    }

    /**
     * Check that each identifier is defined before being used.
     */
    private static void checkIdentifiers(List<Step> steps) {
        BitSet defined = new BitSet();
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            final int s = i;
            step.forEachUse(id -> {
                if (!defined.get(id)) {
                    throw new SpecException("Step #" + s + " (" + step + ") uses undefined identifier " + id);
                }
            });
            step.forEachDef(defined::set);
        }
    }
}
