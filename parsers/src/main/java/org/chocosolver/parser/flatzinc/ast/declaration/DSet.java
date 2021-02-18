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

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 8 janv. 2010
* Since : Choco 2.1.1
*
* Declaration of set in flatzinc format, like 'set of what' or 'var set of what'.
*/
public final class DSet extends Declaration {

    public final Declaration what;

    public DSet(Declaration what) {
        super(DType.SET);
        this.what = what;
    }

    public Declaration getWhat() {
        return what;
    }

    @Override
    public String toString() {
        return "set of " + what.toString();
    }
}
