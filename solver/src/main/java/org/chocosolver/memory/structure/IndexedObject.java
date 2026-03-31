/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
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
