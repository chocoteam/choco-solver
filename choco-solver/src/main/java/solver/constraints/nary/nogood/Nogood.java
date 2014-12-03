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
package org.chocosolver.solver.constraints.nary.nogood;

import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;

/**
 * A class to define a Nogood of size > 1.
 * <p/>
 * Made of a list of variables, a list of values and an int.
 * {vars, values} matches positive decisions.
 * A positive decision d_i is vars_i=values_i.
 * <p/>
 * Related to "Nogood Recording from Restarts", C. Lecoutre et al.
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/06/13
 */
public class Nogood implements INogood {

    final IntVar[] vars;
    final int[] wl;
    final int nbvars;
    final int[] values;
    int idxInStore;


    public Nogood(IntVar[] vars, int[] values) {
        this.values = values;
        this.vars = vars;
        this.nbvars = vars.length;
        this.wl = new int[Math.max(nbvars, 2)];
        for (int i = 0; i < nbvars; i++) {
            wl[i] = i;
        }
    }

    public void setIdx(int idx) {
        this.idxInStore = idx;
    }

    public int getIdx() {
        return this.idxInStore;
    }

    public boolean findLiteral(int start) {
        for (int i = start; i < nbvars; i++) {
            int k = wl[i];
            if (vars[k].contains(values[k])) {
                if (!vars[k].isInstantiated()) {
                    int lwl = wl[start];
                    wl[start] = wl[i];
                    wl[i] = lwl;
                    break;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public int propagate(PropNogoodStore pngs) throws ContradictionException {
        if (!findLiteral(0)) {
//            pngs.silent(this);
            return -1;
        }
        if (vars[wl[0]].isInstantiatedTo(values[wl[0]])) {
            pngs.contradiction(null, "Inconsistent");
        }
        findLiteral(1);
        if (vars[wl[1]].isInstantiatedTo(values[wl[1]])) {
            int k = wl[0];
            if (vars[k].removeValue(values[k], pngs)) {
                return vars[k].isInstantiated() ? k : -1;
            }
            pngs.watch(vars[k], this, k);
        } else {
            pngs.watch(vars[wl[0]], this, wl[0]);
            pngs.watch(vars[wl[1]], this, wl[1]);
        }
        return -1;
    }

    public int awakeOnInst(int idx, PropNogoodStore pngs) throws ContradictionException {
        assert vars[idx].isInstantiated();
        if (!vars[idx].contains(values[idx])) {
//            pngs.silent(this);
            return -1;
        }
        if (wl[0] == idx) {
            wl[0] = wl[1];
            wl[1] = idx;
        }

        if (!vars[wl[0]].contains(values[wl[0]])) {
//            pngs.silent(this);
            return -1;
        }

        int k;
        for (int i = 2; i < wl.length; i++) {
            k = wl[i];
            if (vars[k].contains(values[k])) {
                if (!vars[k].isInstantiated()) {
                    wl[1] = wl[i];
                    wl[i] = idx;
                    pngs.unwatch(vars[idx], this);
                    pngs.watch(vars[wl[1]], this, wl[1]);
                    return -99;
                }
            } else {
//                pngs.silent(this);
                return -1;
            }
        }
        // unit nogood
        if (vars[wl[0]].removeValue(values[wl[0]], pngs)) {
//            pngs.silent(this);
            return vars[wl[0]].isInstantiated() ? wl[0] : -1;
        }
        return -1;
    }

    @Override
    public boolean isUnit() {
        return false;
    }

    public ESat isEntailed() {
        int c = 0;
        for (int i = 0; i < vars.length; i++) {
            if (vars[i].contains(values[i])) {
                if (vars[i].isInstantiated()) {
                    c++;
                }
            } else {
                return ESat.TRUE;
            }
        }
        return c == vars.length ? ESat.FALSE : ESat.UNDEFINED;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nbvars; i++) {
            sb.append(vars[i].getName()).append("==").append(values[i]).append(',');
        }
        return sb.toString();
    }

    @Override
    public int size() {
        return vars.length;
    }

    @Override
    public IntVar getVar(int i) {
        return vars[i];
    }

    @Override
    public int getVal(int i) {
        return values[i];
    }
}
