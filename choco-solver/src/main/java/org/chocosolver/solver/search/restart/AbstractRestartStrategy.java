/**
 * Copyright (c) 2014,
 *       Charles Prud'homme (TASC, INRIA Rennes, LINA CNRS UMR 6241),
 *       Jean-Guillaume Fages (COSLING S.A.S.).
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the <organization> nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
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

/**
 * <br/>
 *
 * @author Charles Prud'homme, , Arnaud Malapert
 * @since 13/05/11
 */
public abstract class AbstractRestartStrategy implements IRestartStrategy {

    private final String name;

    protected int scaleFactor = 1;

    protected double geometricalFactor = 1;

    protected AbstractRestartStrategy(String name, int scaleFactor, double geometricalFactor) {
        this.name = name;
        setScaleFactor(scaleFactor);
        setGeometricalFactor(geometricalFactor);
    }


    protected static void checkPositiveValue(double value) {
        if (value <= 0) {
            throw new IllegalArgumentException("arguments should be strictly positive.");
        }
    }

    @Override
    public double getGeometricalFactor() {
        return geometricalFactor;
    }

    @Override
    public final String getName() {
        return name;
    }


    @Override
    public final int getScaleFactor() {
        return scaleFactor;
    }

    @Override
    public void setGeometricalFactor(double geometricalFactor) {
        checkPositiveValue(geometricalFactor);
        this.geometricalFactor = geometricalFactor;

    }

    @Override
    public final void setScaleFactor(int scaleFactor) {
        checkPositiveValue(scaleFactor);
        this.scaleFactor = scaleFactor;
    }

    @Override
    public String toString() {
        return getName() + '(' + getScaleFactor() + ',' + getGeometricalFactor() + ')';
    }

    public int[] getSequenceExample(int length) {
        int[] res = new int[length];
        for (int i = 0; i < res.length; i++) {
            res[i] = getNextCutoff(i);
        }
        return res;
    }

}
