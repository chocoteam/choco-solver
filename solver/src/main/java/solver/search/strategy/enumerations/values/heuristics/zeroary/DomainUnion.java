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

package solver.search.strategy.enumerations.values.heuristics.zeroary;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.TIntHashSet;import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.IntVar;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 20/01/11
 */
public class DomainUnion extends HeuristicVal {

    TIntHashSet _hs = new TIntHashSet();
    IntVar[] variables;
    int[] values;
    int idx;

    private DomainUnion(Action action) {
        super(action);
    }

    public DomainUnion(IntVar[] variables) {
        super();
        this.variables = variables;
        init();
    }

    public DomainUnion(IntVar[] variables, Action action) {
        super(action);
        this.variables = variables;
        init();
    }

    private void init() {
        for (int i = 0; i < variables.length; i++) {
            IntVar var = this.variables[i];
            int ub = var.getUB();
            for (int val = var.getLB(); val <= ub; val = var.nextValue(val)) {
                _hs.add(val);
            }
        }
        values = _hs.toArray();
        //TODO: DO we need to sort the values?
//        Arrays.sort(values);
        _hs.clear();
        idx = 0;
    }


    @Override
    public boolean hasNext() {
        return idx < values.length;
    }

    @Override
    public int next() {
        return values[idx++];
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("DomainUnion.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        init();
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            DomainUnion duplicata = new DomainUnion(this.action);
            //TODO: share variables and values?
            duplicata.variables = this.variables.clone();
            duplicata.values = this.values.clone();
            duplicata.idx = this.idx;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}
