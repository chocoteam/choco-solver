/**
 * This file is part of cutoffseq, https://github.com/chocoteam/cutoffseq
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.cutoffseq;

/**
 * A Luby cutoff strategy.
 *
 * Based on:
 * <br/>
 * "Optimal Speedup of Las Vegas Algorithms",
 * M. Luby, A. Sinclair, D. Zuckerman,
 * IPL: Information Processing Letters, 1993, 47, 173-180.
 * <br/>
 *
 * Example, with <i>s</i>=1:
 * 1, 1, 2, 1, 1, 2, 4, 1, 1, 2, 1, 1, 2, 4, 8, 1, 1, 2, ...
 *
 * @author Charles Prud'homme, Arnaud Malapert, Hadrien Cambazard
 * @since 13/05/11
 */
public final class LubyCutoffStrategy extends AbstractCutoffStrategy {

    /**
     * Current cutoff, starts at 1 and will be multiplied by {@link #scaleFactor}
     * anytime {@link #getNextCutoff()} is called.
     */
    private int un = 1;
    /**
     * Current limit, which set {@link #un} to 1 when reached.
     */
    private int vn = 1;

    /**
     * A Luby cutoff strategy.
     * @param s scale factor
     */
    @SuppressWarnings("WeakerAccess")
    public LubyCutoffStrategy(long s) {
        super(s);
    }

    /**
     * From SAT 2012: computing Luby values the way presented by Donald Knuth 
     * in his invited talk at the SAT 2012 conference.
     * </br> 
     * Credits: sat4j.
     */
    @Override
    public long getNextCutoff() {
        final long cutoff = scaleFactor * this.vn;
        if ((this.un & -this.un) == this.vn) {
            this.un = this.un + 1;
            this.vn = 1;
        } else {
            this.vn = this.vn << 1;
        }
        return cutoff;
    }

    @Override
    public String toString() {
        return "LUBY(s=" + scaleFactor + ",log2)";
    }
}
