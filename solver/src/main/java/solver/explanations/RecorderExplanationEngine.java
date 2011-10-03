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

import choco.kernel.memory.IStateBitSet;
import solver.ICause;
import solver.Solver;
import solver.constraints.propagators.Propagator;
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


    public RecorderExplanationEngine(Solver solver) {
        super(solver);
        removedvalues = new HashMap<Variable, IStateBitSet>();
        valueremovals = new HashMap<Variable, HashMap<Integer, ValueRemoval>>();
        database = new HashMap<Deduction, Explanation>();
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
    public Deduction explain(IntVar var, int val) {
        ValueRemoval vr = getValueRemoval(var, val);
        return vr;
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
    public void removeValue(IntVar var, int val, ICause cause){
        IStateBitSet invdom = getRemovedValues(var);
        Deduction vr = getValueRemoval(var, val);
        database.put(vr, cause.explain(var, vr));
//        System.out.println("recording " + var + " - " + val + database.get(vr));
        invdom.set(val);
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
       int v = invdom.nextClearBit(0);
       while (v < invdom.size()) {
//           System.out.println("testing value " + v);
           if (v != val) {
                Deduction vr = getValueRemoval(var, v);
                database.put(vr, cause.explain(var, vr));
                invdom.set(v);
           }
           v = invdom.nextClearBit(v+1);
//           System.out.println("looking for value " + v);
       }
    }

    @Override
    public Explanation why(IntVar var, int val) {

        Explanation toreturn = new Explanation(null, null);

        if (var.contains(val)) return toreturn;


        Set<Deduction> toexpand = new HashSet<Deduction>();
        Set<Deduction> expanded = new HashSet<Deduction>();
        toexpand.add(getValueRemoval(var, val));

        while (! toexpand.isEmpty()) {
            Deduction d = toexpand.iterator().next();
            toexpand.remove(d);
            expanded.add(d);
            Explanation e = database.get(d);
//            System.out.println("Expanding d " + d);
            if (e != null) {
                if (e.contraintes != null) {
                    for (Propagator prop : e.contraintes) {
                        toreturn.add(prop);
                    }
                }
                if (e.deductions != null) {

                    for (Deduction ded : e.deductions){
                        if (! expanded.contains(ded)) {
//                            System.out.println("adding ded = " + ded);
                            toexpand.add(ded);
                        }
                    }
                }
            }
            else {
                toreturn.add(d);
            }
        }
        return toreturn; 
    }
}
