/*
 * This file is part of choco-parsers, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.parser.flatzinc.ast;

import org.chocosolver.parser.flatzinc.ast.declaration.Declaration;

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for Predicate parameter defintion based on flatzinc-like objects.
*/
public final class FPredParam {

    final Declaration type;
    final String identifier;

    public FPredParam(Declaration type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    @Override
    public String toString() {
        return type.toString() + ": " + identifier;
    }
}
