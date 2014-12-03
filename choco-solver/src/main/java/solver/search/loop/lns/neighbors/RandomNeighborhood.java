/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.BitSet;
import java.util.Random;

/**
 * A Random LNS
 * <p/>
 *
 * @author Charles Prud'homme
 * @since 18/04/13
 */
public class RandomNeighborhood extends ANeighbor {

    protected final int n;
    protected final IntVar[] vars;
    protected final int[] bestSolution;
    private final int[] previous;
    private Random rd;
    private double nbFixedVariables = 0d;
    private int nbCall;
    private int limit;
    private int level;

    protected BitSet fragment;  // index of variable to set unfrozen

    public RandomNeighborhood(Solver aSolver, IntVar[] vars, int level, long seed) {
        super(aSolver);
        this.n = vars.length;
        this.vars = vars.clone();
        this.level = level;

        this.rd = new Random(seed);
        this.bestSolution = new int[n];
        this.previous = new int[n];
        this.fragment = new BitSet(n);
    }

    @Override
    public boolean isSearchComplete() {
        return false;
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < vars.length; i++) {
            previous[i] = bestSolution[i];
            bestSolution[i] = vars[i].getValue();
        }
        nbFixedVariables = 2. * n / 3. + 1;
        nbCall = 0;
        limit = 200; //geo.getNextCutoff(nbCall);
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        nbCall++;
        restrictLess();
        fragment.set(0, n); // all variables are frozen
        for (int i = 0; i < nbFixedVariables - 1 && fragment.cardinality() > 0; i++) {
            int id = selectVariable();
            if (vars[id].contains(bestSolution[id])) {  // to deal with objective variable and related
                impose(id, cause);
            }
            fragment.clear(id);
        }
    }

    protected void impose(int id, ICause cause) throws ContradictionException {
        vars[id].instantiateTo(bestSolution[id], cause);
    }

    protected int selectVariable() {
        int id;
        int cc = rd.nextInt(fragment.cardinality());
        for (id = fragment.nextSetBit(0); id >= 0 && cc > 0; id = fragment.nextSetBit(id + 1)) {
            cc--;
        }
        return id;
    }

    @Override
    public void restrictLess() {
        if (nbCall > limit) {
            limit = nbCall + level;
            nbFixedVariables = rd.nextDouble() * n;
        }
    }
}
