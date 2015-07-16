/*
 * Copyright (c) 1999-2015, Ecole des Mines de Nantes
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Ecole des Mines de Nantes nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.search.loop.lns.neighbors;

import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.Random;

/**
 * Created by cprudhom on 08/07/15.
 * Project: choco.
 */
public class SubstructureNeighborhood extends ANeighbor {

    int seql;
    IntVar[] variables;
    int[] solution, _solution;
    int[][] combs;
    Random rnd;

    public SubstructureNeighborhood(IntVar[] vars, long seed) {
        super(vars[0].getSolver());
        this.seql = vars.length;
        this.variables = vars.clone();
        this.solution = new int[seql];
        this._solution = new int[seql];
        this.rnd = new Random(seed);
        combs = new int[seql / 2][2];
        int k = 0;
        for (int i = 2; i < Math.sqrt(seql); i++) {
            if (seql % i == 0) {
                combs[k][0] = i;
                combs[k++][1] = seql / i;
            }
        }
        combs = Arrays.copyOf(combs, k);
    }

    @Override
    public void recordSolution() {
        for (int i = 0; i < seql; i++) {
            solution[i] = variables[i].getValue();
        }
    }

    @Override
    public void fixSomeVariables(ICause cause) throws ContradictionException {
        System.arraycopy(solution, 0, _solution, 0, seql);
        int c = rnd.nextInt(combs.length);
        int row, col;
        if (rnd.nextBoolean()) {
            col = combs[c][0];
            row = combs[c][1];
        } else {
            col = combs[c][1];
            row = combs[c][0];
        }
        // we want to relax 3 rows or columns
        int max = Math.max(row, col);
        if (row > col) {
            // we relax at least 2 rows
            int rel = 2 + rnd.nextInt(row - 2);
            int tries = row;
            while (tries > 0 && rel > 0) {
                int i = rnd.nextInt(row);
                if (_solution[i * col] != Integer.MAX_VALUE) {
                    int rnk = (i+1) * col;
                    for (int j = i * col; j < rnk; j++) {
                        _solution[j] = Integer.MAX_VALUE;
                    }
                    rel--;
                }
                tries--;
            }
        } else {
            // we relax at least 2 cols
            int rel = 2 + rnd.nextInt(col - 2);
            int tries = col;
            while (tries > 0 && rel > 0) {
                int i = rnd.nextInt(col);
                if (_solution[i] != Integer.MAX_VALUE) {
                    for (int j = 0; j < row; j++) {
                        _solution[i + j * col] = Integer.MAX_VALUE;
                    }
                    rel--;
                }
                tries--;
            }
        }
        // then freeze all non selected cells
        for (int i = 0; i < seql; i++) {
            if (_solution[i] != Integer.MAX_VALUE) {
                variables[i].isInstantiatedTo(_solution[i]);
            }
        }
    }

    @Override
    public void restrictLess() {

    }

    @Override
    public boolean isSearchComplete() {
        return false;
    }
}
