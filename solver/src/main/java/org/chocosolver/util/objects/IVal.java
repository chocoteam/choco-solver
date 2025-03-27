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

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 30/10/2018.
 */
public interface IVal {

    double activity(int value);

    void setactivity(int value, double activity);

    void update(int nb_probes);

    void transfer();

    /**
     *
     * <p>
     * Project: choco-solver.
     * @author Charles Prud'homme
     * @since 30/10/2018.
     */
    class MapVal implements IVal {

        private final TIntDoubleHashMap Av;
        private final TIntDoubleHashMap mAv;
        private final int os;  // offset

        public MapVal(int os) {
            this.os = os;
            this.Av = new TIntDoubleHashMap(32, 0.5f, 0, 0);
            this.mAv = new TIntDoubleHashMap(32, 0.5f, 0, 0);
        }

        @Override
        public double activity(int value) {
            return Av.get(value - os);
        }

        @Override
        public void setactivity(int value, double activity) {
            Av.put(value - os, activity);
        }

        @Override
        public void update(int nb_probes) {
            double activity, oldmA, U;
            for (int k : Av.keys()) {
                activity = Av.get(k);
                oldmA = mAv.get(k);
                U = activity - oldmA;
                mAv.adjustValue(k, U / nb_probes);
            }
        }

        @Override
        public void transfer() {
            Av.clear();
            Av.putAll(mAv);
        }
    }

    /**
     * <p>
     * Project: choco-solver.
     *
     * @author Charles Prud'homme
     * @since 30/10/2018.
     */
    class ArrayVal implements IVal {

        private final double[] Av;
        private final double[] mAv;
        private final int size;
        private final int os;  // offset

        public ArrayVal(int size, int os) {
            this.size = size;
            this.os = os;
            this.Av = new double[size];
            this.mAv = new double[size];
        }

        @Override
        public double activity(int value) {
            return Av[value - os];
        }


        @Override
        public void setactivity(int value, double activity) {
            Av[value - os] = activity;
        }

        @Override
        public void update(int nb_probes) {
            double activity, oldmA, U;
            for (int j = 0; j < Av.length; j++) {
                activity = Av[j];
                oldmA = mAv[j];
                U = activity - oldmA;
                mAv[j] += (U / nb_probes);
            }
        }

        @Override
        public void transfer() {
            System.arraycopy(mAv, 0, Av, 0, size);
        }
    }
}
