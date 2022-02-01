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
import org.chocosolver.solver.variables.SetVar;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 12 janv. 2010
* Since : Choco 2.1.1
*
* Class for set expressions definition based on flatzinc-like objects.
*/
public abstract class ESet extends Expression {

    protected ESet(EType typeOf) {
        super(typeOf);
    }

    public abstract int[] enumVal();


    @Override
    public final int[] toIntArray() {
        return enumVal();
    }

    @Override
    public final SetVar setVarValue(Model model) {
        int[] values = enumVal();
        return model.setVar(model.generateName("set_const"), values);
    }

    //
    @Override
    public final SetVar[] toSetVarArray(Model solver) {
        return new SetVar[]{setVarValue(solver)};
    }
}
