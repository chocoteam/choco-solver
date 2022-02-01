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

/*
* User : CPRUDHOM
* Mail : cprudhom(a)emn.fr
* Date : 11 janv. 2010
* Since : Choco 2.1.1
*
* Class for string expressions definition based on flatzinc-like objects.
*/
public final class EString extends Expression {

    public final String st;

    public EString(String st) {
        super(EType.STR);
        this.st = st;
    }

    @Override
    public String toString() {
        return st;
    }
}
