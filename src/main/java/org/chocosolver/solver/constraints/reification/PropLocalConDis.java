/**
 * Copyright (c) 2016, Ecole des Mines de Nantes
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
package org.chocosolver.solver.constraints.reification;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.propagation.IPropagationEngine;
import org.chocosolver.solver.propagation.NoPropagationEngine;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.solver.variables.ranges.IntIterableRangeSet;
import org.chocosolver.solver.variables.ranges.IntIterableSetUtils;
import org.chocosolver.util.ESat;

import java.util.BitSet;

/**
 * A propagator for constructive disjunction, with local deductions.
 * The propagator propagates each constraint, in sequence, and maintains the domain union of each modified variable.
 * This propagator declares no propagation engine temporarily.
 * <p>
 * <p>
 * Project: choco.
 * <p>
 *
 * @author Charles Prud'homme
 * @author Jean-Guillaume Fages
 * @since 25/01/2016.
 */
public class PropLocalConDis extends Propagator<IntVar> {

    /**
     * Constraints in disjunction
     */
    Propagator<IntVar>[][] propagators;
    /**
     * Index of the last propagators not wrong
     */
    IStateInt idx;
    /**
     * Store cardinality of variables before a try
     */
    final int[] cardinalities;
    /**
     * Store the union of domain of modified variables
     */
    TIntObjectHashMap<IntIterableRangeSet> domains;
    /**
     * Cardinality of domains (external to limit GC)
     */
    BitSet toUnion;

    /**
     * A propagator to deal with constructive disjunction
     * @param propagators matrix of propagators, columns are in disjunction
     */
    public PropLocalConDis(IntVar[] vars, Propagator[][] propagators) {
        super(vars, PropagatorPriority.VERY_SLOW, false);
        this.propagators = propagators;
        cardinalities = new int[vars.length];
        domains = new TIntObjectHashMap<>();
        toUnion = new BitSet();
        idx = model.getEnvironment().makeInt(propagators.length-1);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        IPropagationEngine eng = model.getSolver().getEngine();
        try {
            do {
                // a fix point needs to be reached
                for (int i = 0; i < vars.length; i++) {
                    cardinalities[i] = vars[i].getDomainSize();
                    if (domains.get(i) != null) {
                        domains.get(i).clear();
                    }
                }
                toUnion.clear();
                model.getSolver().setEngine(NoPropagationEngine.SINGLETON);
                for (int i = idx.get(); i >= 0; i--) {
                    if (propagate(propagators[i], i)) {
                        int last = idx.add(-1) + 1;
                        if (last > i) {
                            Propagator<IntVar>[] tmp = propagators[i];
                            propagators[i] = propagators[last];
                            propagators[last] = tmp;
                        }
                    }else if(toUnion.cardinality() == 0){
                        break;
                    }
                }
                model.getSolver().setEngine(eng);
            } while (applyDeductions());
        } finally {
            model.getSolver().setEngine(eng);
        }
    }

    /**
     * Based on deductions made before, filters domain of variables.
     *
     * @throws ContradictionException domain wiper out
     */
    private boolean applyDeductions() throws ContradictionException {
        boolean change = false;
        int id = idx.get();
        if(id < 0){
            this.fails();
        }
        // push domains
        for (int p = toUnion.nextSetBit(0); p >= 0; p = toUnion.nextSetBit(p + 1)) {
            change |= vars[p].removeAllValuesBut(domains.get(p), this);
        }
        if(id == 0){
            setPassive();
            for (int p = 0; p < propagators[0].length; p++) {
                assert (propagators[0][p].isReifiedAndSilent());
                propagators[0][p].setReifiedTrue();
                //todo: solver.getEventObserver().activePropagator(bool, propagators[p]);
                propagators[0][p].propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
                model.getSolver().getEngine().onPropagatorExecution(propagators[0][p]);
            }
            change = false;
        }
        return change;
    }

    /**
     * Force boolean variable <i>b</i> to <<tt>true</tt> and collect modified domains.
     *
     * @param props    propagator to execute
     * @return <tt>false</tt> if the propagation fails
     */
    private boolean propagate(Propagator<IntVar>[] props, int cidx) {
        boolean fails = false;
        // make a backup world
        model.getEnvironment().worldPush();
//        System.out.printf("%sTry %s for %s\n", pad("", solver.getEnvironment().getWorldIndex(), "."), vars[b].getName(), this);
        try {
            for(int i  = 0; i < props.length; i++) {
                props[i].setReifiedTrue();
                props[i].propagate(PropagatorEventType.CUSTOM_PROPAGATION.getMask());
            }
            // find modified variables and copy their domain
            readDomains();
        } catch (ContradictionException cex) {
            // if failure occurs, then we consider all domains as empty
            // and union is maintained as is
            fails = true;
        }
        // restore backup world
        model.getEnvironment().worldPop();
        return fails;
    }

    /**
     * Find modified domains and compute unions from one propagation to the other.
     */
    @SuppressWarnings("Duplicates")
    private void readDomains() {
        if (toUnion.cardinality() == 0) {
            for (int i = 0; i < vars.length; i++) {
                if (cardinalities[i] > vars[i].getDomainSize()) {
                    IntIterableRangeSet rs = domains.get(i);
                    if (rs == null) {
                        rs = new IntIterableRangeSet();
                        domains.put(i, rs);
                    }
                    IntIterableSetUtils.copyIn(vars[i], rs);
                    toUnion.set(i);
                }
            }
        } else { // only iterate over previously modified variables
            for (int p = toUnion.nextSetBit(0); p >= 0; p = toUnion.nextSetBit(p + 1)) {
                // check if domain has changed
                if (cardinalities[p] > vars[p].getDomainSize()) {
                    IntIterableSetUtils.union(domains.get(p), vars[p]);
                    if (domains.get(p).size() == cardinalities[p]) {
                        toUnion.clear(p);
                    }
                } else {
                    toUnion.clear(p);
                }
            }
        }
    }

    @Override
    public ESat isEntailed() {
        /*int zero = 0, one = 0;
        for (int i = 0; i < propagators.length; i++) {
            int _zero = 0, _one = 0;
            for (int j = 0; j < propagators[i].length; j++) {
                switch (propagators[i][j].isEntailed()) {
                    case TRUE:
                        _one++;
                        break;
                    case FALSE:
                        _zero++;
                        break;
                    case UNDEFINED:
                        break;
                }
            }
            if (_zero == propagators[i].length) zero++;
            if (_one > 0) one++;
        }
        if (zero == propagators.length) {
            return ESat.FALSE;
        } else if (one > 0) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;*/
        return ESat.TRUE;
    }

    @Override
    public String toString() {
        return "ConstructiveDisjunction";
    }
}
