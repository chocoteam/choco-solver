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

import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.domain.IIntDomain;

public class Random extends HeuristicVal {
    long seed;
    IIntDomain domain;
    java.util.Random generator;
    TIntHashSet elts;

    private Random(Action action) {
        super(action);
    }

    public Random(IIntDomain domain) {
        this(domain, System.currentTimeMillis());
    }

    public Random(IIntDomain domain, Action action) {
        this(domain, System.currentTimeMillis(), action);
    }

    Random(IIntDomain domain, long seed) {
        super();
        this.seed = seed;
        this.domain = domain;
        this.generator = new java.util.Random(seed);
        elts = new TIntHashSet();
    }

    Random(IIntDomain domain, long seed, Action action) {
        super(action);
        this.seed = seed;
        this.domain = domain;
        this.generator = new java.util.Random(seed);
        elts = new TIntHashSet();
    }

    public boolean hasNext() {
        return domain.getSize() > elts.size();
    }

    public int next() {
        int n = generator.nextInt(domain.getSize() - elts.size());
        int j = domain.getLB();
        for (int i = 0; i < n;) {
            if (!elts.contains(j)) {
                i++;
            }
            j = domain.nextValue(j);
        }
        elts.add(j);
        return j;
    }

    public void remove() {
        throw new UnsupportedOperationException("Random.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        this.generator = new java.util.Random(seed);
        elts.clear();
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            Random duplicata = new Random(this.action);
            duplicata.seed = this.seed;
            //BEWARE: Cannot clone() generator...!! INCOHERENCE is at our door!
            duplicata.generator = this.generator;
            duplicata.elts = this.elts;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}
