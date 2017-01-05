/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
    public LubyCutoffStrategy(int s) {
        super(s);
    }

    /**
     * From SAT 2012: computing Luby values the way presented by Donald Knuth 
     * in his invited talk at the SAT 2012 conference.
     * </br> 
     * Credits: sat4j.
     */
    @Override
    public int getNextCutoff() {
        final int cutoff = scaleFactor * this.vn;
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
