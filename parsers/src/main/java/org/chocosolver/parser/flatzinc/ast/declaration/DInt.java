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
* Declaration of int in flatzinc format, like 'int' or 'var int'.
*/
public final class DInt extends Declaration {

    public static DInt me = new DInt();

    private DInt() {
        super(DType.INT);
    }

    @Override
    public String toString() {
        return "int";
    }
}
