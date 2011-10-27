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

package solver.explanations;

import choco.kernel.common.util.iterators.DisposableValueIterator;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
import solver.exception.ContradictionException;
import solver.search.strategy.decision.Decision;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 14:18:18
 * <p/>
 * An RecorderExplanationEngine is used to record explanations throughout computation.
 * Here we just record the explanations in a HashMap ...
 * <p/>
 */
public class RecorderExplanationEngine extends ExplanationEngine {

    HashMap<Variable, OffsetIStateBitset> removedvalues; // maintien du domaine courant
    HashMap<IntVar, HashMap<Integer, ValueRemoval>> valueremovals; // maintien de la base de deduction
    HashMap<Deduction, Explanation> database; // base d'explications

    HashMap<Variable, HashMap<Integer, VariableAssignment>> variableassignments; // maintien de la base de VariableAssignment
    HashMap<Variable, HashMap<Integer, VariableRefutation>> variablerefutations; // maintien de la base de VariableRefutation


    public RecorderExplanationEngine(Solver solver) {
        super(solver);
        removedvalues = new HashMap<Variable, OffsetIStateBitset>();
        valueremovals = new HashMap<IntVar, HashMap<Integer, ValueRemoval>>();
        database = new HashMap<Deduction, Explanation>();
        variableassignments = new HashMap<Variable, HashMap<Integer, VariableAssignment>>();
        variablerefutations = new HashMap<Variable, HashMap<Integer, VariableRefutation>>();

        for(Variable v : solver.getVars()) {
            getRemovedValues((IntVar) v); // TODO make a more generic method for that
        }
    }

    @Override
    public OffsetIStateBitset getRemovedValues(IntVar v) {
        OffsetIStateBitset toreturn = removedvalues.get(v);
        if (toreturn == null) {
            toreturn = new OffsetIStateBitset(v); // .getSolver().getEnvironment().makeBitSet(v.getUB());
            removedvalues.put(v, toreturn);
            valueremovals.put(v, new HashMap<Integer, ValueRemoval>());
        }
        return toreturn;
    }

    @Override
    public Explanation retrieve(IntVar var, int val) {
        return database.get(getValueRemoval(var, val));
    }

    protected ValueRemoval getValueRemoval(IntVar var, int val) {
        ValueRemoval vr;
        HashMap<Integer, ValueRemoval> hm = valueremovals.get(var);
        if (hm == null) {
            valueremovals.put(var, new HashMap<Integer, ValueRemoval>());
        }
        vr = valueremovals.get(var).get(val);
        if (vr == null) {
            vr = new ValueRemoval(var, val);
            valueremovals.get(var).put(val, vr);
        }
        return vr;
    }

    @Override
    public int getWorldIndex(Variable var, int val) {
        int wi = solver.getEnvironment().getWorldIndex();
        Decision dec = solver.getSearchLoop().decision;
        while (! dec.getPositiveDeduction().getVar().equals(var)) {
            dec = dec.getPrevious();
            wi--;
        }
        return wi;
    }

    @Override
    public VariableAssignment getVariableAssignment(IntVar var, int val) {
        HashMap mapvar = variableassignments.get(var);
        if (mapvar == null) {
            variableassignments.put(var, new HashMap<Integer, VariableAssignment>());
            variableassignments.get(var).put(val, new VariableAssignment(var, val));
        }
        VariableAssignment vr = variableassignments.get(var).get(val);
        if (vr == null) {
            vr = new VariableAssignment(var, val);
            variableassignments.get(var).put(val, vr);
        }
        return vr;
    }

    @Override
    public VariableRefutation getVariableRefutation(IntVar var, int val, Decision dec) {
        HashMap mapvar = variablerefutations.get(var);
        if (mapvar == null) {
            variablerefutations.put(var, new HashMap<Integer, VariableRefutation>());
            variablerefutations.get(var).put(val, new VariableRefutation(var, val, dec));
        }
        VariableRefutation vr = variablerefutations.get(var).get(val);
        if (vr == null) {
            vr = new VariableRefutation(var, val, dec);
            variablerefutations.get(var).put(val, vr);
        }
        vr.decision = dec;
        return vr;
    }


    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        OffsetIStateBitset invdom = getRemovedValues(var);
        Deduction vr = getValueRemoval(var, val);
        Explanation expl = cause.explain(vr);
        database.put(vr, expl);
        invdom.set(val);
        emList.onRemoveValue(var, val, cause, expl);
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int val, ICause cause) {
        OffsetIStateBitset invdom = getRemovedValues(var);
        for (int v = old; v < val; v++) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
            Explanation expl = cause.explain(vr);
            database.put(vr, expl);
            invdom.set(v);
            emList.onUpdateLowerBound(var, old, val, cause, expl);
        }
    }

    @Override
    public void updateUpperBound(IntVar var, int old, int val, ICause cause) {
        OffsetIStateBitset invdom = getRemovedValues(var);
        for (int v = old; v > val; v--) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
            Explanation explain = cause.explain(vr);
            database.put(vr, explain);
            invdom.set(v);
            emList.onUpdateUpperBound(var, old, val, cause, explain);
        }
    }


    @Override
    public void instantiateTo(IntVar var, int val, ICause cause) {
        OffsetIStateBitset invdom = getRemovedValues(var);
        DisposableValueIterator it = var.getValueIterator(true);
        while (it.hasNext()) {
            int v = it.next();
            if ( v != val ) {
                Deduction vr = getValueRemoval(var,v);
                Explanation explain = cause.explain(vr);
                database.put(vr, explain);
                invdom.set(v);
                emList.onInstantiateTo(var, val, cause, explain);
            }
        }
    }

    @Override
    public Explanation flatten(Explanation expl) {
        Explanation toreturn = new Explanation(null, null);

        Set<Deduction> toexpand = new HashSet<Deduction>();
        Set<Deduction> expanded = new HashSet<Deduction>();

        if (expl.deductions != null) {
            toexpand = new HashSet<Deduction>(expl.deductions); //
        }
        while (!toexpand.isEmpty()) {
            Deduction d = toexpand.iterator().next();
            toexpand.remove(d);
            expanded.add(d);
            Explanation e = database.get(d);

            if (e != null) {
                if (e.contraintes != null) {
                    for (Propagator prop : e.contraintes) {
                        toreturn.add(prop);
                    }
                }
                if (e.deductions != null) {

                    for (Deduction ded : e.deductions) {
                        if (!expanded.contains(ded)) {
                            toexpand.add(ded);
                        }
                    }
                }
            } else {
                toreturn.add(d);
            }
        }
        return toreturn;
    }

    @Override
    public Explanation flatten(IntVar var, int val) {
        // TODO check that it is always called with val NOT in var
        return flatten(getValueRemoval(var, val));
    }

    @Override
    public Explanation flatten(Deduction deduction) {
        Explanation expl = new Explanation(null, null);
        expl.add(deduction);
        return flatten(expl);
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        return explain(getValueRemoval(var, val));
    }

    @Override
    public Deduction explain(Deduction deduction) {
        return deduction;
    }


    private Decision updateVRExplainUponbacktracking(int nworld, Explanation expl) {
        Decision dec = solver.getSearchLoop().decision; // the current decision to undo
        while (dec != null && nworld > 1) {
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != null) {
            if (! dec.hasNext())  throw new UnsupportedOperationException("RecorderExplanationEngine.updatVRExplain should get to a POSITIVE decision");
            Deduction vr = dec.getNegativeDeduction();
            Deduction assign = dec.getPositiveDeduction();
            expl.remove(assign);
            if  (assign instanceof  VariableAssignment) {
                VariableAssignment va = (VariableAssignment) assign;
                variableassignments.get(va.var).remove(va.val);
            }
            database.put(vr, flatten(expl));
        }
        return dec;
    }



    @Override
    public void onContradiction(ContradictionException cex) {
        if ((cex.v != null) || (cex.c != null)) { // contradiction on domain wipe out
            Explanation expl = (cex.v != null) ? cex.v.explain(VariableState.DOM)
                    : cex.c.explain(null);
            Solver solver = (cex.v != null) ? cex.v.getSolver() : cex.c.getConstraint().getVariables()[0].getSolver();
            Explanation complete = flatten(expl);
            int upto = complete.getMostRecentWorldToBacktrack(this);
            solver.getSearchLoop().overridePreviousWorld(upto);
            Decision dec = updateVRExplainUponbacktracking(upto, complete);
            emList.onContradiction(cex, complete, upto, dec);
        } else {
            throw new UnsupportedOperationException("RecorderExplanationEngine.onContradiction incoherent state");
        }
    }

    @Override
    public void onRemoveValue(IntVar var, int val, ICause cause, Explanation explanation) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("::EXPL:: REMVAL " + val + " FROM " + var + " APPLYING " + cause + " BECAUSE OF " + flatten(explanation));
        }
    }

    @Override
    public void onUpdateLowerBound(IntVar intVar, int old, int value, ICause cause, Explanation explanation) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("::EXPL:: UPLOWB from " + old + " to " + value + " FOR " + intVar + " APPLYING " + cause + " BECAUSE OF " + flatten(explanation));
        }
    }

    @Override
    public void onUpdateUpperBound(IntVar intVar, int old, int value, ICause cause, Explanation explanation) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("::EXPL:: UPUPPB from " + old + " to " + value + " FOR " + intVar + " APPLYING " + cause + " BECAUSE OF " + flatten(explanation));
        }
    }

    @Override
    public void onInstantiateTo(IntVar var, int val, ICause cause, Explanation explanation) {
        if (LOGGER.isInfoEnabled()) {
            LOGGER.info("::EXPL:: INST to " + val + " FOR " + var + " APPLYING " + cause + " BECAUSE OF " + flatten(explanation));
        }
    }

    @Override
    public void onContradiction(ContradictionException cex, Explanation explanation, int upTo, Decision decision) {
        if (LOGGER.isInfoEnabled()) {
            if (cex.v != null) {
                LOGGER.info("::EXPL:: CONTRADICTION on " + cex.v + " BECAUSE " + explanation);
            }
            else if (cex.c != null) {
                LOGGER.info("::EXPL:: CONTRADICTION on " + cex.c + " BECAUSE " + explanation);
            }
            LOGGER.info("::EXPL:: BACKTRACK on " + decision +" (up to " + upTo + " level(s))");
        }
    }

    @Override
    public void onSolution() {
        // we need to prepare a "false" backtrack on this decision
        Decision dec = solver.getSearchLoop().decision;
        while ((dec != null) && (! dec.hasNext())) {
            dec = dec.getPrevious();
        }
        if (dec != null) {
            database.put(dec.getNegativeDeduction(), Explanation.SYSTEM);
        }
    }


}
