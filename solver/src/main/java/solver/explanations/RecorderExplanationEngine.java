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

import solver.ICause;
import solver.variables.IntVar;
import solver.variables.Variable;

import java.util.BitSet;
import java.util.HashMap;

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

    HashMap<Variable, BitSet> removedvalues; // maintien du domaine courant
    HashMap<Variable, HashMap<Integer, ValueRemoval>> valueremovals; // maintien de la base de deduction
    HashMap<Deduction, Explanation> database; // base d'explications


    public RecorderExplanationEngine() {
        removedvalues = new HashMap<Variable, BitSet>();
        valueremovals = new HashMap<Variable, HashMap<Integer, ValueRemoval>>();
        database = new HashMap<Deduction, Explanation>();
    }

    @Override
    public BitSet getRemovedValues(IntVar v) {
        BitSet toreturn = removedvalues.get(v);
        if (toreturn == null) {
            toreturn = new BitSet();
            removedvalues.put(v, toreturn);
            valueremovals.put(v, new HashMap<Integer, ValueRemoval>());
        }
        return toreturn;
    }

    @Override
    public Deduction explain(IntVar var, int val) {
        return getValueRemoval(var, val);
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
    public void removeValue(IntVar var, int val, ICause cause) {
        System.out.println("recording " + var + " - " + val);
        BitSet invdom = getRemovedValues(var);
        Deduction vr = getValueRemoval(var, val);
        database.put(vr, cause.explain(var, vr));
        invdom.set(val);
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int val, ICause cause) {
        System.out.println("recording " + var + " : " + old + " -> " + val);
        BitSet invdom = getRemovedValues(var);
        for (int v = old; v < val; v++) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
            database.put(vr, cause.explain(var, vr));
            invdom.set(v);
        }
    }

    @Override
    public void updateUpperBound(IntVar var, int old, int val, ICause cause) {
        System.out.println("recording " + var + " : " + old + " -> " + val);
        BitSet invdom = getRemovedValues(var);
        for (int v = old; v > val; v--) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
            database.put(vr, cause.explain(var, vr));
            invdom.set(v);
        }
    }


    @Override
    public void instantiateTo(IntVar var, int val, ICause cause) {
        System.out.println("recording " + var + " instantiated to " + val);

        BitSet invdom = getRemovedValues(var);
        int ub = var.getUB();
        for (int v = var.getLB(); v <= ub; v = var.nextValue(v)) {
            if (v != val) {
                Deduction vr = getValueRemoval(var, v);
                database.put(vr, cause.explain(var, vr));
                invdom.set(v);
            }
        }
    }
}
