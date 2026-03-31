/*
 * This file is part of choco-parsers, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast.declaration;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 7 janv. 2010
* Since : Choco 2.1.1
*
* Boolean declaration in flatzinc format, like 'float' or 'var float'.
*/
public final class DFloat extends Declaration {

    public static DFloat me = new DFloat();

    private DFloat() {
        super(DType.FLOAT);
    }

    @Override
    public String toString() {
        return "float";
    }
}
