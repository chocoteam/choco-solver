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

package solver.search.strategy.enumerations.values.heuristics.nary;

import gnu.trove.THashMap;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;

public class ZipN extends NaryHeuristicVal<HeuristicVal> {

    int current = 0;

    private ZipN(Action action) {
        super(action);
    }

    public ZipN(HeuristicVal[] subs) {
        super(subs);
    }

    public ZipN(HeuristicVal[] subs, Action action) {
        super(subs, action);
    }

    public ZipN(HeuristicVal left, HeuristicVal right) {
        this(new HeuristicVal[]{left, right});
    }

    public ZipN(HeuristicVal left, HeuristicVal right, Action action) {
        this(new HeuristicVal[]{left, right}, action);
    }


    public boolean hasNext() {
        for (HeuristicVal h : subs) {
            if (h.hasNext()) {
                return true;
            }
        }
        return false;
    }

    public int next() {
        while (!subs[current].hasNext()) {
            current = (current + 1) % subs.length;
        }
        int result = subs[current].next();
        current = (current + 1) % subs.length;
        return result;
    }

    public void remove() {
        throw new UnsupportedOperationException("ZipN.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        current = 0;
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            ZipN duplicata = new ZipN(this.action);
            duplicata.current = current;
            duplicata.subs = new HeuristicVal[subs.length];
            for (int i = 0; i < subs.length; i++) {
                duplicata.subs[i] = this.subs[i].duplicate(map);
            }
            map.put(this, duplicata);
            return duplicata;
        }
    }
}

