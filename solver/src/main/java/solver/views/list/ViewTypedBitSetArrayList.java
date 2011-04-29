/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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
package solver.views.list;

import choco.kernel.memory.IEnvironment;
import choco.kernel.memory.IStateBitSet;
import solver.ICause;
import solver.constraints.propagators.Propagator;
import solver.variables.EventType;
import solver.variables.IntVar;
import solver.variables.domain.delta.IDelta;
import solver.views.IView;

import java.util.Arrays;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 23/02/11
 */
public final class ViewTypedBitSetArrayList<V extends IView> implements IViewList<V> {

    protected static int[] INDEX;

    static {
        INDEX = new int[(1 << 4) + 1];
        Arrays.fill(INDEX, -1);
        for (int j = 1; j < 5; j++) {
            INDEX[1 << j] = j - 1;
        }
    }

    protected V[] views;
    protected IStateBitSet[] typedIdx;

    protected ViewTypedBitSetArrayList(IEnvironment env) {
        views = (V[]) new IView[0];
        typedIdx = new IStateBitSet[4];
        for (int i = 0; i < 4; i++) {
            typedIdx[i] = env.makeBitSet(64);
        }
    }

    @Override
    public void setPassive(V view) {
        int idx = view.getIdxInVar();
        for (int j = 0; j < 4; j++) {
            typedIdx[j].set(idx, false);
        }
    }

    @Override
    public void addView(V view) {
        V[] tmp = views;
        int size = tmp.length;
        views = (V[]) new IView[size + 1];
        System.arraycopy(tmp, 0, views, 0, size);
        views[size] = view;
        view.setIdxInVar(size);

        int mask = view.getMask();
        for (int j = 0; j < 4; j++) {
            if ((mask & (1 << (j + 1))) != 0) {
                typedIdx[j].set(size, true);
            }
        }
    }

    @Override
    public void deleteView(IView view) {
        int i = 0;
        for (; i < views.length && views[i] != view; i++) {
        }
        if (i == views.length) return;
        //remove views
        V[] tmp = views;
        views = (V[]) new IView[tmp.length - 1];
        System.arraycopy(tmp, 0, views, 0, i);
        System.arraycopy(tmp, i + 1, views, i, tmp.length - i - 1);
        for (int j = i; j < views.length; j++) {
            views[j].setIdxInVar(j);
        }
        // remove indexes:
        for (int j = 0; j < 4; j++) {
            typedIdx[j].set(i, false);
        }
    }

    @Override
    public int size() {
        return views.length;
    }

    @Override
    public int cardinality() {
        int cpt = 0;
        for (int i = 0; i < views.length; i++) {
            if (views[i].getPropagator().isActive()) {
                cpt++;
            }
        }
        return cpt;
    }

    @Override
    public void notifyButCause(ICause cause, EventType event, IDelta delta) {
        IView view;
        int mask = INDEX[event.mask];
        IStateBitSet _indices = typedIdx[mask];
        for (int i = _indices.nextSetBit(0); i >= 0; i = _indices.nextSetBit(i + 1)) {
            view = views[i];
            Propagator<IntVar> o = view.getPropagator();
            if (o != cause) {
                view.update(event);
            }
        }

    }
}
