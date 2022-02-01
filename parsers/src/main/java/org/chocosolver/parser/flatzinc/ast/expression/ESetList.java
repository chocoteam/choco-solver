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

import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 8 janv. 2010
* Since : Choco 2.1.1
*
* Class for identifier expressions definition based on flatzinc-like objects,
* defined with a list of EInt.
*/
public final class ESetList extends ESet {

    final int[] values;

    public ESetList(List<EInt> sl) {
        super(EType.SET_L);
        values = new int[sl.size()];
        for (int i = 0; i < sl.size(); i++) {
            values[i] = sl.get(i).value;
        }
    }

    @Override
    public int[] enumVal() {
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
