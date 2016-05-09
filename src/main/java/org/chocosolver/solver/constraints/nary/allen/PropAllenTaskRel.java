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
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;

import java.util.Arrays;
import java.util.BitSet;

import static org.chocosolver.solver.constraints.nary.allen.Allen.Relation.*;

/**
 * A propagator that filters lower bounds of tasks wrt to a given set of relations and intervals.
 * Created by cprudhom on 17/02/15.
 * Project: allen.
 *
 * @author Alban Derrien, Thierry Petit, Charles Prud'homme
 */
public class PropAllenTaskRel extends Propagator<IntVar> {

    public static boolean LINEAR = true;

    IntVar[] TSTA, TDUR, TEND; // array representing task variables
    protected int TS;   // nb tasks

    protected int[] ISTA, IEND; // array  representing interval values
    protected int IS;   // nb intervals

    protected BitSet modifiedTasks;

    protected int RCARD = 0;  // BEWARE declaredRelation can be updated (f.ex. relPI), so cardinality is not OK

    final static Allen.Relation[] relations = {
            P, M, O, S, D, F, EQ, PI, MI, OI, SI, DI, FI, CO
    };

    protected final ISweep[] sweeps;

    public PropAllenTaskRel(IntVar[] vars, int[] fixed, String[] rel) {
        this(vars, fixed, Allen.buildRelationsForMin(rel), Allen.buildRelationsForMax(rel));
    }

    protected PropAllenTaskRel(IntVar[] vars, int[] fixed, BitSet minrel, BitSet maxrel) {
        super(vars, PropagatorPriority.LINEAR, true);
        // relations
        this.RCARD = minrel.cardinality();
        // intervals
        this.ISTA = Arrays.copyOfRange(fixed, 0, fixed.length / 2);
        this.IEND = Arrays.copyOfRange(fixed, fixed.length / 2, fixed.length);
        this.IS = ISTA.length;
        // tasks
        this.TSTA = Arrays.copyOfRange(vars, 0, vars.length / 3);
        this.TS = TSTA.length;
        this.TDUR = Arrays.copyOfRange(vars, vars.length / 3, 2 * (vars.length / 3));
        this.TEND = Arrays.copyOfRange(vars, 2 * (vars.length / 3), vars.length);

        this.modifiedTasks = new BitSet(TS - 1);
        sweeps = new ISweep[2];
        sweeps[0] = new SweepMin(this, minrel);
        sweeps[0].setModifiedTasks(this.modifiedTasks);
        sweeps[1] = new SweepMax(this, maxrel);
        sweeps[1].setModifiedTasks(this.modifiedTasks);
    }


    @Override
    public ESat isEntailed() {
        return ESat.TRUE;
    }


    @Override
    public int getPropagationConditions(int vIdx) {
        return IntEventType.BOUND.getMask() + IntEventType.INSTANTIATE.getMask();
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            if (RCARD == 0) {
                this.fails();
            }
            this.modifiedTasks.set(0, TS);
        }
        try {
            sweeps[0].findRelations();
            sweeps[1].findRelations();
        } finally {
            this.modifiedTasks.clear();
        }
    }

    @Override
    public void propagate(int idxVarInProp, int mask) throws ContradictionException {
        modifiedTasks.set(idxVarInProp % TS);

        forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
    }


    public abstract class ISweep {
        PropAllenTaskRel masterProp;
        BitSet myRels;
        BitSet modifiedTasks;
        IStateInt[] firstMatchingInterval;
        int[] SUPPORTS = new int[3];
        int TSLB, TSUB, TELB, TEUB, TDLB, TDUB;

        public ISweep(PropAllenTaskRel masterProp, BitSet relations) {
            this.masterProp = masterProp;
            this.myRels = relations;
            this.firstMatchingInterval = new IStateInt[TS];
            this.modifiedTasks = new BitSet(TS);
            for (int i = 0; i < TS; i++) {
                this.firstMatchingInterval[i] = masterProp.getModel().getEnvironment().makeInt(0);
            }
        }

        void setModifiedTasks(BitSet modifiedTasks) {
            this.modifiedTasks = modifiedTasks;
        }

        /**
         * Ensures T_i < T_(i+1)
         *
         * @throws ContradictionException
         */
        void propagateChain() throws ContradictionException {
            propagateStartPlusDurEqualEndMin(0);
            for (int i = 1; i < TS; i++) {
                TSTA[i].updateLowerBound(TEND[i - 1].getLB(), masterProp);
                propagateStartPlusDurEqualEndMin(i);
            }
            propagateStartPlusDurEqualEndMax(TS - 1);
            for (int i = TS - 2; i >= 0; i--) {
                TEND[i].updateUpperBound(TSTA[i + 1].getUB(), masterProp);
                propagateStartPlusDurEqualEndMax(i);
            }
        }

        /**
         * Ensures s + d = e, for lower bounds only
         */
        void propagateStartPlusDurEqualEndMin(int i) throws ContradictionException {
            TSTA[i].updateLowerBound(TEND[i].getLB() - TDUR[i].getUB(), masterProp);
            TEND[i].updateLowerBound(TSTA[i].getLB() + TDUR[i].getLB(), masterProp);
            TDUR[i].updateLowerBound(TEND[i].getLB() - TSTA[i].getUB(), masterProp);
        }

        /**
         * Ensures s + d = e, for lower bounds only
         */
        void propagateStartPlusDurEqualEndMax(int i) throws ContradictionException {
            TEND[i].updateUpperBound(TSTA[i].getUB() + TDUR[i].getUB(), masterProp);
            TSTA[i].updateUpperBound(TEND[i].getUB() - TDUR[i].getLB(), masterProp);
            TDUR[i].updateUpperBound(TEND[i].getUB() - TSTA[i].getLB(), masterProp);
        }

        /**
         * Main filtering algorithm
         */
        abstract void findRelations() throws ContradictionException;

        abstract ESat isSatisfied();
    }

    public class SweepMin extends ISweep {

        public SweepMin(PropAllenTaskRel mother, BitSet relations) {
            super(mother, relations);
        }

        void findRelations() throws ContradictionException {
//            propagateChain();
            int iidx = 0;
            if (LINEAR) {
                for (int tidx = modifiedTasks.nextSetBit(0); tidx > -1; tidx = modifiedTasks.nextSetBit(tidx + 1)) {
                    if (TDUR[tidx].getLB() > 0) {
                        iidx = lowerBounds(tidx, iidx);
                        propagateStartPlusDurEqualEndMin(tidx);
                        propagateStartPlusDurEqualEndMax(tidx);
                    }
                    if (tidx < TS - 1) {
                        // task i ends before task i+1
                        if (TSTA[tidx + 1].updateLowerBound(TEND[tidx].getLB(), masterProp)) {
                            modifiedTasks.set(tidx + 1);
                            // E >= S+P  pour i+1
                            TEND[tidx + 1].updateLowerBound(TSTA[tidx + 1].getLB() + TDUR[tidx + 1].getLB(), masterProp);
                        }
                    }
                }
            } else {
                for (int tidx = 0; tidx < TS; tidx++) {
                    if (TDUR[tidx].getLB() > 0) {
                        lowerBounds(tidx, 0);
                        propagateStartPlusDurEqualEndMin(tidx);
                        propagateStartPlusDurEqualEndMax(tidx);
                    }
                    if (tidx < TS - 1) {
                        // task i ends before task i+1
                        if (TSTA[tidx + 1].updateLowerBound(TEND[tidx].getLB(), masterProp)) {
                            // E >= S+P  pour i+1
                            TEND[tidx + 1].updateLowerBound(TSTA[tidx + 1].getLB() + TDUR[tidx + 1].getLB(), masterProp);
                        }
                    }
                }
            }
        }

        private int lowerBounds(int tidx, int iidx) throws ContradictionException {
            boolean run;
            do {
                if (LINEAR) {
                    iidx = Math.max(iidx, firstMatchingInterval[tidx].get());
                }
                // Initialize dates
                TSLB = TSTA[tidx].getLB();
                TSUB = TSTA[tidx].getUB();
                TDLB = TDUR[tidx].getLB();
                TDUB = TDUR[tidx].getUB();
                TELB = TEND[tidx].getLB();
                TEUB = TEND[tidx].getUB();

                // if rels contains P and P validates an interval, no more checks need to be done
                if (myRels.get(relP) && P.condition(TSLB, TSUB, TDLB, TDUB, TELB, TEUB, ISTA[IS - 1], IEND[IS - 1])) {
                    return iidx;
                }

                SUPPORTS[0] = TSUB + 1;
                SUPPORTS[1] = TDUB + 1;
                SUPPORTS[2] = TEUB + 1;

                int ok = Integer.MAX_VALUE; // on garde en mémoire l'indice de l'intervalle donnant le premier support

                // PI only needs to be validate on interval 0
                if (myRels.get(relPI) && PI.condition(TSLB, TSUB, TDLB, TDUB, TELB, TEUB, ISTA[0], IEND[0])) {
                    PI.lowerBounds(SUPPORTS, TSLB, TSUB, TDLB, TDUB, TELB, TEUB, ISTA[0], IEND[0]); //May be validate by interval #0 (best one)
                    ok = Math.min(ok, iidx);
                }
                do {
                    // Loop over each declared relation
                    for (int i = myRels.nextSetBit(0); i >= 0; i = myRels.nextSetBit(i + 1)) {
                        if (relations[i] != PI) { // can be skipped since if T0 validates it, that's enough
                            if (relations[i].condition(TSLB, TSUB, TDLB, TDUB, TELB, TEUB, ISTA[iidx], IEND[iidx])) {
                                relations[i].lowerBounds(SUPPORTS, TSLB, TSUB, TDLB, TDUB, TELB, TEUB, ISTA[iidx], IEND[iidx]);
                                ok = Math.min(ok, iidx);
                            }
                        }
                    }
                    iidx++;
                }
                while (iidx < IS && /*ok == Integer.MAX_VALUE*/SUPPORTS[2] >= ISTA[iidx - 1] && !(SUPPORTS[0] == TSLB && SUPPORTS[1] == TDLB && SUPPORTS[2] == TELB));

                if (ok == Integer.MAX_VALUE) { // si on a pas trouvé d'intervalle donnant un support => échec
                    fails();
                }

                firstMatchingInterval[tidx].set(ok); // on memorise l'indice du premier intervalle support pour la prochaine propagation

                run = TSTA[tidx].updateLowerBound(SUPPORTS[0], masterProp) && TSTA[tidx].getLB() > SUPPORTS[0];
//                run |= TDUR[tidx].updateLowerBound(SUPPORTS[1], masterProp) && TDUR[tidx].getLB() > SUPPORTS[1];
                run |= TEND[tidx].updateLowerBound(SUPPORTS[2], masterProp) && TEND[tidx].getLB() > SUPPORTS[2];
                if (run) {
                    iidx = ok; // pour gérer les domaines a trous, on repart du premier intervalle validant
                }
            } while (run && iidx < IS);
            return Math.max(0, iidx - 2); // on fait le -2 a cause de l'increment en fin de boucle while
        }

        public ESat isSatisfied() {
            return ESat.TRUE;
        }

    }

    public class SweepMax extends ISweep {

        public SweepMax(PropAllenTaskRel masterProp, BitSet relations) {
            super(masterProp, relations);
        }

        void findRelations() throws ContradictionException {
//            propagateChain();
            int iidx = IS - 1;
            if (LINEAR) {
                for (int tidx = modifiedTasks.previousSetBit(TS - 1); tidx > -1; tidx = modifiedTasks.previousSetBit(tidx - 1)) {
                    if (TDUR[tidx].getLB() > 0) {
                        iidx = lowerBounds(tidx, iidx);
                        propagateStartPlusDurEqualEndMin(tidx);
                        propagateStartPlusDurEqualEndMax(tidx);
                    }
                    if (tidx > 0) {
                        if (TEND[tidx - 1].updateUpperBound(TSTA[tidx].getUB(), masterProp)) {
                            modifiedTasks.set(tidx - 1);
                            TSTA[tidx - 1].updateUpperBound(TEND[tidx - 1].getUB() - TDUR[tidx - 1].getLB(), masterProp);
                        }
                    }
                }
            } else {
                for (int tidx = TS - 1; tidx > -1; tidx--) {
                    if (TDUR[tidx].getLB() > 0) {
                        lowerBounds(tidx, IS-1);
                        propagateStartPlusDurEqualEndMin(tidx);
                        propagateStartPlusDurEqualEndMax(tidx);
                    }
                    if (tidx > 0) {
                        if (TEND[tidx - 1].updateUpperBound(TSTA[tidx].getUB(), masterProp)) {
                            modifiedTasks.set(tidx - 1);
                            TSTA[tidx - 1].updateUpperBound(TEND[tidx - 1].getUB() - TDUR[tidx - 1].getLB(), masterProp);
                        }
                    }
                }
            }
        }


        private int lowerBounds(int tidx, int iidx) throws ContradictionException {
            boolean run;
            do {
                if (LINEAR) {
                    iidx = Math.max(iidx, firstMatchingInterval[tidx].get());
                }
                // Initialize dates
                TSLB = TSTA[tidx].getLB();
                TSUB = TSTA[tidx].getUB();
                TDLB = TDUR[tidx].getLB();
                TDUB = TDUR[tidx].getUB();
                TELB = TEND[tidx].getLB();
                TEUB = TEND[tidx].getUB();

                // if rels contains PI and PI validates an interval, no more checks need to be done
                if (myRels.get(relP) && P.condition(-TEUB, -TELB, TDLB, TDUB, -TSUB, -TSLB, -IEND[0], -ISTA[0])) {
                    return iidx;
                }

                SUPPORTS[0] = -TELB + 1;
                SUPPORTS[1] = TDUB + 1;
                SUPPORTS[2] = -TSLB + 1;

                int ok = Integer.MIN_VALUE; // on garde en mémoire l'indice de l'intervalle donnant le premier support

                // PI only needs to be validate on interval 0
                if (myRels.get(relPI) && PI.condition(-TEUB, -TELB, TDLB, TDUB, -TSUB, -TSLB, -IEND[IS - 1], -ISTA[IS - 1])) {
                    PI.lowerBounds(SUPPORTS, -TEUB, -TELB, TDLB, TDUB, -TSUB, -TSLB, -IEND[IS - 1], -ISTA[IS - 1]); //May be validate by interval #0 (best one)
                    ok = Math.max(ok, iidx);
                }
                do {
                    // Loop over each declared relation
                    for (int i = myRels.nextSetBit(0); i >= 0; i = myRels.nextSetBit(i + 1)) {
                        if (relations[i] != PI) { // can be skipped since if T0 validates it, that's enough
                            if (relations[i].condition(-TEUB, -TELB, TDLB, TDUB, -TSUB, -TSLB, -IEND[iidx], -ISTA[iidx])) {
                                relations[i].lowerBounds(SUPPORTS, -TEUB, -TELB, TDLB, TDUB, -TSUB, -TSLB, -IEND[iidx], -ISTA[iidx]);
                                ok = Math.max(ok, iidx);
                            }
                        }
                    }
                    iidx--;
                }
                while (iidx > -1 && /*ok == Integer.MIN_VALUE*/SUPPORTS[2] >= -IEND[iidx + 1] && !(SUPPORTS[0] == -TEUB && SUPPORTS[1] == TDLB && SUPPORTS[2] == -TSUB));

                if (ok == Integer.MIN_VALUE) { // si on a pas trouvé d'intervalle donnant un support => échec
                    fails();
                }

                firstMatchingInterval[tidx].set(ok); // on memorise l'indice du premier intervalle support pour la prochaine propagation

                run = TSTA[tidx].updateUpperBound(-SUPPORTS[2], masterProp) && TSTA[tidx].getUB() < -SUPPORTS[2];
//                run |= TDUR[tidx].updateLowerBound(SUPPORTS[1], masterProp) && TDUR[tidx].getLB() > SUPPORTS[1];
                run |= TEND[tidx].updateUpperBound(-SUPPORTS[0], masterProp) && TEND[tidx].getUB() < -SUPPORTS[0];
                if (run) {
                    iidx = ok; // pour gérer les domaines a trous, on repart du premier intervalle validant
                }
            } while (run && iidx > -1);
            return Math.min(IS - 1, iidx + 2); // on fait le -2 a cause de l'increment en fin de boucle while
        }

        public ESat isSatisfied() {
            return ESat.TRUE;
        }
    }
}
