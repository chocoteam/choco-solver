/**
 * Copyright (c) 1999-2014, Ecole des Mines de Nantes
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
package org.chocosolver.solver.explanations.arlil;

import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.explanations.store.IEventStore;
import org.chocosolver.solver.search.strategy.assignments.DecisionOperator;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;

import static org.chocosolver.solver.variables.events.PropagatorEventType.FULL_PROPAGATION;

/**
 * A RuleStore is a central object in ARLIL explanation engine.
 * It stores a set of rules which enables to compute the reason of a <i>situation</i> (for instance a conflict) by
 * scanning the events generated on the current branch.
 * The set of rules is dynamically maintained.
 * <p>
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 */
public class RuleStore {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleStore.class);
    private static final int NO_ENTRY = Integer.MIN_VALUE;
    static final int DM = 15;
    static final int BD = 7;
    static final int UB = 5;
    static final int LB = 3;
    static final int RM = 1;


    private final TIntHashSet paRules; // rules for propagator activation
    private final TIntIntHashMap vmRules;    // rules for variable modification
    private final TIntObjectHashMap<TIntSet> vmRemval;    // store value removal when necessary
    private final TIntObjectHashMap<TIntObjectHashMap<HashMap<DecisionOperator, Reason>>> decRefut; // store refuted decisions


    private IntVar lastVar;
    private IEventType lastEvt;
    private int lastVid, lastValue, lastMask;

    public RuleStore() {
        paRules = new TIntHashSet(16, 0.5f, NO_ENTRY);
        vmRules = new TIntIntHashMap(16, .5f, NO_ENTRY, NO_ENTRY);
        vmRemval = new TIntObjectHashMap<>(16, .5f, NO_ENTRY);
        decRefut = new TIntObjectHashMap<>(16, .5f, NO_ENTRY);
    }

    /**
     * Return true if the event represented by matches one of the active rules.
     *
     * @param idx        index in <code>eventStore</code> of the event to evaluate
     * @param eventStore set of events
     * @throws org.chocosolver.solver.exception.SolverException when the type of the variable is neither {@link Variable#BOOL} or {@link Variable#INT}.
     */
    public boolean match(final int idx, final IEventStore eventStore) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("MATCH? < {} / {} / {} / {} / {} >", eventStore.getVariable(idx), eventStore.getCause(idx), eventStore.getEventType(idx),
                    eventStore.getFirstValue(idx), eventStore.getSecondValue(idx), eventStore.getThirdValue(idx));
        }

        lastVar = eventStore.getVariable(idx);
        lastValue = eventStore.getFirstValue(idx); // either the propagator ID, or a value related to the variable event (eg, instantiated value)
        lastEvt = eventStore.getEventType(idx);

        if (lastEvt != FULL_PROPAGATION) {
            // the event is a variable modification
            lastVid = lastVar.getId();
            lastMask = vmRules.get(lastVid);

            if (lastMask == DM) { // only to speed up the entire process
                return true;
            } else if (lastMask != NO_ENTRY) {
                IntEventType ievt = (IntEventType) lastEvt;
                return matchDomain(lastMask, lastVar, ievt, lastValue, eventStore.getSecondValue(idx), eventStore.getThirdValue(idx));
            }
            return false;
        } else {
            // Does it match a propagator activation known rule?
            return paRules.contains(lastValue);
        }
    }

    /**
     * Check whether a variable domain matches a rule
     *
     * @param ruleMask the current rule mask
     * @param ivar     the integer variable
     * @param evt      the event
     * @param i1       either instantiated value (IN) or new lb (LB) or new ub (UB) or removed value (RM)
     * @param i2       either old lb (IN, LB) or old ub (UB) or -1 (RM)
     * @param i3       either old ub (IN), or -1 (LB, UB, RM)
     */
    boolean matchDomain(int ruleMask, IntVar ivar, IntEventType evt, int i1, int i2, int i3) {
        int vid = ivar.getId();
        switch (ruleMask) {
            case DM:
                return true;
            case BD:
                switch (evt) {
                    case INSTANTIATE:
                        return i2 < i1 || i1 < i3;
                    case DECUPP:
                    case INCLOW:
                        return true;
                    case REMOVE:
                        return (i1 < ivar.getLB() || i1 > ivar.getUB());
                }
            case UB:
                switch (evt) {
                    case INSTANTIATE:
                        return i1 < i3;
                    case DECUPP:
                        return true;
                    case INCLOW:
                        return false;
                    case REMOVE:
                        return i1 > ivar.getUB();
                }
            case LB:
                switch (evt) {
                    case INSTANTIATE:
                        return i1 > i2;
                    case DECUPP:
                        return false;
                    case INCLOW:
                        return true;
                    case REMOVE:
                        return i1 < ivar.getLB();
                }
            case RM:
                if (ivar.hasEnumeratedDomain()) {
                    switch (evt) {
                        case INSTANTIATE:
                            return intersect(i2, i3, vmRemval.get(vid));
                        case DECUPP:
                            return intersect(i1, i2, vmRemval.get(vid));
                        case INCLOW:
                            return intersect(i2, i1, vmRemval.get(vid));
                        case REMOVE:
                            return vmRemval.get(vid).contains(i1);
                    }
                }
        }
        throw new SolverException("Unknown event");
    }

    /**
     * Check whether an interval intersects at least one value from a given set
     *
     * @param i1      lower bound of the interval (included)
     * @param i2      upper bound of the interval (included)
     * @param tIntSet set of values
     */
    private boolean intersect(int i1, int i2, TIntSet tIntSet) {
        while (i1 <= i2 && !tIntSet.contains(i1)) {
            i1++;
        }
        return i1 <= i2;
    }


    /**
     * Update the rule store, and the reason, wrt a given event
     *
     * @param idx        index of the event
     * @param eventStore the event store
     * @param reason     the reason to compute
     */
    public void update(final int idx, final IEventStore eventStore, Reason reason) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug("UPDATE < {} / {} / {} / {} / {} >", eventStore.getVariable(idx), eventStore.getCause(idx), eventStore.getEventType(idx),
                    eventStore.getFirstValue(idx), eventStore.getSecondValue(idx), eventStore.getThirdValue(idx));
        }
        assert lastVar == eventStore.getVariable(idx) : "Wrong variable loaded";
        assert lastEvt == eventStore.getEventType(idx) : "Wrong event loaded";
        if (!lastEvt.equals(FULL_PROPAGATION)) {

            // Get the cause
            ICause lastCause = eventStore.getCause(idx);
            // If the cause is a decision
            if (lastCause instanceof Decision) {
                Decision decision = (Decision) lastCause;
                // If it is a LEFT decision, simply add it
                if (decision.hasNext()) {
                    reason.addDecicion(decision);
                } else {
                    // Otherwise, get the reason of the refutation
                    Reason drr = getDecisionRefutationReason(decision);
                    assert drr != null : "No reason for decision refutation :" + decision.toString();
                    reason.addAll(drr);
                }
            } else {
                assert lastValue == eventStore.getFirstValue(idx) : "Wrong value loaded";

                // The cause is not a decision, that is, certainly a propagator
                // add the cause to the reason
                reason.addCause(lastCause);
                // then add new rules to the rule store to explain the cause application
                lastCause.why(this, lastVar, lastEvt, lastValue);
            }
        } else {
            // the event was a propagator activation
            // 1. add a new rule: reason of the variable instantiation
            addFullDomainRule(lastVar);
            // 2. remove the propagator activation rule, now we know it depends on the variable
            paRules.remove(lastValue);
        }
    }


    /**
     * Clear the rule store, remove all data stored
     */
    public void clear() {
        paRules.clear();
        vmRules.clear();
        for (int k : vmRemval.keys()) {
            vmRemval.get(k).clear();
        }
//        //TODO: clear decRefute...
    }

    /**
     * Check whether the rule store is empty
     *
     * @return true if the rule store is empty
     */
    public boolean isEmpty() {
        return paRules.isEmpty() && vmRules.isEmpty();
    }


    /**
     * Add a value removal rule, that is, the event which remove the value needs to be retained.
     *
     * @param var   the variable to add rule on
     * @param value the removed value
     * @return true if a new rule has been added (false = already existing rule)
     * @throws org.chocosolver.solver.exception.SolverException when the domain is not enumerated
     */
    public boolean addRemovalRule(IntVar var, int value) {
        if (var.hasEnumeratedDomain()) {
            int vid = var.getId();
            putMask(vid, RM);
            TIntSet remvals = vmRemval.get(vid);
            if (remvals == null) {
                remvals = new TIntHashSet(16, .5f, NO_ENTRY);
                vmRemval.put(vid, remvals);
            }
            return remvals.add(value);
        } else {
            if (value <= var.getLB()) {
                // Only value strictly lesser than the current LB are eligible.
                // One exception, though, when the event tested is the one that fails, then it can be equal to.
                return addLowerBoundRule(var);
            } else if (value >= var.getUB()) {
                // Only value strictly greater than the current UB are eligible.
                // One exception, though, when the event tested is the one that fails, then it can be equal to.
                return addUpperBoundRule(var);
            } else {
                throw new SolverException("Cannot add REMOVE rule for bounded variable");
            }
        }
    }

    /**
     * Add a full domain rule, that is, any events involving the variable needs to be retained.
     *
     * @param var the variable to add rule on
     * @return true if a new rule has been added (false = already existing rule)
     */
    public boolean addFullDomainRule(IntVar var) {
        return putMask(var.getId(), DM);
    }

    /**
     * Add a lower bound rule, that is, any event on the lower bound of the variable needs to be retained
     *
     * @param var the variable to add rule on
     * @return true if a new rule has been added (false = already existing rule)
     */
    public boolean addLowerBoundRule(IntVar var) {
        return putMask(var.getId(), LB);
    }

    /**
     * Add a upper bound rule, that is, any event on the upper bound of the variable needs to be retained
     *
     * @param var the variable to add rule on
     * @return true if a new rule has been added (false = already existing rule)
     */
    public boolean addUpperBoundRule(IntVar var) {
        return putMask(var.getId(), UB);
    }

    /**
     * Update the rule mask for a given variable (denoted by its vid)
     *
     * @param vid  the index of the variable
     * @param mask the new mask to merge
     * @return true if the mask has been updated (false = already existing mask)
     */
    private boolean putMask(int vid, int mask) {
        int cmask = vmRules.get(vid);
        if (cmask == NO_ENTRY) {
            return vmRules.put(vid, mask) == NO_ENTRY;
        } else {
            int amount = (cmask | mask) - cmask;
            return amount > 0 && vmRules.adjustValue(vid, amount);
        }
    }

    /**
     * Return the current rule mask associated to the variable vid
     *
     * @param var a variable
     * @return the current mask or NO_ENTRY
     */
    public int getMask(Variable var) {
        return vmRules.get(var.getId());
    }

    /**
     * Add a propagator activation rule
     *
     * @param propagator activated propagator
     * @return true if a new rule has been adde
     */
    public boolean addPropagatorActivationRule(Propagator propagator) {
        paRules.add(propagator.getId());
        return false;
    }


    /**
     * Store a decision refutation, for future reasoning.
     *
     * @param decision refuted decision
     * @param reason   the reason of the refutation
     */
    public void storeDecisionRefutation(Decision decision, Reason reason) {
        TIntObjectHashMap<HashMap<DecisionOperator, Reason>> mk1 = decRefut.get(decision.getDecisionVariable().getId());
        if (mk1 == null) {
            mk1 = new TIntObjectHashMap<>(16, .5f, NO_ENTRY);
            decRefut.put(decision.getDecisionVariable().getId(), mk1);
        }
        HashMap<DecisionOperator, Reason> mk2 = mk1.get((Integer) decision.getDecisionValue());
        if (mk2 == null) {
            mk2 = new HashMap<>(16, .5f);
            mk1.put((Integer) decision.getDecisionValue(), mk2);
        }
        mk2.put(decision.getDecisionOperator(), reason);
    }

    /**
     * Get the reason associated to a decision refutation
     *
     * @param decision a RIGHT branch decision
     * @return a reason
     */
    Reason getDecisionRefutationReason(Decision decision) {
        if (decision.hasNext()) {
            throw new SolverException(decision.toString() + "is not explained yet");
        }
        return decRefut
                .get(decision.getDecisionVariable().getId())
                .get((Integer) decision.getDecisionValue())
                .get(decision.getDecisionOperator());
    }


    /**
     * Inform the rule store that the propagator cannot provide more rules in the future
     *
     * @param cause a cause
     */
    public void skip(ICause cause) {
        if (LOGGER.isWarnEnabled()) {
            LOGGER.warn("Skip method does not do anything, {} will not be skipped", cause);
        }
    }

    /**
     * Print the retained rules
     *
     * @param solver a solver to get the variables
     */
    public void printRules(Solver solver) {
        StringBuilder st = new StringBuilder();
        for (Variable v : solver.getVars()) {
            int m = vmRules.get(v.getId());
            if (m != NO_ENTRY) {
                st.append(v.getName()).append(":").append(m);
                if (vmRemval.contains(v.getId()) && vmRemval.get(v.getId()).size() > 0) {
                    TIntSet values = vmRemval.get(v.getId());
                    st.append("\n\t").append(Arrays.toString(values.toArray()));
                }
                st.append("\n");
            }
        }
        System.out.printf("%s\n", st.toString());
    }
}
