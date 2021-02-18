/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser;

import org.chocosolver.solver.Solver;
import org.chocosolver.solver.search.strategy.Search;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 16/06/2020
 */
public class ParserParameters {
    public static class ResConf {
        final Search.Restarts pol;
        final int cutoff;
        final int offset;
        final double geo;

        public ResConf(Search.Restarts pol, int cutoff, double geo, int offset) {
            this.pol = pol;
            this.cutoff = cutoff;
            this.offset = offset;
            this.geo = geo;
        }

        public ResConf(Search.Restarts pol, int cutoff, int offset) {
            this(pol, cutoff, 1d, offset);
        }

        public void declare(Solver solver) {
            pol.declare(solver, cutoff, geo, offset);
        }
    }

    public static class LimConf {
        final long time; // in ms
        final int sols;
        final int runs;

        public LimConf(long time, int sols, int runs) {
            this.time = time;
            this.sols = sols;
            this.runs = runs;
        }
    }
}
