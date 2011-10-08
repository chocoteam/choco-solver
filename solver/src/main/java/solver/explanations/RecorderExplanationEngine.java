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
import choco.kernel.memory.IStateBitSet;
import com.sun.tools.javac.util.Pair;
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
 * TODO revise the way of recording in order to be able to retrieve information
 */
public class RecorderExplanationEngine extends ExplanationEngine {

    HashMap<Variable, IStateBitSet> removedvalues; // maintien du domaine courant
    HashMap<Variable, HashMap<Integer, ValueRemoval>> valueremovals; // maintien de la base de deduction
    HashMap<Deduction, Explanation> database; // base d'explications

    HashMap<Variable, HashMap<Integer, Pair<VariableAssignment, Integer>>> variableassignments; // maintien de la base de VariableAssignment
    HashMap<Variable, HashMap<Integer, VariableRefutation>> variablerefutations; // maintien de la base de VariableRefutation


    public RecorderExplanationEngine(Solver solver) {
        super(solver);
        removedvalues = new HashMap<Variable, IStateBitSet>();
        valueremovals = new HashMap<Variable, HashMap<Integer, ValueRemoval>>();
        database = new HashMap<Deduction, Explanation>();
        variableassignments = new HashMap<Variable, HashMap<Integer, Pair<VariableAssignment, Integer>>>();
        variablerefutations = new HashMap<Variable, HashMap<Integer, VariableRefutation>>();

        for(Variable v : solver.getVars()) {
            getRemovedValues((IntVar) v); // TODO make a more generic method for that
        }
    }

    @Override
    public IStateBitSet getRemovedValues(IntVar v) {
        IStateBitSet toreturn = removedvalues.get(v);
        if (toreturn == null) {
            toreturn = solver.getEnvironment().makeBitSet(v.getUB());
            removedvalues.put(v, toreturn);
            valueremovals.put(v, new HashMap<Integer, ValueRemoval>());
        }
        return toreturn;
    }


    @Override
    public Explanation check(IntVar var, int val) {
        return database.get(getValueRemoval(var, val));
    }

    protected ValueRemoval getValueRemoval(IntVar var, int val) {
        ValueRemoval vr = valueremovals.get(var).get(val);
        if (vr == null) {
            vr = new ValueRemoval(var, val);
            valueremovals.get(var).put(val, vr);
        }
        return vr;
    }


    @Override
    public VariableAssignment getVariableAssignment(IntVar var, int val) {
        HashMap mapvar = variableassignments.get(var);
        if (mapvar == null) {
            variableassignments.put(var, new HashMap<Integer, Pair<VariableAssignment, Integer>>());
            variableassignments.get(var).put(val, new Pair<VariableAssignment, Integer>(
                    new VariableAssignment(var, val), this.solver.getEnvironment().getWorldIndex()));
        }
        Pair<VariableAssignment, Integer> vrw = variableassignments.get(var).get(val);
        if (vrw == null) {
            vrw = new Pair<VariableAssignment, Integer>(new VariableAssignment(var, val), this.solver.getEnvironment().getWorldIndex());
            variableassignments.get(var).put(val, vrw);
        }
        return vrw.fst;
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
        //       database.put(vr, vr.explain());
        return vr;
    }


    @Override
    public void removeValue(IntVar var, int val, ICause cause) {
        IStateBitSet invdom = getRemovedValues(var);
        Deduction vr = getValueRemoval(var, val);
        database.put(vr, cause.explain(var, vr));
        invdom.set(val);
//        System.out.println("RecorderExplanationEngine.removeValue");
//        System.out.println("var = " + var + " val " + val);
//        System.out.println("database.get(vr) = " + database.get(vr));
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int val, ICause cause) {
        //System.out.println("recording " + var + " : " + old+ " -> " + val);
        IStateBitSet invdom = getRemovedValues(var);
        for (int v = old; v < val; v++) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
            database.put(vr, cause.explain(var, vr));
            invdom.set(v);
            //           System.out.println("recording " + var + " - " + v + " " + database.get(vr));
        }

    }

    @Override
    public void updateUpperBound(IntVar var, int old, int val, ICause cause) {
        //System.out.println("recording " + var + " : " + old+ " -> " + val);
        IStateBitSet invdom = getRemovedValues(var);
        for (int v = old; v > val; v--) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
//            System.out.println("vr = " + vr);
            database.put(vr, cause.explain(var, vr));
            invdom.set(v);
//            System.out.println("recording " + var + " - " + v + " " + database.get(vr));
        }
    }


    @Override
    public void instantiateTo(IntVar var, int val, ICause cause) {
        //System.out.println("recording " + var + " instantiated to " + val);
        IStateBitSet invdom = getRemovedValues(var);
        DisposableValueIterator it = var.getValueIterator(true);
        while (it.hasNext()) {
            int v = it.next();
            if ( v != val ) {
                Deduction vr = getValueRemoval(var,v);
                database.put(vr, cause.explain(var, vr));
                invdom.set(v);
            }
        }
    }

    @Override
    public Explanation why(Explanation expl) {
        Explanation toreturn = new Explanation(null, null);

        Set<Deduction> toexpand = new HashSet<Deduction>();
        Set<Deduction> expanded = new HashSet<Deduction>();

        if (expl.deductions != null) {
            toexpand = new HashSet<Deduction>(expl.deductions); //
        }
//        System.out.println("RecorderExplanationEngine.why");

        while (!toexpand.isEmpty()) {
            Deduction d = toexpand.iterator().next();
            toexpand.remove(d);
            expanded.add(d);
            Explanation e = database.get(d);
//            System.out.println("d: " + d + " e = " + e);


            if (e != null) {
                if (e.contraintes != null) {
                    for (Propagator prop : e.contraintes) {
                        toreturn.add(prop);
                    }
                }
                if (e.deductions != null) {

                    for (Deduction ded : e.deductions) {
                        if (!expanded.contains(ded)) {
//                            System.out.println("adding ded = " + ded);
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
    public Explanation why(IntVar var, int val) {

        Explanation toreturn = new Explanation(null, null);

        if (var.contains(val)) return toreturn;

        toreturn.add(getValueRemoval(var, val));
        return why(toreturn);
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        ValueRemoval vr = getValueRemoval(var, val);
        return vr;
    }

    @Override
    public Deduction explain(Deduction deduction) {
        return deduction;
    }

    public int getMostRecentWorldToBacktrack(Explanation expl) {
//        System.out.println(">>> RecorderExplanationEngine.getMostRecentWorldToBacktrack");
//        System.out.println(expl);
//        System.out.println("RecorderExplanationEngine.getMostRecentWorldToBacktrack");
        int topworld = 0;
        if (expl.deductions != null) {
            for (Deduction dec : expl.deductions) {
                if (dec instanceof VariableAssignment) {
                    Variable va = ((VariableAssignment) dec).var;
                    int val = ((VariableAssignment) dec).val;
//                    System.out.println("dec = " + dec);
//                    System.out.println("variableassignments = " + variableassignments);
//                    System.out.println(variableassignments.get(va));
                    Pair<VariableAssignment, Integer> vr = variableassignments.get(va).get(val);
//                    System.out.println("dec = " + dec + "vr" + vr.snd);
                    if (vr == null) System.exit(0);
                    if (vr.snd > topworld) {
                        topworld = vr.snd;
                    }
                }
            }
        }
//        System.out.println("RecorderExplanationEngine.getMostRecentWorldToBacktrack");
//        System.out.println(expl);
//        System.out.println("topworld (cw) = " + topworld + " (" + this.solver.getEnvironment().getWorldIndex() + ")");

        updateVRExplainUponbacktracking(1 + (this.solver.getEnvironment().getWorldIndex() - topworld), expl);

//        System.out.println("<<< RecorderExplanationEngine.getMostRecentWorldToBacktrack");

        return 1 + (this.solver.getEnvironment().getWorldIndex() - topworld);
//        return 1;
    }

    private void updateVRExplainUponbacktracking(int nworld, Explanation expl) {
//        System.out.println("RecorderExplanationEngine.updateVRExplainUponbacktracking");
//        System.out.println("nworld = " + nworld);
        Decision dec = solver.getSearchLoop().decision; // the current decision to undo
        while (dec != null && nworld > 1) {
//            System.out.println(")> dec = " + dec);
            dec = dec.getPrevious();
            nworld--;
        }
        if (dec != null) {
            Deduction vr = dec.getPositiveDeduction();
            Deduction assign = dec.getNegativeDeduction();
            expl.remove(assign);
//            System.out.println("RecorderExplanationEngine.updateVRExplainUponbacktracking");
//            System.out.println("vr = " + vr + " expl " + why(expl));
            database.put(vr, why(expl));
        }
//        System.out.println("END");
    }

    @Override
    public void onContradiction(ContradictionException cex) {
//        System.out.println("RecorderExplanationEngine.onContradiction");
        if (cex.v != null) { // contradiction on domain wipe out
//            System.out.println("*** " + cex.v + " (" + cex.v.getClass() + ") has been wiped out");
            Explanation expl = cex.v.explain(Explanation.DOM);
//            System.out.println("expl = " + expl);
//            System.out.println("w(expl) = " + why(expl));
//            System.out.println(expl);
            cex.v.getSolver().getSearchLoop().overridePreviousWolrd(getMostRecentWorldToBacktrack(why(expl)));
        }
    }
}
