package solver.explanations;

import choco.kernel.common.util.iterators.DisposableIntIterator;
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
 *
 * An RecorderExplanationEngine is used to record explanations throughout computation.
 * Here we just record the explanations in a HashMap ...
 *
 * TODO revise the way of recording in order to be able to retrieve information 
 *
 */
public class RecorderExplanationEngine extends ExplanationEngine {

    HashMap<Variable, BitSet> removedvalues; // maintien du domaine courant
    HashMap<Variable, HashMap<Integer, ValueRemoval>> valueremovals; // maintien de la base de deduction
    HashMap<Deduction, Explanation> database; // base d'explications


    public RecorderExplanationEngine(){
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
    public void removeValue(IntVar var, int val, ICause cause){
        System.out.println("recording " + var + " - " + val);
        BitSet invdom = getRemovedValues(var);
        Deduction vr = getValueRemoval(var, val);
        database.put(vr, cause.explain(var, vr));
        invdom.set(val);
    }

    @Override
    public void updateLowerBound(IntVar var, int old, int val, ICause cause) {
        System.out.println("recording " + var + " : " + old+ " -> " + val);
        BitSet invdom = getRemovedValues(var);
        for (int v = old; v < val; v++) {    // itération explicite des valeurs retirées
            Deduction vr = getValueRemoval(var, v);
            database.put(vr, cause.explain(var, vr));
            invdom.set(v);
        }
    }

    @Override
    public void updateUpperBound(IntVar var, int old, int val, ICause cause) {
        System.out.println("recording " + var + " : " + old+ " -> " + val);
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
        DisposableIntIterator it = var.getIterator();

        while (it.hasNext()) {
            int v = it.next();
            if (v != val) {
                Deduction vr = getValueRemoval(var, v);
                database.put(vr, cause.explain(var, vr));
                invdom.set(v);
            }
        }
    }
}
