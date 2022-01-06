/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.declaration;

import org.chocosolver.parser.flatzinc.ast.expression.EInt;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 7 janv. 2010
* Since : Choco 2.1.1
*
* Declaration of int in flatzinc format, like '1..3' or 'var 2..6'.
*
*/
public final class DInt2 extends Declaration {

    final int low, upp;

    public DInt2(EInt v1, EInt v2) {
        super(DType.INT2);
        low = v1.value;
        upp = v2.value;
    }

    public int getLow() {
        return low;
    }

    public int getUpp() {
        return upp;
    }

    @Override
    public String toString() {
        return low + ".." + upp;
    }
}
