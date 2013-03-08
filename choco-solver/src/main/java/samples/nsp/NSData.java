/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
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

import gnu.trove.map.hash.TObjectIntHashMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
* Created by IntelliJ IDEA.
* User: sofdem - sophie.demassey{at}emn.fr
* Date: Jul 27, 2010 - 2:09:07 PM
*
* Data of a Nurse Scheduling Problem instance.
* The schedule of an employee (nurse) is an assignment from any period (day) to an activity type.
* The schedule is feasible if it satisfies the sequencing rules defined by the employee contract.
* The NSProblem is to find a feasible schedule for each employee on a given time horizon
* such that the cover requirements at any period and for any activity are satisfied.
* ASSUMPTIONS:
* - the first day of the planning is the first day of a month (1st) and the first day of a week (MONDAY)
* - the number of days in the planning is a multiple of 7
* - the cover requirements are identical over all the periods
* - the first 'nbFullEmployees' employees have a full-time contract, the others have a part-time contract
* - full-time and part-time contracts may define different activity counter bounds, but they share the same forbidden pattern rules
* - mandatory and forbidden assignments may be defined per employee
* - the initials of the work activity names are all different
* @author Sophie Demassey
*/
public class NSData {

    /**
     * literal representing a rest period (to be used in forbidden patterns)
     */
    public final static char REST_LITERAL = '-';

    /**
     * literal representing a non-fixed period (to be used in forbidden patterns)
     */
    public final static char ANY_LITERAL = '*';

    /**
     * literal representing a work period (to be used in forbidden patterns)
     */
    public final static char WORK_LITERAL = '$';

    /**
     * the activity counter types that can be constrained (bounded)
     */
    public enum CounterType {
        /**
         * number of occurrences of an activity
         */
        HORIZON,
        /**
         * number of occurrences of an activity per week
         */
        WEEK,
        /**
         * number of consecutive work shifts
         */
        WORK_SPAN
    }


    /**
     * number of days (periods) in the planning horizon
     */
    private int nbDays;

    /**
     * number of employees (nurses)
     */
    private int nbEmployees;

    /**
     * number of employees with a full time contract
     */
    private int nbFullTimeEmployees;

    /**
     * number of activities (including REST)
     */
    private int nbActivities;

    /**
     * table [nbActivities] of the activity literals (name first character) including '-' (REST) at the last index (nbActivities-1)
     */
    private char[] literals;

    /**
     * inverse map [nbActivities] of the activity literals to the activity values (index in table 'literals')
     */
    private TObjectIntHashMap<Character> literalValue;

    /**
     * map [nbActivities] of the activity names to the activity values
     */
    private TObjectIntHashMap<String> activityValue;

    /**
     * regular expression "any activity value" = (0|1|..|nbActivities-1)
     */
    private String regExpAny;

    /**
     * regular expression "any work activity value" = (0|1|..|nbActivities-2)
     */
    private String regExpWork;


    /**
     * index of the lower bound in the bound tables
     */
    static final int LB = 0;
    /**
     * index of the upper bound in the bound tables
     */
    static final int UB = 1;

    /**
     * table [2][nbActivities] of the activity cover requirement bounds
     */
    private int[][] coverBounds;

    /**
     * map [CounterType] to the table [2][nbActivities] of the activity counter bounds defined by the full time contract
     */
    private Map<CounterType, int[][]> fullCounters;

    /**
     * map [CounterType] to the table [2][nbActivities] of the activity counter bounds defined by the part time contract
     */
    private Map<CounterType, int[][]> partCounters;

    /**
     * list of patterns - defined as string of activity literals prefixed by a day index - that must not appear in any employee schedule
     */
    private List<String> forbiddenPatterns;

    /**
     * list of regular expressions of activity values translating the forbidden patterns
     */
    private List<String> forbiddenRegExps;

    /**
     * list of mandatory and forbidden assignments
     */
    private List<int[]> preAssignments;

    /**
     * list of employee groups that are interchangeable (ex: same contracts, same pre-assignments)
     */
    private List<int[]> symmetricEmployeeGroups;

    /**
     * list of employee groups who require a fair distribution of the activities
     */
    private List<int[]> equityEmployeeGroups;


    /**
     * build an instance without constraints, initialize the activity tables
     *
     * @param nbDays              number of days
     * @param nbEmployees         number of employees
     * @param nbFullTimeEmployees number of employees with a full-time contract
     * @param workActivities      list of work activity names
     */
    private NSData(int nbDays, int nbEmployees, int nbFullTimeEmployees, String[] workActivities) {
        // periods
        assert nbDays % 7 == 0 : "the number of periods must be a multiple of 7";
        this.nbDays = nbDays;

        // employees
        this.nbEmployees = nbEmployees;
        this.nbFullTimeEmployees = nbFullTimeEmployees;
        this.symmetricEmployeeGroups = new ArrayList<int[]>();
        this.equityEmployeeGroups = new ArrayList<int[]>();

        // activities
        this.nbActivities = workActivities.length + 1;
        this.literals = new char[nbActivities];
        this.activityValue = new TObjectIntHashMap<String>(nbActivities);
        this.literalValue = new TObjectIntHashMap<Character>(nbActivities);
        for (int a = 0; a < workActivities.length; a++) {
            activityValue.put(workActivities[a], a);
            char literal = workActivities[a].charAt(0);
            literalValue.put(literal, a);
            literals[a] = literal;
        }
        activityValue.put("REST", nbActivities - 1);
        literalValue.put(REST_LITERAL, nbActivities - 1);
        literals[nbActivities - 1] = REST_LITERAL;

        this.initActivityRegExps(workActivities.length);

        // rules for FullTime and PartTime contracts
        this.fullCounters = new HashMap<CounterType, int[][]>();
        this.partCounters = new HashMap<CounterType, int[][]>();
        this.forbiddenPatterns = new ArrayList<String>();
        this.forbiddenRegExps = new ArrayList<String>();
        this.preAssignments = new ArrayList<int[]>();
    }

    /**
     * initialize the regular expressions of activities
     *
     * @param nbWorkActs number of work activities
     */
    private void initActivityRegExps(int nbWorkActs) {
        StringBuffer b = new StringBuffer("(0");
        for (int a = 1; a < nbWorkActs; a++)
            b.append("|").append(a);
        regExpWork = b.toString() + ")";
        regExpAny = b.append("|").append(nbWorkActs).append(")").toString();
    }

    /**
     * translate a pattern of activity literals into a regular expression of activity values
     *
     * @param literalPattern sequence of activity literals
     * @return the regular expression
     */
    private String makeValueRegExp(String literalPattern) {
        char[] literals = literalPattern.toCharArray();
        StringBuffer b = new StringBuffer();
        for (char c : literals) {
            switch (c) {
                case WORK_LITERAL:
                    b.append(regExpWork);
                    break;
                case ANY_LITERAL:
                    b.append(regExpAny);
                    break;
                default:
                    b.append(this.getValue(c));
            }
        }
        return b.toString();
    }

    /**
     * add a sliding pattern to the list of forbidden patterns : the pattern must not appear at any time in an employee schedule
     *
     * @param literalPattern sequence of activity literals
     */
    private void addForbiddenPattern(String literalPattern) {
        forbiddenPatterns.add("9" + literalPattern);
        StringBuffer b = new StringBuffer(regExpAny).append("*").append(this.makeValueRegExp(literalPattern)).append(regExpAny).append("*");
        forbiddenRegExps.add(b.toString());
    }

    /**
     * add a week pattern to the list of forbidden patterns: the pattern may appear except starting at a specific day of the week
     *
     * @param literalPattern sequence of activity literals
     * @param offset         first index of the specific day (0 = MONDAY, ..., 6 = SUNDAY)
     */
    private void addForbiddenPattern(String literalPattern, int offset) {
        forbiddenPatterns.add(offset + literalPattern);
        StringBuffer b = new StringBuffer("(").append(regExpAny).append("{7})*");
        b.append(regExpAny).append("{").append(offset).append("}");
        b.append(this.makeValueRegExp(literalPattern)).append(regExpAny).append("*");
        forbiddenRegExps.add(b.toString());
    }

    /**
     * get the number of days in the planning horizon
     *
     * @return the number of days
     */
    public int nbDays() {
        return nbDays;
    }

    /**
     * get the number of weeks in the planning horizon
     *
     * @return the number of weeks
     */
    public int nbWeeks() {
        return nbDays / 7;
    }

    /**
     * get the number of employees
     *
     * @return the number of employees
     */
    public int nbEmployees() {
        return nbEmployees;
    }

    /**
     * get the number of employees having a full-time contract
     *
     * @return the number of employees having a full-time contract
     */
    public int nbFullTimeEmployees() {
        return nbFullTimeEmployees;
    }

    /**
     * check whether an employee has a full-time contract
     *
     * @param employeeIndex the employee index
     * @return true iff e has a full-time contract
     */
    public boolean isFullTimeEmployee(int employeeIndex) {
        return employeeIndex < nbFullTimeEmployees;
    }

    /**
     * get the number of activities (including REST)
     *
     * @return the number of activities
     */
    public int nbActivities() {
        return nbActivities;
    }

    /**
     * get the activity value according to the activity name
     *
     * @param activityName the activity name
     * @return the activity value
     */
    public int getValue(String activityName) {
        return activityValue.get(activityName);
    }

    /**
     * get the activity value according to the activity literal
     *
     * @param activityLiteral the activity literal
     * @return the activity value
     */
    public int getValue(char activityLiteral) {
        return literalValue.get(activityLiteral);
    }

    /**
     * get the activity literal according to the activity value
     *
     * @param activityVal the activity value
     * @return the activity literal
     */
    public char getLiteral(int activityVal) {
        return this.literals[activityVal];
    }

    /**
     * check whether an activity is a rest or a work shift
     *
     * @param activityVal the activity value
     * @return true iff the activity is a REST
     */
    public boolean isRestValue(int activityVal) {
        return activityVal == activityValue.get("REST");
    }

    /**
     * check whether an activity value matches a literal
     *
     * @param activityVal the activity value to be checked
     * @param literal     the literal
     * @return true iff the activity matches the literal
     */
    public boolean isMatchedBy(int activityVal, char literal) {
        return literal == ANY_LITERAL || (literal == WORK_LITERAL && activityVal != getValue("REST")) || activityVal == getValue(literal);
    }

    /**
     * get the groups of symmetric employees
     *
     * @return the groups of symmetric employees
     */
    public List<int[]> symmetricEmployeeGroups() {
        return symmetricEmployeeGroups;
    }

    /**
     * get the groups of equitable employees
     *
     * @return the groups of equitable employees
     */
    public List<int[]> equityEmployeeGroups() {
        return equityEmployeeGroups;
    }


    /**
     * get the list of mandatory/forbidden assignments
     * an assignment is given by a 4-uple of integers (b, employeeIndex, dayIndex, activityValue) where b=1 if mandatory, b=0 if forbidden
     *
     * @return the list of mandatory/forbidden assignments
     */
    public List<int[]> preAssignments() {
        return preAssignments;
    }

    /**
     * get the list of forbidden patterns
     * a pattern is defined by a string of activity literals prefixed by a number = 9 for a sliding pattern or = the first day index (0=MONDAY, ..., 6=SUNDAY) for a weekly pattern
     *
     * @return the list of forbidden patterns
     */
    public List<String> forbiddenPatterns() {
        return forbiddenPatterns;
    }

    /**
     * get the list of forbidden patterns as regular expression of activity literals
     *
     * @return the list of forbidden patterns
     */
    public List<String> forbiddenRegExps() {
        return forbiddenRegExps;
    }

    /**
     * get the minimum cover required for an activity
     *
     * @param activityVal activity value
     * @return the minimum cover
     */
    public int getCoverLB(int activityVal) {
        return coverBounds[LB][activityVal];
    }

    /**
     * get the maximum cover required for an activity
     *
     * @param activityVal activity value
     * @return the maximum cover
     */
    public int getCoverUB(int activityVal) {
        return coverBounds[UB][activityVal];
    }

    /**
     * get the total cover over all periods for an activity: only if the cover is fixed (min=max)
     *
     * @param activityVal activity value
     * @return the total cover
     */
    public int getTotalCover(int activityVal) {
        assert coverBounds[LB][activityVal] == coverBounds[UB][activityVal];
        return coverBounds[LB][activityVal] * nbDays;
    }

    private Map<CounterType, int[][]> getCounters(int employeeIndex) {
        return isFullTimeEmployee(employeeIndex) ? fullCounters : partCounters;
    }

    private int getCounter(int employeeIndex, int activity, CounterType type, int bound) {
        return getCounters(employeeIndex).get(type)[bound][activity];
    }


    /**
     * get the maximum occurrence number of an activity required for an employee over the planning horizon
     *
     * @param employeeIndex the employee index
     * @param activityValue the activity value
     * @return the maximum occurence number
     */
    public int getCounterUB(int employeeIndex, int activityValue) {
        return getCounter(employeeIndex, activityValue, CounterType.HORIZON, UB);
    }

    /**
     * get the minimum occurrence number of an activity required for an employee over the planning horizon
     *
     * @param employeeIndex the employee index
     * @param activityValue the activity value
     * @return the minimum occurence number
     */
    public int getCounterLB(int employeeIndex, int activityValue) {
        return getCounter(employeeIndex, activityValue, CounterType.HORIZON, LB);
    }

    /**
     * get the maximum occurrence number of an activity required for an employee over a week
     *
     * @param employeeIndex the employee index
     * @param activityValue the activity value
     * @return the maximum occurence number per week
     */
    public int getWeekCounterUB(int employeeIndex, int activityValue) {
        return getCounter(employeeIndex, activityValue, CounterType.WEEK, UB);
    }

    /**
     * get the minimum occurrence number of an activity required for an employee over a week
     *
     * @param employeeIndex the employee index
     * @param activityValue the activity value
     * @return the minimum occurence number per week
     */
    public int getWeekCounterLB(int employeeIndex, int activityValue) {
        return getCounter(employeeIndex, activityValue, CounterType.WEEK, LB);
    }

    /**
     * get the maximum number of consecutive work shifts required for an employee
     *
     * @param employeeIndex the employee index
     * @return the maximum number of consecutive work shifts
     */
    public int getMaxWorkSpan(int employeeIndex) {
        return getCounter(employeeIndex, 0, CounterType.WORK_SPAN, UB);
    }

    /**
     * get the minimum number of consecutive work shifts required for an employee
     *
     * @param employeeIndex the employee index
     * @return the minimum number of consecutive work shifts
     */
    public int getMinWorkSpan(int employeeIndex) {
        return getCounter(employeeIndex, 0, CounterType.WORK_SPAN, LB);
    }

    /**
     * get the maximum number of work shifts required for an employee over the planning horizon
     *
     * @param employeeIndex the employee index
     * @return the maximum number of work shifts
     */
    public int getMaxWork(int employeeIndex) {
        return nbDays - getCounterLB(employeeIndex, getValue("REST"));
    }

    /**
     * get the minimum number of work shifts required for an employee over the planning horizon
     *
     * @param employeeIndex the employee index
     * @return the minimum number of work shifts
     */
    public int getMinWork(int employeeIndex) {
        return nbDays - getCounterUB(employeeIndex, getValue("REST"));
    }

    /**
     * get the maximum number of work shifts required for an employee over a week
     *
     * @param employeeIndex the employee index
     * @return the maximum number of work shifts per week
     */
    public int getMaxWorkPerWeek(int employeeIndex) {
        return 7 - getWeekCounterLB(employeeIndex, getValue("REST"));
    }

    /**
     * get the minimum number of work shifts required for an employee over a week
     *
     * @param employeeIndex the employee index
     * @return the minimum number of work shifts per week
     */
    public int getMinWorkPerWeek(int employeeIndex) {
        return 7 - getWeekCounterUB(employeeIndex, getValue("REST"));
    }

//////////////////////////////////////////// DEFAULT INSTANCE


    /**
     * the default instance with 8 employees and 3 activities on 1 month (=28 days) and a list of constraints
     *
     * @return the instance
     */
    public static NSData makeDefaultInstance() {
        NSData data = new NSData(28, 8, 4, new String[]{"DAY", "NIGHT"});

        // COVER: 3 DAY and 1 NIGHT shifts a day
        data.coverBounds = new int[][]{{3, 1, 4}, {3, 1, 4}};

        // COUNTER: exactly 18 work shifts a month of which at most 4 NIGHT
        data.fullCounters.put(CounterType.HORIZON, new int[][]{{0, 0, 10}, {28, 4, 10}});
        // COUNTER: between 4 and 5 work shifts a week
        data.fullCounters.put(CounterType.WEEK, new int[][]{{0, 0, 2}, {7, 7, 3}});
        // COUNTER: at most 5 consecutive work shifts
        data.fullCounters.put(CounterType.WORK_SPAN, new int[][]{{0}, {5}});

        // COUNTER: at most 10 work shifts a month of which at most 4 NIGHT
        data.partCounters.put(CounterType.HORIZON, new int[][]{{0, 0, 18}, {28, 4, 28}});
        // COUNTER: between 2 and 3 work shifts a week
        data.partCounters.put(CounterType.WEEK, new int[][]{{0, 0, 4}, {7, 7, 5}});
        // COUNTER: at most 5 consecutive work shifts
        data.partCounters.put(CounterType.WORK_SPAN, new int[][]{{0}, {5}});

        // Pattern: at most 5 consecutive work shifts (redundant)
        data.addForbiddenPattern("$$$$$$");
        // Pattern: complete week-end
        data.addForbiddenPattern("$-", 5);
        data.addForbiddenPattern("-$", 5);
        // Pattern: no night before a free week-end
        data.addForbiddenPattern("N--", 4);
        // Pattern: two rests after a night or a sequence of nights
        data.addForbiddenPattern("ND");
        data.addForbiddenPattern("N-$");
        // Pattern: at most 2 consecutive worked week-ends
        data.addForbiddenPattern("$$*****$$*****$$", 5);

        // mandatory assignment employee 0 period 0 activity 0
        data.preAssignments.add(new int[]{1, 0, 0, 0});
        // mandatory assignment employee 0 period 1 activity 0
        data.preAssignments.add(new int[]{1, 0, 1, 0});
        // mandatory assignments employee 2
        data.preAssignments.add(new int[]{1, 2, 0, 0});
        data.preAssignments.add(new int[]{1, 2, 1, 0});
        // mandatory assignments employee 3
        data.preAssignments.add(new int[]{1, 3, 0, 1});
        data.preAssignments.add(new int[]{1, 3, 1, 1});
        // mandatory assignments employee 4
        data.preAssignments.add(new int[]{1, 4, 0, 0});
        data.preAssignments.add(new int[]{1, 4, 1, 0});

        // employees 0 and 2 are interchangeable (redundant)
        data.symmetricEmployeeGroups.add(new int[]{0, 2});
        // employees 5, 6, 7 are interchangeable (redundant)
        data.symmetricEmployeeGroups.add(new int[]{5, 6, 7});

        // equity between employees with a full-time contract
        data.equityEmployeeGroups.add(new int[]{0, 1, 2, 3});
        // equity between employees with a part-time contract
        data.equityEmployeeGroups.add(new int[]{4, 5, 6, 7});

        return data;
    }


//////////////////////////////////////////// PARSER CSV TANGUY LAPEGUE

    /**
     * create an instance of the NSP by reading a data file
     *
     * @return the instance
     */
    public static NSData makeInstanceNSP() {
        int nbD = NSParser.searchInteger(NSParser.NB_DAYS);
        int nbE = NSParser.searchInteger(NSParser.NB_EMPLOYEES);
        int nbF = NSParser.searchInteger(NSParser.NB_FULL_TIME);
        String[] work = NSParser.searchStringTab(NSParser.WORK);
        NSData data = new NSData(nbD, nbE, nbF, work);

        data.makeCoverBounds();
        data.makeWorkCapacity();
        data.makeForbiddenPattern();
        data.makePreAssignments();
        data.makeSymetricEmployeeGroups();
        data.makeEquityEmployeeGroups();

        return data;
    }

    private void makeEquityEmployeeGroups() {
        int[] full = new int[nbFullTimeEmployees];
        for (int i = 0; i < nbFullTimeEmployees; i++) {
            full[i] = i;
        }
        // equity between employees with a full-time contract
        this.equityEmployeeGroups.add(full);

        int[] partial = new int[nbEmployees - nbFullTimeEmployees];
        for (int i = nbFullTimeEmployees; i < nbEmployees; i++) {
            partial[i - nbFullTimeEmployees] = i;
        }
        // equity between employees with a part-time contract
        this.equityEmployeeGroups.add(partial);
    }

    private void makeSymetricEmployeeGroups() {
        for (String group : NSParser.searchStringTab(NSParser.SYMETRIC)) {
            this.symmetricEmployeeGroups.add(NSParser.splitIntoInt(group));
        }
    }

    private void makePreAssignments() {
        for (String group : NSParser.searchStringTab(NSParser.ASSIGNMENTS)) {
            this.preAssignments.add(NSParser.splitIntoInt(group));
        }
    }

    private void makeForbiddenPattern() {
        for (String pattern : NSParser.searchStringTab(NSParser.SLIDING_P)) {
            this.addForbiddenPattern(pattern);
        }
        for (String infos : NSParser.searchStringTab(NSParser.FIXED_P)) {
            String pattern = infos.split("_")[0];
            int position = Integer.parseInt(infos.split("_")[1]);
            this.addForbiddenPattern(pattern, position);
        }
    }

    private void makeWorkCapacity() {

        int[] f_h_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.F_HORIZON)[0]);
        int[] f_h_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.F_HORIZON)[1]);
        this.fullCounters.put(CounterType.HORIZON, new int[][]{f_h_inf, f_h_sup});

        f_h_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.F_WEEK)[0]);
        f_h_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.F_WEEK)[1]);
        this.fullCounters.put(CounterType.WEEK, new int[][]{f_h_inf, f_h_sup});

        f_h_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.F_WORK_SPAN)[0]);
        f_h_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.F_WORK_SPAN)[1]);
        this.fullCounters.put(CounterType.WORK_SPAN, new int[][]{f_h_inf, f_h_sup});

        f_h_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.P_HORIZON)[0]);
        f_h_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.P_HORIZON)[1]);
        this.partCounters.put(CounterType.HORIZON, new int[][]{f_h_inf, f_h_sup});

        f_h_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.P_WEEK)[0]);
        f_h_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.P_WEEK)[1]);
        this.partCounters.put(CounterType.WEEK, new int[][]{f_h_inf, f_h_sup});

        f_h_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.P_WORK_SPAN)[0]);
        f_h_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.P_WORK_SPAN)[1]);
        this.partCounters.put(CounterType.WORK_SPAN, new int[][]{f_h_inf, f_h_sup});
    }

    private void makeCoverBounds() {
        int[] cover_inf = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.COVER)[0]);
        int[] cover_sup = NSParser.splitIntoInt(NSParser.searchStringTab(NSParser.COVER)[1]);
        this.coverBounds = new int[][]{cover_inf, cover_sup};
    }

}