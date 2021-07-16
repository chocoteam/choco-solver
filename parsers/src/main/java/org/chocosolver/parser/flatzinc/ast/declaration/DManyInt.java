/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.declaration;

import org.chocosolver.parser.flatzinc.ast.expression.EInt;

import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 7 janv. 2010
* Since : Choco 2.1.1
*
* Declaration of int list in flatzinc format, like '{2,3,5,7}' or 'var {2,4,6,8}'.
*/
public final class DManyInt extends Declaration {

    final int[] values;

    public DManyInt(List<EInt> values) {
        super(DType.INTN);
        this.values = new int[values.size()];
        for (int i = 0; i < values.size(); i++) {
            this.values[i] = values.get(i).value;
        }
    }

    public int[] getValues() {
        return values;
    }

    @Override
    public String toString() {
        StringBuilder bf = new StringBuilder("{");
        if (values.length > 0) {
            bf.append(values[0]);
            for (int i = 1; i < values.length; i++) {
                bf.append(',').append(values[i]);
            }
        }
        return bf.append('}').toString();
    }
}
