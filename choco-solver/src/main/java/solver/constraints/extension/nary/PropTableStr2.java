package solver.constraints.extension.nary;

import gnu.trove.map.hash.THashMap;
import memory.IEnvironment;
import memory.IStateInt;
import solver.Solver;
import solver.constraints.Propagator;
import solver.constraints.PropagatorPriority;
import solver.exception.ContradictionException;
import solver.variables.IntVar;
import util.ESat;
import util.iterators.DisposableValueIterator;
import util.objects.setDataStructures.swapList.Set_Std_Swap_Array;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * STR2 Propagator for table constraints (only positive tuples)
 *
 * @author Guillaume Perez, Jean-Guillaume Fages (minor)
 * @since 26/07/2014
 */
public class PropTableStr2 extends Propagator<IntVar> {

    //***********************************************************************************
    // VARIABLES
    //***********************************************************************************

    int[][] table;
    str2_var str2vars[];
    Set_Std_Swap_Array tuples;
    ArrayList<str2_var> Ssup;
    ArrayList<str2_var> Sval;
    boolean firstProp = true;

    //***********************************************************************************
    // CONSTRUCTOR
    //***********************************************************************************

    public PropTableStr2(IntVar[] vars_, int[][] table) {
        super(vars_, PropagatorPriority.LINEAR, false);
        str2vars = new str2_var[table[0].length];
        for (int i = 0; i < table[0].length; i++) {
            str2vars[i] = new str2_var(solver.getEnvironment(), vars_[i], i, table);
        }
        tuples = new Set_Std_Swap_Array(solver.getEnvironment(), table.length);
        Ssup = new ArrayList<>();
        Sval = new ArrayList<>();
        this.table = table;
    }

    //***********************************************************************************
    // PROP METHODS
    //***********************************************************************************

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (firstProp) {
            firstProp = false;
            initialPropagate();
        }
        Filter();
    }

    @Override
    public ESat isEntailed() {
        boolean hasSupport = false;
        for (int tuple = tuples.getFirstElement(); tuple >= 0 && !hasSupport; tuple = tuples.getNextElement()) {
            if (is_tuple_supported(tuple)) {
                hasSupport = true;
            }
        }
        if (hasSupport) {
            if (isCompletelyInstantiated()) {
                return ESat.TRUE;
            } else {
                return ESat.UNDEFINED;
            }
        } else {
            return ESat.FALSE;
        }
    }

    @Override
    public String toString() {
        return "STR2 table constraint with " + table[0].length + "vars and " + table.length + "tuples";
    }

    //***********************************************************************************
    // DEDICATED METHODS
    //***********************************************************************************

    boolean is_tuple_supported(int tuple_index) {
        for (str2_var v : Sval) {
            if (!v.var.contains(table[tuple_index][v.indice])) {
                return false;
            }
        }
        return true;
    }

    void initialPropagate() throws ContradictionException {
        for (str2_var vst : str2vars) {
            DisposableValueIterator vit = vst.var.getValueIterator(true);
            while (vit.hasNext()) {
                int value = vit.next();
                if (!vst.index_map.containsKey(value)) {
                    vst.var.removeValue(value, aCause);
                }
            }
            vit.dispose();
        }
        for (int t = 0; t < table.length; t++) {
            tuples.add(t);
        }
    }

    void Filter() throws ContradictionException {
        Ssup.clear();
        Sval.clear();
        for (str2_var tmp : str2vars) {
            tmp.GAC_clear();
            Ssup.add(tmp);
            if (tmp.last_size.get() != tmp.var.getDomainSize()) {
                Sval.add(tmp);
                tmp.last_size.set(tmp.var.getDomainSize());
            }
        }
        for (int tuple = tuples.getFirstElement(); tuple >= 0; tuple = tuples.getNextElement()) {
            if (is_tuple_supported(tuple)) {
                for (int var = 0; var < Ssup.size(); var++) {
                    str2_var v = Ssup.get(var);
                    if (!v.isConsistant(table[tuple][v.indice])) {
                        v.makeConsistant(table[tuple][v.indice]);
                        if (v.nb_consistant == v.var.getDomainSize()) {
                            Ssup.set(var, Ssup.get(Ssup.size() - 1));
                            Ssup.remove(Ssup.size() - 1);
                            var--;
                        }
                    }
                }
            } else {
                tuples.remove(tuple);
            }
        }
        for (str2_var v : Ssup) {
            v.remove_unsupported_value();
        }
    }

    /**
     * var class which will save local var information
     */
    class str2_var {

        IntVar var;
        /**
         * original var
         */
        int indice;
        /**
         * index in the table
         */
        IStateInt last_size;
        /**
         * Numerical reversible of the last size
         */
        BitSet GAC_Val;
        /**
         * at each step, it will say if the value is GAC
         */
        int nb_consistant;
        /**
         * count the number of consistant value
         */
        TreeMap<Integer, Integer> index_map;

        /**
         * contains all the value of the variable
         */

        str2_var(IEnvironment env, IntVar var_, int indice_, int[][] table) {
            var = var_;
            last_size = env.makeInt(0);
            indice = indice_;
            nb_consistant = 0;
            index_map = new TreeMap<Integer, Integer>();
            int key = 0;
            for (int[] t : table) {
                if (!index_map.containsKey(t[indice])) {
                    index_map.put(t[indice], key++);
                }
            }
            GAC_Val = new BitSet(index_map.size());
        }

        void GAC_clear() {
            GAC_Val.clear();
            nb_consistant = 0;
        }

        boolean isConsistant(int value) {
            return GAC_Val.get(index_map.get(value));
        }

        void makeConsistant(int value) {
            GAC_Val.set(index_map.get(value));
            nb_consistant++;
        }

        void remove_unsupported_value() throws ContradictionException {
            Iterator<Entry<Integer, Integer>> it = index_map.entrySet().iterator();
            while (it.hasNext()) {
                Entry<Integer, Integer> e = it.next();
                if (var.contains(e.getKey()) && !GAC_Val.get(e.getValue())) {
                    var.removeValue(e.getKey(), aCause);
                }
            }
        }
    }

    @Override
    public void duplicate(Solver solver, THashMap<Object, Object> identitymap) {
        if (!identitymap.containsKey(this)) {
            int size = this.vars.length;
            IntVar[] aVars = new IntVar[size];
            for (int i = 0; i < size; i++) {
                this.vars[i].duplicate(solver, identitymap);
                aVars[i] = (IntVar) identitymap.get(this.vars[i]);
            }
            identitymap.put(this, new PropTableStr2(aVars, this.table.clone()));
        }
    }
}
