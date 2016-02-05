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
package org.chocosolver.solver.constraints.extension.nary;

import org.chocosolver.memory.IEnvironment;
import org.chocosolver.memory.IStateInt;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.iterators.DisposableValueIterator;
import org.chocosolver.util.objects.setDataStructures.swapList.Set_Std_Swap_Array;

import java.util.ArrayList;
import java.util.BitSet;
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
            str2vars[i] = new str2_var(model.getEnvironment(), vars_[i], i, table);
        }
        tuples = new Set_Std_Swap_Array(model.getEnvironment(), table.length);
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
                    vst.var.removeValue(value, this);
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
            v.remove_unsupported_value(this);
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
            index_map = new TreeMap<>();
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

        void remove_unsupported_value(ICause cause) throws ContradictionException {
            for (Entry<Integer, Integer> e : index_map.entrySet()) {
                if (var.contains(e.getKey()) && !GAC_Val.get(e.getValue())) {
                    var.removeValue(e.getKey(), cause);
                }
            }
        }
    }

}
