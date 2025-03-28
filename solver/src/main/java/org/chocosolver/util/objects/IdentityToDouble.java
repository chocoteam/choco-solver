/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

import org.chocosolver.solver.Identity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Define a way to store and manipulate weights associated to identifiable objects.
 * <br/>
 *
 * @param <I> the type of identifiable objects
 * @author Charles Prud'homme
 * @implNote The values are stored in an array, indexed by the id of the identifiable object.
 * @since 26/03/2025
 */
public class IdentityToDouble<I extends Identity> {

    // the weights
    private double[] values;

    /**
     * Create a structure to store weights associated with identifiable objects
     */
    public IdentityToDouble() {
        this.values = new double[16];
    }

    /**
     * Ensure that the internal array has a capacity of at least {@code capacity}
     *
     * @param capacity the capacity to ensure
     */
    private void ensureCapacity(int capacity) {
        if (values.length < capacity) {
            values = Arrays.copyOf(values, capacity);
        }
    }

    /**
     * Return the weight associated with {@code i}
     *
     * @param i an identifiable object
     * @return the weight associated with {@code i}
     */
    public double get(I i) {
        ensureCapacity(i.getId() + 1);
        return values[i.getId()];
    }

    /**
     * Set the weight associated with {@code i}
     *
     * @param i      an identifiable object
     * @param weight the weight to set
     */
    public void set(I i, double weight) {
        ensureCapacity(i.getId() + 1);
        values[i.getId()] = weight;
    }

    /**
     * Increase the weight associated with {@code i} by {@code delta}
     *
     * @param i     an identifiable object
     * @param delta the value to add
     */
    public void inc(I i, double delta) {
        ensureCapacity(i.getId() + 1);
        values[i.getId()] += delta;
    }

    /**
     * Clear all weights
     */
    public void clear() {
        Arrays.fill(values, 0);
    }

    /**
     * Return the top n identifiable objects wrt to their weight
     *
     * @param n number of objects to return
     * @return a list of n identifiable objects
     */
    public List<Integer> getTop(int n) {
        return IntStream.range(0, values.length)
                .boxed()
                .sorted(Comparator.comparingDouble(i -> -values[i]))
                .limit(n)
                .collect(Collectors.toList());
    }
}
