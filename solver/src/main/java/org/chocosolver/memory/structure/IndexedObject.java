/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory.structure;

/**
 * Interface to manipulate object as integers in some StoredData Structures :
 * e.g : StoredIndexedBipartiteSet
 */
public interface IndexedObject extends Cloneable {

    int getObjectIdx();

    IndexedObject clone() throws CloneNotSupportedException;

}
