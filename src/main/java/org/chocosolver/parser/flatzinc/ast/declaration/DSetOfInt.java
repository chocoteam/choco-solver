/**
 * This file is part of choco-parsers, https://github.com/chocoteam/choco-parsers
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
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
public final class DSetOfInt extends Declaration {

    public static DSetOfInt me = new DSetOfInt();

    private DSetOfInt() {
        super(DType.SETOFINT);
    }

    @Override
    public String toString() {
        return "set of int";
    }
}
