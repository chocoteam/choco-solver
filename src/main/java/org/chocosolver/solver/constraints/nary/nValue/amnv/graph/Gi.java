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
package org.chocosolver.solver.constraints.nary.nValue.amnv.graph;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;

/**
 * Intersection Graph
 *
 * @author Jean-Guillaume Fages
 * @since 01/01/2014
 */
public class Gi extends G {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    protected IntVar[] X;

    //***********************************************************************************
    // CONSTRUCTORS
    //***********************************************************************************

    /**
     * Creates the intersection graph of X
     *
     * @param X integer variable
     */
    public Gi(IntVar[] X) {
        super(X[0].getModel(), X.length);
        this.X = X;
    }

    //***********************************************************************************
    // ALGORITHMS
    //***********************************************************************************

    public void build() {
        int n = getNbMaxNodes();
        for (int i = 0; i < n; i++) {
            getNeighOf(i).clear();
        }
        for (int i = 0; i < n; i++) {
            for (int i2 = i + 1; i2 < n; i2++) {
                if (intersect(i, i2)) {
                    addEdge(i, i2);
                }
            }
        }
    }

    public void update() {
        int n = getNbMaxNodes();
        for (int i = 0; i < n; i++) {
            update(i);
        }
    }

    public void update(int i) {
        ISet nei = getNeighOf(i);
        for (int j : nei) {
            if (!intersect(i, j)) {
                removeEdge(i, j);
            }
        }
    }

    protected boolean intersect(int i, int j) {
        IntVar x = X[i];
        IntVar y = X[j];
        if (x.getLB() > y.getUB() || y.getLB() > x.getUB()) {
            return false;
        }
        int ub = x.getUB();
        for (int val = x.getLB(); val <= ub; val = x.nextValue(val)) {
            if (y.contains(val)) {
                return true;
            }
        }
        return false;
    }

}
