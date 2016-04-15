/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
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
package org.chocosolver.solver.search.restart;

import org.chocosolver.util.tools.MathUtils;

/**
 * Restart strategy based on:
 * <br/>
 * "Optimal Speedup of Las Vegas Algorithms",
 * M. Luby, A. Sinclair, D. Zuckerman,
 * IPL: Information Processing Letters, 1993, 47, 173-180.
 * <br/>
 *
 * @author Charles Prud'homme, Arnaud Malapert, Hadrien Cambazard
 * @since 13/05/11
 */
public class LubyRestartStrategy extends AbstractRestartStrategy {

    private int geometricalIntFactor;

    private int divFactor;

    public LubyRestartStrategy(int scaleFactor, int geometricalFactor) {
        super(scaleFactor, geometricalFactor);
    }

    @Override
    public final void setGeometricalFactor(double geometricalFactor) {
        checkPositiveValue(geometricalFactor);
        double f = Math.floor(geometricalFactor);
        if (f != geometricalFactor) {
            throw new IllegalArgumentException("Luby geometrical parameter should be an integer");
        }
        super.setGeometricalFactor(geometricalFactor);
        geometricalIntFactor = (int) geometricalFactor;
        divFactor = geometricalIntFactor - 1;
    }

    private static int geometricalSum(int value, int exponent) {
        return (MathUtils.pow(value, exponent) - 1) / (value - 1);
    }


    /**
     * Returns the Las Vegas coefficient corresponding to the i^h calls.
     * @param i number of calls
     * @return the LV coefficient
     */
    private int getLasVegasCoef(int i) {
        //<hca> I round it to PRECISION because of issues between versions of the jvm on mac and pc
        final double log = MathUtils.roundedLog(i * divFactor + 1, geometricalIntFactor);
        final int k = (int) Math.floor(log);
        if (log == k) {
            return MathUtils.pow(geometricalIntFactor, k - 1);
        } else {
            //recursion
            return getLasVegasCoef(i - geometricalSum(geometricalIntFactor, k));
        }
    }

    @Override
    public int getNextCutoff(int nbRestarts) {
        return getLasVegasCoef(nbRestarts + 1) * scaleFactor;
    }


    @Override
    public String toString() {
        return "LUBY(" + scaleFactor + ',' + geometricalFactor + ')';
    }
}
