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

import gnu.trove.map.hash.TIntDoubleHashMap;
import org.chocosolver.solver.Identity;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 26/03/2025
 */
public interface Weightable<I extends Identity> {
    /**
     * Return the weight of an identifiable object i
     *
     * @param i an identifiable object
     * @return its weight
     */
    double get(I i);

    /**
     * Set the weight of an identifiable object i
     *
     * @param i      an identifiable object
     * @param weight its weight
     */
    void set(I i, double weight);

    /**
     * Update the weight of an identifiable object i
     *
     * @param i     an identifiable object
     * @param delta the increment
     */
    void inc(I i, double delta);

    /**
     * Clear the weights
     */
    void clear();

    /**
     * Return the top n identifiable objects wrt to their weight
     *
     * @param n number of objects to return
     * @return a list of n identifiable objects
     */
    List<Integer> getTop(int n);

    class Map<I extends Identity> implements Weightable<I> {
        private final TIntDoubleHashMap weights;

        public Map() {
            this.weights = new TIntDoubleHashMap(15, 1.5f, -1, 0.);
        }

        @Override
        public double get(I i) {
            return weights.get(i.getId());
        }

        @Override
        public void set(I i, double weight) {
            weights.put(i.getId(), weight);
        }

        @Override
        public void inc(I i, double delta) {
            weights.adjustOrPutValue(i.getId(), delta, delta);
        }

        @Override
        public void clear() {
            weights.clear();
        }

        @Override
        public List<Integer> getTop(int n) {
            return Arrays.stream(weights.keys())
                    .boxed()
                    .sorted((k1, k2) -> Double.compare(weights.get(k2), weights.get(k1)))
                    .limit(n)
                    .collect(Collectors.toList());
        }
    }

    /**
     * <br/>
     *
     * @author Charles Prud'homme
     * @since 26/03/2025
     */
    class Array<I extends Identity> implements Weightable<I> {
        private double[] weights;

        public Array() {
            this.weights = new double[16];
        }

        private void ensureCapacity(int capacity) {
            if (weights.length < capacity) {
                weights = Arrays.copyOf(weights, capacity);
            }
        }

        @Override
        public double get(I i) {
            ensureCapacity(i.getId() + 1);
            return weights[i.getId()];
        }


        @Override
        public void set(I i, double weight) {
            ensureCapacity(i.getId() + 1);
            weights[i.getId()] = weight;
        }

        @Override
        public void inc(I i, double delta) {
            ensureCapacity(i.getId() + 1);
            weights[i.getId()] += delta;
        }

        @Override
        public void clear() {
            Arrays.fill(weights, 0);
        }

        @Override
        public List<Integer> getTop(int n) {
            return IntStream.range(0, weights.length)
                    .boxed()
                    .sorted(Comparator.comparingDouble(i -> -weights[i]))
                    .limit(n)
                    .collect(Collectors.toList());
        }
    }
}
