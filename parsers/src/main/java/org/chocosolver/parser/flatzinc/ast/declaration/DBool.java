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

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 7 janv. 2010
* Since : Choco 2.1.1
*
* Boolean declaration in flatzinc format, like 'bool' or 'var bool'.
*/
public final class DBool extends Declaration {

    public static DBool me = new DBool();

    private DBool() {
        super(DType.BOOL);
    }

    @Override
    public String toString() {
        return "bool";
    }
}
