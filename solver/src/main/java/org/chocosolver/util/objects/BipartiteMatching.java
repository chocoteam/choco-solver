/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2024, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;


/**
 * Stores a matching in a bipartite graph G = (U,V,E)
 * The set of vertices U and V are intervals of integers
 * The matching is implemented as an array of integers
 * We distinguish the operations over U and V
 * @author Sulian Le Bozec-Chiffoleau
 * @since 17 Oct. 2024
 */

public class BipartiteMatching {
    private final int minU;
    private final int maxU;
    private final int sizeU; 
    private final int minV;
    private final int maxV;
    private final int sizeV;
    private final int unmatched;
    private final int[] matchingU;
    private final int[] matchingV;
    private int size;

    public BipartiteMatching(int minU, int maxU, int minV, int maxV) {
        this.minU = minU;
        this.maxU = maxU;
        this.sizeU = maxU - minU + 1;
        this.minV = minV;
        this.maxV = maxV;
        this.sizeV = maxV - minV + 1;
        this.unmatched = minU < minV ? minU - 1 : minV - 1;
        this.matchingU = new int[sizeU];
        for (int i = 0; i < sizeU; i++) {
            matchingU[i] = unmatched;
        }
        this.matchingV = new int[sizeV];
        for (int i = 0; i < sizeV; i++) {
            matchingV[i] = unmatched;
        }
        this.size = 0;
    }

    public int getSizeU() {return this.sizeU;}

    public int getSizeV() {return this.sizeV;}

    public boolean inMatchingU(int u) {
        return matchingU[u - minU] != unmatched;
    }

    public boolean inMatchingV(int v) {
        return matchingV[v - minV] != unmatched;
    }

    public int getMatchU(int u) {
        if (!inMatchingU(u)) {
            throw new Error("Error: This vertex is not matched");
        }
        return matchingU[u - minU];
    }

    public int getMatchV(int v) {
        if (!inMatchingV(v)) {
            throw new Error("Error: This vertex is not matched");
        }
        return matchingV[v - minV];
    }

    public void setMatch(int u, int v) {
        if (inMatchingU(u) || inMatchingV(v)) {
            throw new Error("Error: The vertices you want to match are already matched");
        }
        matchingU[u - minU] = v;
        matchingV[v - minV] = u;
        size++;
    }

    public void unMatch(int u, int v) {
        if (!inMatchingU(u) || !inMatchingV(v) || getMatchU(u) != v || getMatchV(v) != u) {
            throw new Error("Error: The pair you want to unmatch is not part of the matching");
        }
        matchingU[u - minU] = unmatched;
        matchingV[v - minV] = unmatched;
        size--;
    }

    public int getSize() {
        return size;
    }

    public boolean isMaximum() {
        return (size == sizeU) || (size == sizeV);
    }

    public boolean isValid() {
        boolean[] scanned = new boolean[sizeV];
        int count = 0;
        for (int u = minU; u <= maxU; u++) {
            if (inMatchingU(u)) {
                count++;
                int v = getMatchU(u);
                if (scanned[v - minV] || getMatchV(v) != u) {return false;}
                scanned[v - minV] = true;
            }
        }
        for (int v = minV; v <= maxV; v++) {
            if (inMatchingV(v)) {
                count--;
            }
        }
        return count == 0;
    }

    @Override
    public String toString() {
        String printedString = "";
        for (int u = minU; u <= maxU; u++) {
            if (inMatchingU(u)) {
                printedString += u + " <--> " + getMatchU(u) + '\n';
            }
        }
        return printedString;
    }

    public boolean equals(BipartiteMatching otherMatching) {
        if (sizeU != otherMatching.getSizeU() || sizeV != otherMatching.getSizeV()) {return false;}
        for (int u = minU; u <= maxU; u++) {
            if ((inMatchingU(u) && !otherMatching.inMatchingU(u)) || (!inMatchingU(u) && otherMatching.inMatchingU(u)) || getMatchU(u) != otherMatching.getMatchU(u)) {
                return false;
            }
        }
        return true;
    }
}
