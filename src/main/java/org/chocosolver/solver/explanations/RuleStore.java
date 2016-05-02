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
package org.chocosolver.solver.explanations;

import gnu.trove.set.TIntSet;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.SolverException;
import org.chocosolver.solver.search.strategy.decision.Decision;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Variable;
import org.chocosolver.solver.variables.events.IEventType;
import org.chocosolver.solver.variables.events.IntEventType;

import static org.chocosolver.solver.variables.events.PropagatorEventType.FULL_PROPAGATION;

/**
 * A RuleStore is a central object in the Asynchronous, Reverse, Low-Intrusive and Lazy explanation engine.
 * It stores a set of rules which enables to compute the explanation of a <i>situation</i> (for instance a conflict) by
 * scanning the events generated on the current branch.
 * The set of rules is dynamically maintained.
 * <p>
 * Created by cprudhom on 09/12/14.
 * Project: choco.
 * @author Charles Prud'homme
 * @since 09/12/14
 */
public class RuleStore {

    /**
     * No entry value for variable mask
     */
    private static final int NO_ENTRY = Integer.MIN_VALUE;
    /**
     * Mask for integer variable modification
     */
    protected static final int DM = 15;
    /**
     * Mask for integer variable bounds modification
     */
    protected static final int BD = 7;
    /**
     * Mask for integer variable upper bound modification
     */
    protected static final int UB = 5;
    /**
     * Mask for integer variable lower bound modification
     */
    protected static final int LB = 3;
    /**
     * Mask for integer variable value removal
     */
    protected static final int RM = 1;

    /**
     * Set of modification rules
     */
    private Rules cRules;
    /**
     * Stores explanation of refuted decisions
     */
    private Explanation[] decRefut;
    /**
     * Set to <tt>true</tt> when user feedback is needed, ie to keep trace of the propagator (in conflict)
     */
    private final boolean saveCauses;
    /**
     * Set to <tt>true</tt> to enable stopping the explanation computation to the first involved decision.
     * Faster but weaker.
     */
    private final boolean enablePartialExplanation;
    /**
     * When conditions are favorable, early iteration stop can be considered
     */
    private boolean preemptedStop;
    /**
     * Local-like parameter.
     * Reference to the last variable popped from the event store.
     */
    private IntVar lastVar;
    /**
     * Local-like parameter.
     * Reference to the last event popped from the event store.
     */
    private IEventType lastEvt;
    /**
     * Local-like parameter.
     * Reference to the last value popped from the event store.
     */
    private int lastValue;

    /**
     * Instantiate a rule store to compute explanations
     *  @param saveCauses               does it keep trace of the constraints in conflict ?
     * @param enablePartialExplanation do explanations need to be complete (for DBT or nogood extraction) ?
     */
    public RuleStore(boolean saveCauses, boolean enablePartialExplanation) {
        this.saveCauses = saveCauses;
        this.enablePartialExplanation = enablePartialExplanation;
        decRefut = new Explanation[16];
    }

    /**
     * Initialize the rulestore for a new explanation
     * @param expl an explanation
     */
    public void init(Explanation expl) {
        this.cRules = expl.getRules();
        preemptedStop = false;
    }

    /**
     * when conditions are favorable, a preempted stop can be considered: not all events have to be analyzed.
     * @return return <tt>true</tt> if early stop occurs.
     */
    public boolean isPreemptedStop() {
        return preemptedStop;
    }

    /**
     * Return true if the event represented by matches one of the active rules.
     *
     * @param idx        index in {@code eventStore} of the event to evaluate
     * @param eventStore set of events
     * @throws org.chocosolver.solver.exception.SolverException when the type of the variable is neither {@link Variable#BOOL} or {@link Variable#INT}.
     * @return <tt>true</tt> if the event in position {@code idx} in {@code eventStore} matches a rule
     */
    public boolean match(final int idx, final ArrayEventStore eventStore) {
        lastVar = eventStore.getVariable(idx);
        lastValue = eventStore.getFirstValue(idx); // either the propagator ID, or a value related to the variable event (eg, instantiated value)
        lastEvt = eventStore.getEventType(idx);

        if (lastEvt != FULL_PROPAGATION) {
            // the event is a variable modification
            int lastVid = lastVar.getId();
            int lastMask = cRules.getVmRules(lastVid);

            if (lastMask == DM) { // only to speed up the entire process
                return true;
            } else if (lastMask != NO_ENTRY) {
                IntEventType ievt = (IntEventType) lastEvt;
                return matchDomain(lastMask, lastVar, ievt, lastValue, eventStore.getSecondValue(idx), eventStore.getThirdValue(idx));
            }
            return false;
        } else {
            // Does it match a propagator activation known rule?
            return cRules.getPaRules(lastValue);
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
     * @return <tt>true</tt> the variable state matches a rule
     */
    public boolean matchDomain(int ruleMask, IntVar ivar, IntEventType evt, int i1, int i2, int i3) {
        int vid = ivar.getId();
        switch (ruleMask) {
            case DM:
                return true;
            case BD:
                switch (evt) {
                    case INSTANTIATE:
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
                            return cRules.intersect(i2, i3, vid);
                        case DECUPP:
                            return cRules.intersect(i1, i2, vid);
                        case INCLOW:
                            return cRules.intersect(i2, i1, vid);
                        case REMOVE:
                            return cRules.getVmRemval(vid).contains(i1);
                    }
                }
            default:throw new SolverException("Unknown event");
        }
    }


    /**
     * Update the rule store, and the explanation, wrt a given event
     *
     * @param idx         index of the event
     * @param eventStore  the event store
     * @param explanation the explanation to compute
     */
    @SuppressWarnings({"PointlessBooleanExpression", "ConstantConditions"})
    public void update(final int idx, final ArrayEventStore eventStore, Explanation explanation) {
        assert lastVar == eventStore.getVariable(idx) : "Wrong variable loaded";
        assert lastEvt == eventStore.getEventType(idx) : "Wrong event loaded";
        if (!lastEvt.equals(FULL_PROPAGATION)) {

            // Get the cause
            ICause lastCause = eventStore.getCause(idx);
            // If the cause is a decision
            if (lastCause instanceof Decision) {
                Decision decision = (Decision) lastCause;
                // If it is a LEFT decision, simply add it
                if (decision.hasNext() || decision.getArity() == 1) {
                    explanation.addDecision(decision);
                    // if partial explanation is enabled, finding the first decision in conflict is enough
                    if (decision.getArity() > 1 && (preemptedStop |= enablePartialExplanation)) {
                        explanation.setEvtstrIdx(idx);
                    }
                } else if (decision.getArity() > 1) { //  to deal with unary decision (once = true)
                    // Otherwise, get the explanation of the refutation
                    Explanation drr = getDecisionRefutation(decision);
                    assert drr != null : "No explanation for decision refutation :" + decision.toString();
                    explanation.addCausesAndDecisions(drr); //update decisions and causes into the current explanation
                    explanation.addRules(drr.getRules());
                }
                // if no user feedback is required, ie, no conflict constraints are needed, then..
                if (!saveCauses) {
                    // if all decisions, from the current refuted one to ROOT, explained the conflict,
                    // we can stop prematurely the algorithm
                    preemptedStop |= explanation.getDecisions().previousClearBit(decision.getPosition()) == 0;
                    // we do not need to copy the rules, since the explanation is complete, in term of decisions.
                }
            } else {
                assert lastValue == eventStore.getFirstValue(idx) : "Wrong value loaded";

                // The cause is not a decision, that is, certainly a propagator
                // add the cause to the explanation
                explanation.addCause(lastCause);
                // then add new rules to the rule store to explain the cause application
                lastCause.why(this, lastVar, lastEvt, lastValue);
            }
        } else {
            // the event was a propagator activation
            // 1. add a new rule: explanation of the variable instantiation
            addFullDomainRule(lastVar);
            // 2. remove the propagator activation rule, now we know it depends on the variable
            cRules.paRulesClear(lastValue);
        }
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
            cRules.putMask(vid, RM);
            TIntSet remvals = cRules.getVmRemval(vid);
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
        return cRules.putMask(var.getId(), DM);
    }

    /**
     * Add a lower bound rule, that is, any event on the lower bound of the variable needs to be retained
     *
     * @param var the variable to add rule on
     * @return true if a new rule has been added (false = already existing rule)
     */
    public boolean addLowerBoundRule(IntVar var) {
        return cRules.putMask(var.getId(), LB);
    }

    /**
     * Add a upper bound rule, that is, any event on the upper bound of the variable needs to be retained
     *
     * @param var the variable to add rule on
     * @return true if a new rule has been added (false = already existing rule)
     */
    public boolean addUpperBoundRule(IntVar var) {
        return cRules.putMask(var.getId(), UB);
    }

    /**
     * Add an upper bound rule and a lower bound rule, that is, any event on the upper bound or the lower bound of the variable needs to be retained
     *
     * @param var the variable to add rule on
     * @return true if a new rule has been added (false = already existing rule)
     */
    public boolean addBoundsRule(IntVar var) {
        return cRules.putMask(var.getId(), BD);
    }

    /**
     * Return the current rule mask associated to the variable vid
     *
     * @param var a variable
     * @return the current mask or NO_ENTRY
     */
    public int getMask(Variable var) {
        return cRules.getVmRules(var.getId());
    }

    /**
     * Add a propagator activation rule
     *
     * @param propagator activated propagator
     * @return true if a new rule has been adde
     */
    public boolean addPropagatorActivationRule(Propagator propagator) {
        cRules.addPaRules(propagator.getId());
        return false;
    }


    /**
     * Store a decision refutation, for future reasoning.
     *
     * @param decision    refuted decision
     * @param explanation the explanation of the refutation
     */
    public void storeDecisionRefutation(Decision decision, Explanation explanation) {
        int w = decision.getPosition();
        if (w >= decRefut.length) {
            Explanation[] tmp = decRefut;
            decRefut = new Explanation[w + 10];
            System.arraycopy(tmp, 0, decRefut, 0, tmp.length);
        }
        assert explanation == null || w >= explanation.getDecisions().length();
        decRefut[w] = explanation;
    }

    /**
     * Move a decision refutation to the 'to' index
     *
     * @param decision a refuted decision
     * @param to       the new index
     */
    public void moveDecisionRefutation(Decision decision, int to) {
        assert to <= decision.getPosition();
        if (to < decision.getPosition()) {
            decRefut[to] = decRefut[decision.getPosition()];
            decRefut[decision.getPosition()] = null;
        }
    }

    /**
     * Free the explanation related to the decision (for efficiency purpose only)
     *
     * @param decision the decision which is going to be forgotten
     */
    public void freeDecisionExplanation(Decision decision) {
        int w = decision.getPosition();
        // when dealing with parallel portfolio, the cut given by another worker
        // may lead to free a decision which was not explained yet
        if (w < decRefut.length) {
            if (decRefut[w] != null) {
                decRefut[w].recycle();
                decRefut[w] = null;
            }
        }
    }

    /**
     * Get the explanation associated with a decision refutation
     *
     * @param decision a RIGHT branch decision
     * @return an explanation
     */
    public Explanation getDecisionRefutation(Decision decision) {
        assert decision.triesLeft() < 2 : decision.toString() + "is not explained yet";
        return decRefut[decision.getPosition()];
    }
}
