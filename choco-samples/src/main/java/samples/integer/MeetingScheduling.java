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
package samples.integer;

import org.kohsuke.args4j.Option;
import org.slf4j.LoggerFactory;
import samples.AbstractProblem;
import solver.Solver;
import solver.constraints.IntConstraintFactory;
import solver.variables.IntVar;
import solver.variables.VariableFactory;
import util.ESat;

import java.util.Arrays;
import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * <a href="http://www.cs.st-andrews.ac.uk/~ianm/CSPLib/prob/prob046/">CSPLib prob046</a>:
 * "Informally, a set of agents want to meet. They search for a feasible meeting time that satisfieses
 * the private constraints of each of the agents and in addition satisfies arrival-time constraints
 * (among different meetings of the same agent). "
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 04/03/13
 */
public class MeetingScheduling extends AbstractProblem {

    private static final String groupSeparator = "\\,";
    private static final String decimalSeparator = "\\.";
    private static final String non0Digit = "[\\p{javaDigit}&&[^0]]";
    private static Pattern decimalPattern;

    static {
        // \\p{javaDigit} may not be perfect, see above
        String digit = "([0-9])";
        String groupedNumeral = "(" + non0Digit + digit + "?" + digit + "?(" +
                groupSeparator + digit + digit + digit + ")+)";
        // Once again digit++ is used for performance, as above
        String numeral = "((" + digit + "++)|" + groupedNumeral + ")";
        String decimalNumeral = "(" + numeral + "|" + numeral +
                decimalSeparator + digit + "*+|" + decimalSeparator +
                digit + "++)";
        String decimal = "([-+]?" + decimalNumeral + ")";
        decimalPattern = Pattern.compile(decimal);
    }


    @Option(name = "-d", usage = "Meeting Scheduling data.", required = false)
    Data mData = Data.instance6;

    // DATA
    private class MSPData {

        int numberOfMeetings = 4;
        int numberOfAgents = 4;
        int domainSize = 5;
        int numberOfMeetingPerAgent = 3;
        int minDisTimeBetweenMeetings = 1;
        int maxDisTimeBetweenMeetings = 3;

        int[][] agentMeetings = {
                {0, 2, 3},  // Agent 0
                {1, 3},     // Agent 1
                {0, 1},     // Agent 2
                {1, 2},     // Agent 3
        };

        int[][] betweenMeetingsDistance = {
                {0, 1, 1, 2},
                {1, 0, 3, 2},
                {1, 3, 0, 2},
                {2, 2, 2, 0},
        };
    }

    MSPData mspdata;

    // VARIABLES

    IntVar[] meetingTime;

    @Override
    public void createSolver() {
        solver = new Solver("MSP");
    }

    @Override
    public void buildModel() {
        mspdata = parse(mData.source());
        meetingTime = VariableFactory.enumeratedArray("ts", mspdata.numberOfMeetings, 0, mspdata.domainSize - 1, solver);
        boolean[][] conflicts = new boolean[mspdata.numberOfMeetings][mspdata.numberOfMeetings];
        for (int i = 0; i < mspdata.numberOfAgents; i++) { // for each pair of meeting
            for (int j = 0; j < mspdata.agentMeetings[i].length; j++) {
                int m1 = mspdata.agentMeetings[i][j];
                for (int k = j + 1; k < mspdata.agentMeetings[i].length; k++) {
                    int m2 = mspdata.agentMeetings[i][k];
                    if (m1 > -1 && m2 > -1) {
                        conflicts[m1][m2] = true;
                        conflicts[m2][m1] = true;
                    }
                }
            }
        }

        for (int i = 0; i < mspdata.numberOfMeetings - 1; i++) { // for each pair of meeting
            for (int j = i + 1; j < mspdata.numberOfMeetings; j++) {
                if (conflicts[i][j]) {
                    solver.post(IntConstraintFactory.distance(meetingTime[i], meetingTime[j], ">", mspdata.betweenMeetingsDistance[i][j]));
                }
            }
        }

    }

    @Override
    public void configureSearch() {
    }

    @Override
    public void solve() {
        solver.findSolution();
    }

    @Override
    public void prettyOut() {
        LoggerFactory.getLogger("bench").info("Meeting Scheduling Problem ({})", mData);

        StringBuilder st = new StringBuilder();
        if (solver.isFeasible() != ESat.TRUE) {
            st.append("\tINFEASIBLE");
        } else {
            for (int i = 0; i < mspdata.numberOfMeetings; i++) {
                st.append("Meeting ").append(i).append(" scheduled at time ").append(meetingTime[i].getValue()).append("\n");
            }
        }
        LoggerFactory.getLogger("bench").info(st.toString());
    }

    public static void main(String[] args) {
        new MeetingScheduling().execute(args);
    }

    private MSPData parse(String source) {
        Scanner sc = new Scanner(source);
        mspdata = new MSPData();
        mspdata.numberOfMeetings = sc.nextInt();
        sc.nextLine();
        mspdata.numberOfAgents = sc.nextInt();
        sc.nextLine();
        mspdata.numberOfMeetingPerAgent = sc.nextInt();
        sc.nextLine();
        mspdata.minDisTimeBetweenMeetings = sc.nextInt();
        sc.nextLine();
        mspdata.maxDisTimeBetweenMeetings = sc.nextInt();
        sc.nextLine();
        mspdata.domainSize = sc.nextInt();
        sc.nextLine();
        mspdata.agentMeetings = new int[mspdata.numberOfAgents][mspdata.numberOfMeetingPerAgent];
        for (int i = 0; i < mspdata.numberOfAgents; i++) {
            Arrays.fill(mspdata.agentMeetings[i], -1);
            int j = 0;
            while (j < mspdata.numberOfMeetingPerAgent) {
                mspdata.agentMeetings[i][j++] = sc.nextInt();
            }
            sc.nextLine();
        }
        mspdata.betweenMeetingsDistance = new int[mspdata.numberOfMeetings][mspdata.numberOfMeetings];
        for (int i = 0; i < mspdata.numberOfMeetings; i++) {
            for (int j = 0; j < mspdata.numberOfMeetings; j++) {
                mspdata.betweenMeetingsDistance[i][j] = sc.nextInt();
            }
            sc.nextLine();
        }
        sc.close();
        return mspdata;
    }

    static enum Data {
        small(
                "4\n" +  //nb of meetings
                        "4\n" +     // nb of agents
                        "3\n" +     // nb of meeting per agent
                        "1\n" +     // min dist time btw meetings
                        "3\n" +     // max dist time btw meetings
                        "8\n" +     // domain size
                        "0 2 3\n" + // agents meetings
                        "1 3 -1\n" +
                        "0 1 -1\n" +
                        "1 2 -1\n" +
                        "0 1 1 2\n" +   // between meetings distance
                        "1 0 3 2\n" +
                        "1 3 0 2\n" +
                        "2 2 2 0\n"
        ),
        instance1(
                "20\n" +
                        "9\n" +
                        "5\n" +
                        "1\n" +
                        "2\n" +
                        "12\n" +
                        "6 7 15 17 18\n" +
                        "1 3 14 15 16\n" +
                        "5 6 10 13 15\n" +
                        "8 11 15 17 18\n" +
                        "1 4 7 9 16\n" +
                        "0 1 11 13 18\n" +
                        "2 5 12 13 18\n" +
                        "2 6 8 12 16\n" +
                        "2 7 15 17 19\n" +
                        "0 1 2 1 2 2 1 2 1 2 2  1  2  1  1  1  2  2  1  2\n" +
                        "1 0 1 1 1 2 1 1 1 1 1  1  2  1  2  1  1  1  1  2\n" +
                        "2 1 0 1 1 2 2 1 1 1 1  1  1  1  1  1  2  2  1  1\n" +
                        "1 1 1 0 1 1 2 2 1 1 2  2  2  2  2  2  1  2  2  1\n" +
                        "2 1 1 1 0 1 2 2 2 2 2  1  1  1  2  2  1  1  2  2\n" +
                        "2 2 2 1 1 0 2 2 2 1 1  1  2  1  2  1  2  2  1  2\n" +
                        "1 1 2 2 2 2 0 1 1 1 1  2  1  2  1  2  1  1  2  2\n" +
                        "2 1 1 2 2 2 1 0 1 2 1  1  1  1  1  2  2  2  1  1\n" +
                        "1 1 1 1 2 2 1 1 0 2 1  1  2  1  1  2  1  2  1  2\n" +
                        "2 1 1 1 2 1 1 2 2 0 2  2  1  1  1  2  2  2  2  1\n" +
                        "2 1 1 2 2 1 1 1 1 2 0  1  2  2  2  2  2  1  1  2\n" +
                        "1 1 1 2 1 1 2 1 1 2 1  0  1  2  1  1  2  1  2  1\n" +
                        "2 2 1 2 1 2 1 1 2 1 2  1  0  1  2  2  2  2  1  2\n" +
                        "1 1 1 2 1 1 2 1 1 1 2  2  1  0  1  1  2  2  2  1\n" +
                        "1 2 1 2 2 2 1 1 1 1 2  1  2  1  0  2  1  1  2  1\n" +
                        "1 1 1 2 2 1 2 2 2 2 2  1  2  1  2  0  2  1  2  2\n" +
                        "2 1 2 1 1 2 1 2 1 2 2  2  2  2  1  2  0  1  1  2\n" +
                        "2 1 2 2 1 2 1 2 2 2 1  1  2  2  1  1  1  0  2  2\n" +
                        "1 1 1 2 2 1 2 1 1 2 1  2  1  2  2  2  1  2  0  1\n" +
                        "2 2 1 1 2 2 2 1 2 1 2  1  2  1  1  2  2  2  1  0\n"
        ),
        instance2(
                "20\n" +
                        "14\n" +
                        "4\n" +
                        "1\n" +
                        "3\n" +
                        "12\n" +
                        "3 6 15 16\n" +
                        "0 1 2 8\n" +
                        "8 14 16 17\n" +
                        "7 8 18 19\n" +
                        "2 5 11 19\n" +
                        "8 10 12 14\n" +
                        "3 4 7 15\n" +
                        "6 7 9 19\n" +
                        "2 9 12 14\n" +
                        "5 6 15 19\n" +
                        "1 3 13 15\n" +
                        "3 4 5 12\n" +
                        "2 7 10 19\n" +
                        "3 5 9 17\n" +
                        "0 3 1 2 1 1 1 2 2 3 2  3  3  2  2  3  2  3  1  2\n" +
                        "3 0 1 1 3 1 3 1 1 2 2  2  2  1  3  2  2  1  3  3\n" +
                        "1 1 0 1 3 3 1 2 2 1 2  2  3  2  2  3  1  3  2  2\n" +
                        "2 1 1 0 1 1 3 1 2 2 2  3  2  3  1  3  3  1  1  1\n" +
                        "1 3 3 1 0 2 3 1 3 1 1  1  1  1  2  3  1  3  2  3\n" +
                        "1 1 3 1 2 0 1 3 2 1 1  3  1  2  2  3  1  1  2  2\n" +
                        "1 3 1 3 3 1 0 1 2 1 1  3  2  1  1  1  2  1  1  2\n" +
                        "2 1 2 1 1 3 1 0 2 3 1  1  1  2  2  1  2  1  2  2\n" +
                        "2 1 2 2 3 2 2 2 0 2 1  3  3  3  2  3  3  1  3  1\n" +
                        "3 2 1 2 1 1 1 3 2 0 3  2  1  3  1  1  1  1  1  3\n" +
                        "2 2 2 2 1 1 1 1 1 3 0  3  1  2  1  3  1  2  2  3\n" +
                        "3 2 2 3 1 3 3 1 3 2 3  0  2  1  1  3  2  2  3  3\n" +
                        "3 2 3 2 1 1 2 1 3 1 1  2  0  2  1  2  2  1  3  2\n" +
                        "2 1 2 3 1 2 1 2 3 3 2  1  2  0  3  3  1  1  3  3\n" +
                        "2 3 2 1 2 2 1 2 2 1 1  1  1  3  0  2  2  3  3  3\n" +
                        "3 2 3 3 3 3 1 1 3 1 3  3  2  3  2  0  3  1  3  2\n" +
                        "2 2 1 3 1 1 2 2 3 1 1  2  2  1  2  3  0  3  1  1\n" +
                        "3 1 3 1 3 1 1 1 1 1 2  2  1  1  3  1  3  0  3  3\n" +
                        "1 3 2 1 2 2 1 2 3 1 2  3  3  3  3  3  1  3  0  3\n" +
                        "2 3 2 1 3 2 2 2 1 3 3  3  2  3  3  2  1  3  3  0\n"
        ),
        instance6(
                "20\n" +
                        "13\n" +
                        "5\n" +
                        "1\n" +
                        "2\n" +
                        "12\n" +
                        "1 4 6 7 19\n" +
                        "2 8 11 18 19\n" +
                        "0 4 8 17 18\n" +
                        "0 6 15 17 18\n" +
                        "2 5 8 11 14\n" +
                        "3 8 14 15 19\n" +
                        "7 10 13 14 16\n" +
                        "8 10 12 13 19\n" +
                        "4 9 12 18 19\n" +
                        "2 4 6 13 17\n" +
                        "5 10 12 13 17\n" +
                        "6 9 11 12 19\n" +
                        "1 6 10 13 17\n" +
                        "0 2 2 2 1 1 2 1 2 2 1  1  2  2  1  2  1  2  1  1\n" +
                        "2 0 1 1 2 1 2 1 2 2 2  1  1  1  2  2  1  2  2  2\n" +
                        "2 1 0 1 1 2 1 1 2 1 2  1  1  1  1  2  1  2  1  2\n" +
                        "2 1 1 0 2 1 1 1 2 2 1  2  2  1  1  1  2  2  1  2\n" +
                        "1 2 1 2 0 1 2 1 1 2 2  2  1  2  2  1  2  1  2  2\n" +
                        "1 1 2 1 1 0 2 2 1 1 1  2  1  1  1  2  1  1  2  1\n" +
                        "2 2 1 1 2 2 0 1 1 2 1  1  2  1  2  1  2  1  1  1\n" +
                        "1 1 1 1 1 2 1 0 1 2 1  2  2  1  1  2  1  1  2  1\n" +
                        "2 2 2 2 1 1 1 1 0 2 1  1  2  1  2  1  2  1  1  2\n" +
                        "2 2 1 2 2 1 2 2 2 0 1  2  2  2  1  1  2  2  1  2\n" +
                        "1 2 2 1 2 1 1 1 1 1 0  2  2  1  1  1  2  1  1  2\n" +
                        "1 1 1 2 2 2 1 2 1 2 2  0  1  1  1  2  1  1  1  1\n" +
                        "2 1 1 2 1 1 2 2 2 2 2  1  0  1  2  2  1  2  1  2\n" +
                        "2 1 1 1 2 1 1 1 1 2 1  1  1  0  1  1  1  1  1  2\n" +
                        "1 2 1 1 2 1 2 1 2 1 1  1  2  1  0  2  1  1  2  2\n" +
                        "2 2 2 1 1 2 1 2 1 1 1  2  2  1  2  0  1  1  2  2\n" +
                        "1 1 1 2 2 1 2 1 2 2 2  1  1  1  1  1  0  1  2  2\n" +
                        "2 2 2 2 1 1 1 1 1 2 1  1  2  1  1  1  1  0  2  1\n" +
                        "1 2 1 1 2 2 1 2 1 1 1  1  1  1  2  2  2  2  0  1\n" +
                        "1 2 2 2 2 1 1 1 2 2 2  1  2  2  2  2  2  1  1  0\n"
        );
        final String source;

        Data(String source) {
            this.source = source;
        }

        String source() {
            return source;
        }
    }
}
