/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 01/06/12
 */
public interface Indexable<K> {

    /**
     * Return the index of <code>this</code> in <code>variable</code>
     *
     * @param key the key element, must be a known <code>this</code>
     * @return index index of <code>this</code> in <code>variable</code> list of event recorder
     */
    int getIdx(K key);

    /**
     * Return the index of <code>this</code> in <code>variable</code>
     *
     * @param key a key element, must be a known <code>this</code>
     * @param idx index of <code>this</code> in <code>variable</code> list of event recorder
     */
    void setIdx(K key, int idx);
}
