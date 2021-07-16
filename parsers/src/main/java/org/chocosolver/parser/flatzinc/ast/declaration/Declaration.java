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
* Date : 7 janv. 2010
* Since : Choco 2.1.1
*
* Declaration defined type for parameter and variable
* in flatzinc format.
*
*/
public abstract class Declaration {

    public enum DType {
        BOOL, FLOAT, INT, SETOFINT, ARRAY, SET, INT2, INTN
    }

    public final DType typeOf;


    protected Declaration(DType type) {
        this.typeOf = type;
    }
}
