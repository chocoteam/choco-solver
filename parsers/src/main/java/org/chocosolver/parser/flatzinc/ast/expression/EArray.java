/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.expression;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.SetVar;

import java.util.Collections;
import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for array expressions based on flatzinc-like objects.
*/
public final class EArray extends Expression {

    public final List<Expression> what;

    public EArray(List<Expression> what) {
        super(EType.ARR);
        this.what = what;
    }

    public EArray() {
        super(EType.ARR);
        this.what = Collections.emptyList();
    }

    public Expression getWhat_i(int i) {
        return what.get(i);
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder("[");
        st.append(what.get(0).toString());
        for (int i = 1; i < what.size(); i++) {
            st.append(',').append(what.get(i).toString());
        }
        return st.append(']').toString();
    }


    @Override
    public int[] toIntArray() {
        int[] arr = new int[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).intValue();
        }
        return arr;
    }

    @Override
    public int[][] toSetArray() {
        int[][] arr = new int[what.size()][];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).setValue();
        }
        return arr;
    }

    @Override
    public boolean[] toBoolArray() {
        boolean[] arr = new boolean[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).boolValue();
        }
        return arr;
    }

    @Override
    public BoolVar[] toBoolVarArray(Model model) {
        BoolVar[] arr = new BoolVar[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).boolVarValue(model);
        }
        return arr;
    }

    @Override
    public IntVar[] toIntVarArray(Model solver) {
        IntVar[] arr = new IntVar[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).intVarValue(solver);
        }
        return arr;
    }

    @Override
    public SetVar[] toSetVarArray(Model solver) {
        SetVar[] arr = new SetVar[what.size()];
        for (int i = 0; i < what.size(); i++) {
            arr[i] = what.get(i).setVarValue(solver);
        }
        return arr;
    }
}
