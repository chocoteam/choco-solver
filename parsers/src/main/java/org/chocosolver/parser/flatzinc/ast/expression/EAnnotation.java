/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
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
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for one annotation declaration based on flatsinc-like objects. 
*/
public final class EAnnotation extends Expression {

    public final EIdentifier id;
    public final List<Expression> exps;

    public EAnnotation(EIdentifier id, List<Expression> exps) {
        super(EType.ANN);
        this.id = id;
        this.exps = exps;
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder(id.value);
        if (exps != null && !exps.isEmpty()) {
            st.append('(').append(exps.get(0).toString());
            for (int i = 1; i < exps.size(); i++) {
                st.append(',').append(exps.get(i).toString());
            }
            st.append(')');
        }
        return st.toString();
    }
}
