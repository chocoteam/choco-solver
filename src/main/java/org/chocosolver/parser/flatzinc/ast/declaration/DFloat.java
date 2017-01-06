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
