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

import java.util.List;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 8 janv. 2010
* Since : Choco 2.1.1
*
* Array-index declaration in flatzinc format, like 'array [index] of what'.
*/
public final class DArray extends Declaration {

    final List<Declaration> indices;
    final Declaration what;

    public DArray(List<Declaration> indices, Declaration what) {
        super(DType.ARRAY);
        this.indices = indices;
        this.what = what;
    }

    public int getDimension() {
        return indices.size();
    }

    public Declaration getIndex(int i) {
        return indices.get(i);
    }

    public Declaration getWhat() {
        return what;
    }

    @Override
    public String toString() {
        return "array [" + indices.toString() + "] of " + what.toString();
    }
}
