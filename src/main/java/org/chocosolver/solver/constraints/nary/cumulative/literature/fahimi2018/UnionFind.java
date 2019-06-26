/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.fahimi2018;

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class UnionFind<T> {
    protected final Map<T, T> parentMap = new LinkedHashMap();
    protected final TObjectIntHashMap rankMap = new TObjectIntHashMap();
    protected int count;

    protected UnionFind() {

    }

    public UnionFind(Set<T> elements) {
        Iterator<T> var2 = elements.iterator();

        while(var2.hasNext()) {
            T element = var2.next();
            this.parentMap.put(element, element);
            this.rankMap.put(element, 0);
        }

        this.count = elements.size();
    }

    public void addElement(T element) {
        if (this.parentMap.containsKey(element)) {
            throw new IllegalArgumentException("element is already contained in UnionFind: " + element);
        } else {
            this.parentMap.put(element, element);
            this.rankMap.put(element, 0);
            ++this.count;
        }
    }

    protected Map<T, T> getParentMap() {
        return this.parentMap;
    }

    protected TObjectIntHashMap getRankMap() {
        return this.rankMap;
    }

    public T find(T element) {
        if (!this.parentMap.containsKey(element)) {
            throw new IllegalArgumentException("element is not contained in this UnionFind data structure: " + element);
        } else {
            T current = element;

            while(true) {
                T root = this.parentMap.get(current);
                if (root.equals(current)) {
                    root = current;

                    T parent;
                    for(current = element; !current.equals(root); current = parent) {
                        parent = this.parentMap.get(current);
                        this.parentMap.put(current, root);
                    }

                    return root;
                }

                current = root;
            }
        }
    }

    public void union(T element1, T element2) {
        if (this.parentMap.containsKey(element1) && this.parentMap.containsKey(element2)) {
            T parent1 = this.find(element1);
            T parent2 = this.find(element2);
            if (!parent1.equals(parent2)) {
                int rank1 = this.rankMap.get(parent1);
                int rank2 = this.rankMap.get(parent2);
                if (rank1 > rank2) {
                    this.parentMap.put(parent2, parent1);
                } else if (rank1 < rank2) {
                    this.parentMap.put(parent1, parent2);
                } else {
                    this.parentMap.put(parent2, parent1);
                    this.rankMap.put(parent1, rank1 + 1);
                }

                --this.count;
            }
        } else {
            throw new IllegalArgumentException("elements must be contained in given set");
        }
    }

    public boolean inSameSet(T element1, T element2) {
        return this.find(element1).equals(this.find(element2));
    }

    public int numberOfSets() {
        assert this.count >= 1 && this.count <= this.parentMap.keySet().size();

        return this.count;
    }

    public int size() {
        return this.parentMap.size();
    }

    public void reset() {
        Iterator<T> var1 = this.parentMap.keySet().iterator();

        while(var1.hasNext()) {
            T element = var1.next();
            this.parentMap.put(element, element);
            this.rankMap.put(element, 0);
        }

        this.count = this.parentMap.size();
    }

    public String toString() {
        Map<T, Set<T>> setRep = new LinkedHashMap();

        T t;
        T representative;
        for(Iterator<T> var2 = this.parentMap.keySet().iterator(); var2.hasNext(); setRep.get(representative).add(t)) {
            t = var2.next();
            representative = this.find(t);
            if (!setRep.containsKey(representative)) {
                setRep.put(representative, new LinkedHashSet());
            }
        }

        return setRep.keySet().stream().map((key) -> {
            return "{" + key + ":" + ((Set)setRep.get(key)).stream().map(Objects::toString).collect(Collectors.joining(",")) + "}";
        }).collect(Collectors.joining(", ", "{", "}"));
    }
}
