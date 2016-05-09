/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.allen;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.util.Arrays;
import java.util.BitSet;

/**
 * An interface defining minimal services for Allen's relations.
 * <br/>
 *
 * @author Charles Prud'homme
 * @version Allen
 * @since 01/10/2014
 */
public interface Allen {

    int relP = 0;
    int relM = 1;
    int relO = 2;
    int relS = 3;
    int relD = 4;
    int relF = 5;
    int relEQ = 6;
    int relPI = 7;
    int relMI = 8;
    int relOI = 9;
    int relSI = 10;
    int relDI = 11;
    int relFI = 12;
    int relCO = 13;


    String[] REL = new String[]{"p", "m", "o", "s", "d", "f", "eq", "pi", "mi", "oi", "si", "di", "fi"};

    static BitSet buildRelationsForMin(String[] rel) {
        BitSet declaredRelations = new BitSet(13);
        for (int i = 0; i < rel.length; i++) {
            switch (rel[i]) {
                case "p":
                    declaredRelations.set(relP);
                    break;
                case "m":
                    declaredRelations.set(relM);
                    break;
                case "o":
                    declaredRelations.set(relO);
                    break;
                case "s":
                    declaredRelations.set(relS);
                    break;
                case "d":
                    declaredRelations.set(relD);
                    break;
                case "f":
                    declaredRelations.set(relF);
                    break;
                case "eq":
                    declaredRelations.set(relEQ);
                    break;
                case "pi":
                    declaredRelations.set(relPI);
                    break;
                case "mi":
                    declaredRelations.set(relMI);
                    break;
                case "oi":
                    declaredRelations.set(relOI);
                    break;
                case "si":
                    declaredRelations.set(relSI);
                    break;
                case "di":
                    declaredRelations.set(relDI);
                    break;
                case "fi":
                    declaredRelations.set(relFI);
                    break;
                case "co":
                    declaredRelations.set(relCO);
                    break;
            }
        }
        return declaredRelations;
    }

    static BitSet buildRelationsForMax(String[] rel) {
        BitSet declaredRelations = new BitSet(13);
        for (int i = 0; i < rel.length; i++) {
            switch (rel[i]) {
                case "p":
                    declaredRelations.set(relPI);
                    break;
                case "pi":
                    declaredRelations.set(relP);
                    break;
                case "m":
                    declaredRelations.set(relMI);
                    break;
                case "mi":
                    declaredRelations.set(relM);
                    break;
                case "o":
                    declaredRelations.set(relOI);
                    break;
                case "oi":
                    declaredRelations.set(relO);
                    break;
                case "s":
                    declaredRelations.set(relF);
                    break;
                case "si":
                    declaredRelations.set(relFI);
                    break;
                case "d":
                    declaredRelations.set(relD);
                    break;
                case "di":
                    declaredRelations.set(relDI);
                    break;
                case "f":
                    declaredRelations.set(relS);
                    break;
                case "fi":
                    declaredRelations.set(relSI);
                    break;
                case "eq":
                    declaredRelations.set(relEQ);
                    break;
                case "co":
                    declaredRelations.set(relCO);
            }
        }
        return declaredRelations;
    }

    static IntVar buildDomainFromRelation(String[] rel, Model model){
        int[] values = new int[rel.length];
        int k =0;
        for (int i = 0; i < rel.length; i++) {
            switch (rel[i]) {
                case "p":
                    values[k++] = AllenRelationMats.b;
                    break;
                case "m":
                    values[k++] = AllenRelationMats.m;
                    break;
                case "o":
                    values[k++] = AllenRelationMats.o;
                    break;
                case "s":
                    values[k++] = AllenRelationMats.s;
                    break;
                case "d":
                    values[k++] = AllenRelationMats.d;
                    break;
                case "f":
                    values[k++] = AllenRelationMats.f;
                    break;
                case "eq":
                    values[k++] = AllenRelationMats.e;
                    break;
                case "pi":
                    values[k++] = AllenRelationMats.bi;
                    break;
                case "mi":
                    values[k++] = AllenRelationMats.mi;
                    break;
                case "oi":
                    values[k++] = AllenRelationMats.oi;
                    break;
                case "si":
                    values[k++] = AllenRelationMats.si;
                    break;
                case "di":
                    values[k++] = AllenRelationMats.di;
                    break;
                case "fi":
                    values[k++] = AllenRelationMats.fi;
                    break;
                case "co":
                    values[k++] = AllenRelationMats.fi;
                    values[k++] = AllenRelationMats.di;
                    values[k++] = AllenRelationMats.e;
                    values[k++] = AllenRelationMats.si;
                    break;
            }
        }
        Arrays.sort(values);
        return model.intVar("R", values);
    }


    /**
     * Search for the first interval matching the relation
     */
    int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m);

    boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end);

    void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end);


    public enum Relation implements Allen {
        P() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && elb[ti] >= sta2[tj]) {
                    tj++;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return elb < i_sta;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = slb;
                dates[1] = dlb;
                dates[2] = elb;
            }
        },
        M() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && elb[ti] > sta2[tj]) {
                    tj++;
                }
                if (tj < m && eub[ti] < sta2[tj]) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return elb <= i_sta && i_sta <= eub;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                // Apply deduction on end
                dates[2] = Math.min(dates[2], i_sta);
                // Back propagate on start
                dates[0] = Math.min(dates[0], Math.max(slb, i_sta - dub));
                // Back propagate on duration
                dates[1] = Math.min(dates[1], Math.max(dlb, i_sta - sub));
            }
        },
        O() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                if (dub[ti] == 1) {
                    tj = m;
                }
                while (tj < m && slb[ti] >= sta2[tj]) {
                    tj++;
                }
                while (tj < m && (elb[ti] >= end2[tj] || ((end2[tj] - sta2[tj]) == 1))) {
                    tj++;
                }
                if (tj < m && eub[ti] <= sta2[tj]) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return elb < i_end
                        && eub > i_sta && slb < i_sta
                        && dub > 1
                        && i_end - i_sta > 1;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                // Apply deduction on end
                dates[2] = Math.min(dates[2], i_sta + 1);
                // Back propagate on start
                dates[0] = Math.min(dates[0], Math.max(slb, (i_sta + 1) - dub));
                // Back propagate on duration
                dates[1] = Math.min(dates[1], Math.max(2, Math.max(dlb, (i_sta + 1) - sub)));
            }
        },
        S() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && slb[ti] > sta2[tj]) {
                    tj++;
                }
                boolean follow = true;
                while (follow && (tj < m && sub[ti] >= sta2[tj])) {
                    if (dlb[ti] >= (end2[tj] - sta2[tj])) {
                        tj++;
                    } else {
                        follow = false;
                    }
                }
                if (follow) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return slb <= i_sta && i_sta <= sub
                        && dlb < (i_end - i_sta)
                        && elb < i_end;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], i_sta);
                dates[1] = dlb; // Math.min(dates[1], ...) is useless
                dates[2] = Math.min(dates[2], i_sta + dlb);
            }
        },
        D() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                if (tj < m && sub[ti] < sta2[tj] + 1) {   // if true once then it should get m directly
                    tj = m;
                }
                boolean follow = true;
                while (follow && (tj < m && (sub[ti] >= (sta2[tj] + 1)))) {
                    if (elb[ti] > (end2[tj] - 1)) {
                        tj++;
                    } else {
                        if (dlb[ti] > ((end2[tj] - sta2[tj]) - 2)) {
                            tj++;
                        } else {
                            follow = false;
                        }
                    }
                }
                if (follow) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return sub > i_sta
                        && elb < i_end
                        && dlb < (i_end - i_sta - 1);
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], i_sta + 1);
                dates[1] = dlb; // Math.min(dates[1], ...) is useless
                dates[2] = Math.min(dates[2], i_sta + 1 + dlb);
            }
        },
        F() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && elb[ti] > end2[tj]) {
                    tj++;
                }
                boolean follow = true;
                while (follow && (tj < m && eub[ti] >= end2[tj])) {
                    if (dlb[ti] >= (end2[tj] - sta2[tj])) {
                        tj++;
                    } else {
                        follow = false;
                    }
                }
                if (follow) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return sub > i_sta
                        && elb <= i_end && i_end <= eub
                        && dlb < (i_end - i_sta);
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[2] = Math.min(dates[2], i_end);
                dates[1] = Math.min(dates[1], Math.max(dlb, i_end - sub));
                dates[0] = Math.min(dates[0], Math.max(slb, Math.max(i_sta + 1, i_end - dub)));
            }
        },
        PI() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while ((tj < m) && (sub[ti] <= end2[tj])) {
                    tj++;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return sub > i_end;       // BEWARE : MUST be called with interval #1 !!!
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                // new support for start
                int sup4sta = Math.max(slb, i_end + 1);
                dates[0] = Math.min(dates[0], sup4sta);
                dates[1] = dlb;
                dates[2] = Math.min(dates[2], Math.max(elb, sup4sta + dlb));
            }
        },
        MI() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && slb[ti] > end2[tj]) {
                    tj++;
                }
                if (tj < m && sub[ti] < end2[tj]) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return slb <= i_end && i_end <= sub;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], i_end);
                dates[1] = dlb;
                dates[2] = Math.min(dates[2], i_end + dlb);
            }
        },
        OI() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                if (dub[ti] == 1) {
                    tj = m;
                }
                while (tj < m && slb[ti] >= end2[tj]) {
                    tj++;
                }
                while (tj < m && (eub[ti] <= end2[tj] || ((end2[tj] - sta2[tj]) == 1))) {
                    tj++;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return sub > i_sta && slb < i_end
                        && eub > i_end
                        && dub > 1
                        && i_end - i_sta > 1;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], Math.max(slb, i_sta + 1));
                // new support for end
                int sup4end = Math.max(elb, i_end + 1);
                dates[2] = Math.min(dates[2], sup4end);
                dates[1] = Math.min(dates[1], Math.max(2, Math.max(dlb, sup4end - sub)));
            }
        },
        SI() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && slb[ti] > sta2[tj]) {
                    tj++;
                }
                boolean follow = true;
                while (follow && (tj < m && sub[ti] >= sta2[tj])) {
                    if (dub[ti] <= (end2[tj] - sta2[tj])) {
                        tj++;
                    } else {
                        follow = false;
                    }
                }
                if (follow) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return slb <= i_sta && i_sta <= sub
                        && dub > i_end - i_sta
                        && eub > i_end;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], i_sta);
                dates[1] = Math.min(dates[1], i_end - i_sta + 1);
                dates[2] = Math.min(dates[2], Math.max(elb, i_end + 1));
            }
        },
        DI() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                if (eub[ti] < end2[tj] + 1) {
                    tj = m;
                }
                boolean follow = true;
                while (follow && (tj < m && (eub[ti] >= (end2[tj] + 1)))) {
                    if (slb[ti] > (sta2[tj] - 1)) {
                        tj++;
                    } else {
                        if (dub[ti] < ((end2[tj] - sta2[tj]) + 2)) {
                            tj++;
                        } else {
                            follow = false;
                        }
                    }
                }
                if (follow) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return slb < i_sta
                        && eub > i_end
                        && dub > i_end - i_sta + 1;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                // new support for end
                int sup4end = Math.max(elb, i_end + 1);
                dates[2] = Math.min(dates[2], sup4end);
                dates[0] = Math.min(dates[0], Math.max(slb, sup4end - dub));
                dates[1] = Math.min(dates[1], Math.max(dlb, sup4end - Math.min(sub, i_sta - 1)));
            }
        },
        FI() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && elb[ti] > end2[tj]) {
                    tj++;
                }
                boolean follow = true;
                while (follow && (tj < m && eub[ti] >= end2[tj])) {
                    if (dub[ti] <= (end2[tj] - sta2[tj])) {
                        tj++;
                    } else {
                        follow = false;
                    }
                }
                if (follow) {
                    tj = m;
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return elb <= i_end && i_end <= eub
                        && slb < i_sta
                        && dub > i_end - i_sta;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[2] = Math.min(dates[2], i_end);
                dates[0] = Math.min(dates[0], Math.max(slb, i_end - dub));
                dates[1] = Math.min(dates[1], Math.max(dlb, i_end - Math.min(sub, i_sta - 1)));
            }
        },
        EQ() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                while (tj < m && slb[ti] > sta2[tj]) {
                    tj++;
                }
                if (tj < m) {
                    int dur2 = (end2[tj] - sta2[tj]);
                    while (tj < m && (dlb[ti] > dur2 || dub[ti] < dur2)) {
                        tj++;
                        if (tj < m) {
                            dur2 = (end2[tj] - sta2[tj]);
                        }
                    }
                    if (tj < m && (elb[ti] > end2[tj] || eub[ti] < end2[tj])) {
                        tj = m;
                    }
                }
                return tj;
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return slb <= i_sta && i_sta <= sub
                        && elb <= i_end && i_end <= eub
                        && dlb <= i_end - i_sta && i_end - i_sta <= dub;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], i_sta);
                dates[1] = Math.min(dates[1], i_end - i_sta);
                dates[2] = Math.min(dates[2], i_end);
            }
        },
        CO() {
            @Override
            public int firstInterval(int ti, int tj, int[] slb, int[] sub, int[] dlb, int[] dub, int[] elb, int[] eub, int[] sta2, int[] end2, int m) {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean condition(int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                return slb <= i_sta
                        && eub >= i_end
                        && dub >= i_end - i_sta;
            }

            @Override
            public void lowerBounds(int[] dates, int slb, int sub, int dlb, int dub, int elb, int eub, int i_sta, int i_end) {
                dates[0] = Math.min(dates[0], Math.max(slb, i_end - dub));
                dates[1] = Math.min(dates[1], i_end - i_sta);
                dates[2] = Math.min(dates[2], Math.max(elb, i_end));
            }
        }

    }

}
