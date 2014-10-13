/**
 *  Copyright (c) 1999-2014, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package samples.nsp;

/*
* Created by IntelliJ IDEA.
* User: sofdem - sophie.demassey{at}emn.fr
* Date: Jul 30, 2010 - 9:10:40 AM
*/
public class NSChecker {

    NSData data;
    int[][] shifts;

    public NSChecker(NSData data) {
        this.data = data;
    }

    class CheckException extends Exception {
        public CheckException(String message) {
            super(message);
        }
    }

    public boolean checkSolution(int[][] shifts) {
        this.shifts = shifts;

        try {
            this.checkAssignments();
            this.checkPreAssignments();
            this.checkCovers();
            this.checkMonthlyCounters();
            this.checkWeeklyCounters();
            this.checkForbiddenPatterns();
        } catch (CheckException e) {
            System.err.println("Solution is not consistant: ");
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private void checkAssignments() throws CheckException {
        for (int[] shift : shifts) {
            for (int s : shift) {
                if (!(s >= 0 && s < data.nbActivities())) {
                    throw new CheckException("activity assignment failed");
                }
            }
        }
    }

    private void checkPreAssignments() throws CheckException {
        for (int[] trip : data.preAssignments()) {
            boolean mandatory = trip[0] > 0;
            int e = trip[1];
            int t = trip[2];
            int a = trip[3];
            if (mandatory) {
                if (shifts[e][t] != a) {
                    throw new CheckException("mandatory assignment failed");
                }
            } else {
                if (shifts[e][t] == a) {
                    throw new CheckException("forbidden assignment failed");
                }
            }
        }
    }

    private void checkCovers() throws CheckException {
        for (int t = 0; t < data.nbDays(); t++) {
            int[] cover = new int[data.nbActivities()];
            for (int e = 0; e < data.nbEmployees(); e++) {
                cover[shifts[e][t]]++;
            }
            for (int a = 0; a < cover.length; a++) {
                if (!(cover[a] >= data.getCoverLB(a) && cover[a] <= data.getCoverUB(a))) {
                    throw new CheckException("cover failed");
                }
            }
        }
    }

    private void checkMonthlyCounters() throws CheckException {
        for (int e = 0; e < data.nbEmployees(); e++) {
            int[] cover = new int[data.nbActivities()];
            for (int t = 0; t < data.nbDays(); t++) {
                cover[shifts[e][t]]++;
            }
            for (int a = 0; a < cover.length; a++) {
                if (!(cover[a] >= data.getCounterLB(e, a) && cover[a] <= data.getCounterUB(e, a))) {
                    throw new CheckException("monthly counter failed for employee " + e + "." +
                            "\n " + data.getCounterLB(e, a) + " <= " + cover[a] + " <= " + data.getCounterUB(e, a));
                }
            }
        }
    }

    private void checkWeeklyCounters() throws CheckException {
        for (int e = 0; e < data.nbEmployees(); e++) {
            for (int w = 0; w < data.nbWeeks(); w++) {
                int[] cover = new int[data.nbActivities()];
                for (int t = 0; t < 7; t++) {
                    cover[shifts[e][t + 7 * w]]++;
                }
                for (int a = 0; a < cover.length; a++) {
                    if (!(cover[a] >= data.getWeekCounterLB(e, a) && cover[a] <= data.getWeekCounterUB(e, a))) {
                        throw new CheckException("weekly counter failed");
                    }
                }
            }
        }
    }

    private void checkMaxWorkSpan() throws CheckException {
        for (int e = 0; e < data.nbEmployees(); e++) {
            int span = data.getMaxWorkSpan(e);
            int l = 0;
            for (int s : shifts[e]) {
                if (!data.isRestValue(s)) {
                    l++;
                    if (l > span) {
                        throw new CheckException("max work span failed");
                    }
                } else {
                    l = 0;
                }
            }
        }
    }

    private void checkForbiddenPatterns() throws CheckException {
        for (int[] shift : shifts) {
            for (String pat : data.forbiddenPatterns()) {
                char[] pattern = pat.toCharArray();
                int freq = pattern[0] - '0';
                if (freq >= 7) {
                    int i = 1;
                    for (int s : shift) {
                        if (data.isMatchedBy(s, pattern[i])) {
                            i++;
                            if (i >= pattern.length) {
                                throw new CheckException("forbidden pattern " + pat + " failed");
                            }
                        } else {
                            i = 1;
                        }
                    }
                } else {
                    for (int offset = freq; offset + pattern.length - 1 < data.nbWeeks(); offset += 7) {
                        boolean recognized = true;
                        for (int i = 1, t = offset; recognized && i < pattern.length; i++, t++) {
                            recognized = data.isMatchedBy(shift[t], pattern[i]);
                        }
                        if (recognized) {
                            throw new CheckException("forbidden pattern " + pat + " failed");
                        }
                    }

                }
            }
        }
    }


}
