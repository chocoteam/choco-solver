/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2026, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import org.chocosolver.memory.IStateBitSet;
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.IntEventType;
import org.chocosolver.solver.variables.events.PropagatorEventType;
import org.chocosolver.util.ESat;
import org.chocosolver.util.sort.ArraySort;
import org.chocosolver.util.sort.IntComparator;

import java.util.*;

import static org.chocosolver.solver.constraints.nary.cumulative.SchedulingUtils.*;

/**
 * Propagator for the Cumulative constraint.
 * It uses : <ul>
 * <li>the scalable TimeTable algorithm from Gay et al. [Gay2015]</li>
 * <li>the OverloadChecking algorithm from Vilim [Vilim2011]</li>
 * </ul>
 * The explanations are the ones described in [Schutt2011].
 * <br>
 * <a href="https://doi.org/10.1007/978-3-319-23219-5_11">Gay, S., Hartert, R., and Schaus, P.: “Simple and Scalable Time-Table Filtering for the Cumulative Constraint”. In: Principles and Practice of Constraint Programming - 21st International Conference, CP 2015, Cork, Ireland, August 31 - September 4, 2015, Proceedings. Ed. by Gilles Pesant. Vol. 9255. Lecture Notes in Computer Science. Springer, 2015, pp. 149–157</a>
 * <br>
 * <a href="https://doi.org/10.1007/978-3-642-21311-3_22">Petr Vilım: “Timetable Edge Finding Filtering Algorithm for Discrete Cumulative Resources”. In: Integration of AI and OR Techniques in Constraint Programming for Combinatorial Optimization Problems - 8th International Conference, CPAIOR 2011, Berlin, Germany, May 23-27, 2011. Proceedings. Ed. by Tobias Achterberg and J. Christopher Beck. Vol. 6697. Lecture Notes in Computer Science. Springer, 2011, pp. 230–245</a>
 * <br>
 * <a href="https://doi.org/10.1007/s10601-010-9103-2">Andreas Schutt et al. “Explaining the cumulative propagator”. In: Constraints An Int. J. 16.3 (2011), pp. 250–282</a>
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 19/10/2023
 */
@Explained
public class PropagatorCumulative extends Propagator<IntVar> {
    private static int getFreeDuration(final Task task) {
        final int pTT = Math.max(0, task.getEct() - task.getLst());
        return task.getDuration().getLB() - pTT;
    }

    private static int compareTaskWithFreeParts(final Task a, final Task b) {
        if (a.getEst() == b.getEst()) {
            return a.getEst() + getFreeDuration(a) - (b.getEst() + getFreeDuration(b));
        }
        return a.getEst() - b.getEst();
    }

    private static IntVar[] extractIntVars(final Task[] tasks, final IntVar[] heights, final IntVar capacity) {
        final List<IntVar> list = new ArrayList<>();
        for (int i = 0; i < tasks.length; i++) {
            list.add(tasks[i].getStart());
            list.add(tasks[i].getDuration());
            list.add(tasks[i].getEnd());
            list.add(heights[i]);
        }
        list.add(capacity);
        for (int i = 0; i < tasks.length; i++) {
            if (!tasks[i].mustBePerformed() && tasks[i].mayBePerformed()) {
                list.add(((OptionalTask) tasks[i]).getPerformed());
            }
        }
        return list.toArray(new IntVar[0]);
    }

    protected final Task[] tasks;
    protected final IntVar[] heights;
    private final IntVar capacity;
    private final IStateBitSet activeTasks;
    private final Map<IntVar, Integer> mapPerformedVarToIdxTask;
    // For the TimeTable
    private final Profile profile;
    // For the overloadChecking
    private final TIntIntHashMap ttAfter;
    private final List<Task> tasksWithFreeParts;
    private final Comparator<Task> comparatorFreeParts;
    private final Map<Task, IntVar> mapTaskToHeight;
    // For energy naive
    private final boolean energyNaive;
    private final IntComparator comparator;
    private final int[] sorArray;
    private final ArraySort<?> sorter;
    // For disjunctive energy naive
    private final boolean disjunctiveEnergyNaive;
    private final ArraySort<?> sort;
    private final IntComparator comp;
    private final int[] tsks;
    // For explanations
    private final TIntArrayList literals;

    public PropagatorCumulative(final Task[] tasks, final IntVar[] heights, final IntVar capacity) {
        this(tasks, heights, capacity, false, false);
    }

    public PropagatorCumulative(
            final Task[] tasks,
            final IntVar[] heights,
            final IntVar capacity,
            final boolean energyNaive,
            final boolean disjunctiveEnergyNaive
    ) {
        super(extractIntVars(tasks, heights, capacity), PropagatorPriority.QUADRATIC, true);
        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity;
        this.activeTasks = this.getModel().getEnvironment().makeBitSet(tasks.length);
        mapPerformedVarToIdxTask = new HashMap<>();
        profile = new Profile(tasks.length);
        tasksWithFreeParts = new ArrayList<>(tasks.length);
        ttAfter = new TIntIntHashMap(2 * tasks.length);
        comparatorFreeParts = PropagatorCumulative::compareTaskWithFreeParts;
        mapTaskToHeight = new HashMap<>(tasks.length);
        this.energyNaive = energyNaive;
        sorArray = new int[tasks.length];
        sorter = new ArraySort<>(tasks.length,false,true);
        comparator = (i1, i2) -> {
            int coef1 = (100 * tasks[i1].getMinDuration() * heights[i1].getLB()) / (tasks[i1].getLct() - tasks[i1].getEst());
            int coef2 = (100 * tasks[i2].getMinDuration() * heights[i2].getLB()) / (tasks[i2].getLct() - tasks[i2].getEst());
            return coef2 - coef1;
        };
        this.disjunctiveEnergyNaive = disjunctiveEnergyNaive;
        sort = new ArraySort<>(tasks.length, false, true);
        comp = (i1, i2) -> tasks[i1].getEst() - tasks[i2].getEst();
        tsks = new int[tasks.length];

        for (int i = 0; i < tasks.length; ++i) {
            if (!tasks[i].mustBePerformed() && tasks[i].mayBePerformed()) {
                mapPerformedVarToIdxTask.put(((OptionalTask) tasks[i]).getPerformed(), i);
            }
            mapTaskToHeight.put(tasks[i], heights[i]);
        }
        literals = new TIntArrayList(4 * tasks.length + 3);
    }

    @Override
    public int getPropagationConditions(int idx) {
        final int n = tasks.length;
        if (idx == 4 * n) { // capacity variable
            return IntEventType.upperBoundAndInst();
        } else if (idx % 4 == 3 && idx < 4 * n) { // heights variables
            return IntEventType.lowerBoundAndInst();
        } else {
            return IntEventType.boundAndInst();
        }
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        if (PropagatorEventType.isFullPropagation(evtmask)) {
            capacity.updateLowerBound(0, this);
            activeTasks.clear();
            for (int i = 0; i < tasks.length; ++i) {
                heights[i].updateLowerBound(0, this, Reason.undef());
                tasks[i].updateMinDuration(0, this, Reason.undef());
                propagateTaskHeight(i);
                if (isActive(i)) {
                    activeTasks.set(i);
                }
            }
        }
        if (activeTasks.size() <= 1) {
            // No need for TimeTable or OverloadChecking in such a case
            return;
        }
        scalableTimeTable();
        overloadChecking();
        updateHeights();
        if (energyNaive) {
            energyNaive();
        }
        if (disjunctiveEnergyNaive && capacity.isInstantiatedTo(1) && activeTasks.size() < 50) {
            disjunctiveEnergyNaive();
        }
    }

    @Override
    public void propagate(int varIdx, int mask) throws ContradictionException {
        final int n = tasks.length;
        if (varIdx == 4 * n) { // capacity variable
            for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
                propagateTaskHeight(i);
                if (!isActive(i)) {
                    activeTasks.clear(i);
                }
            }
            forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
        } else if (varIdx % 4 == 3 && varIdx < 4 * n) { // heights variables
            final int idxTask = varIdx / 4;
            propagateTaskHeight(idxTask);
            if (!isActive(idxTask)) {
                activeTasks.clear(idxTask);
            } else {
                forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
            }
        } else if (varIdx > 4 * n) { // performed variables
            final int idxTask = mapPerformedVarToIdxTask.get(vars[varIdx]);
            if (!isActive(idxTask)) {
                activeTasks.clear(idxTask);
            }
        } else {
            final int idxTask = varIdx / 4;
            if (!isActive(idxTask)) {
                activeTasks.clear(idxTask);
            } else {
                forcePropagate(PropagatorEventType.CUSTOM_PROPAGATION);
            }
        }
    }

    @Override
    public ESat isEntailed() {
        final int n = tasks.length;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for (int i = 0; i < n; i++) {
            if (mustBePerformed(tasks[i], heights[i])) {
                min = Math.min(min, tasks[i].getLst());
                max = Math.max(max, tasks[i].getEct());
            }
        }
        // check capacity
        int maxLoad = 0;
        if (min <= max) {
            int[] consoMin = new int[max - min];
            for (int i = 0; i < n; i++) {
                if (mustBePerformed(tasks[i], heights[i])) {
                    for (int t = tasks[i].getLst(); t < tasks[i].getEct(); t++) {
                        consoMin[t - min] += heights[i].getLB();
                        if (consoMin[t - min] > capacity.getUB()) {
                            return ESat.FALSE;
                        }
                        maxLoad = Math.max(maxLoad, consoMin[t - min]);
                    }
                }
            }
        }
        // check variables are instantiated
        for (int i = 0; i < n; i++) {
            if (!tasks[i].getStart().isInstantiated()
                || !tasks[i].getDuration().isInstantiated()
                || !tasks[i].getEnd().isInstantiated()
                || !heights[i].isInstantiated()
                || tasks[i].mayBePerformed() && !tasks[i].mustBePerformed()
            ) {
                return ESat.UNDEFINED;
            }
        }
        if (!capacity.isInstantiated()) {
            return ESat.UNDEFINED;
        } else if (min <= max && maxLoad <= capacity.getValue()) {
            return ESat.TRUE;
        }
        return ESat.UNDEFINED;
    }

    /**
     * Builds the profil and applies TimeTable filtering (idempotent).
     *
     * @throws ContradictionException when a filtering error is encountered
     */
    private void scalableTimeTable() throws ContradictionException {
        boolean hasFiltered;
        do {
            buildProfile();
            hasFiltered = scalableTimeTableFilter();
        } while (hasFiltered);
    }

    /**
     * Builds the profile (and stores explanations if lcg() is true) and filters the capacity lower bound as the
     * maximum of the height of the rectangles in the profile.
     *
     * @throws ContradictionException when a filtering error is encountered
     */
    private void buildProfile() throws ContradictionException {
        profile.buildProfile(tasks, heights, activeTasks);
        int idxRectMaxHeight = 0;
        for (int j = 0; j < profile.size(); j++) {
            if (profile.getHeightRectangle(idxRectMaxHeight) < profile.getHeightRectangle(j)) {
                idxRectMaxHeight = j;
            }
        }
        if (lcg()) {
            literals.clear();
            final int begin = profile.getStartRectangle(idxRectMaxHeight)
                              + ((profile.getEndRectangle(idxRectMaxHeight) - profile.getStartRectangle(idxRectMaxHeight) - 1) / 2);
            addLiteralsTasks(
                    literals,
                    profile.getIndexesTaskRectangle(idxRectMaxHeight),
                    begin,
                    begin + 1
            );
            capacity.updateLowerBound(profile.getHeightRectangle(idxRectMaxHeight), this, buildReason(literals));
        } else {
            capacity.updateLowerBound(profile.getHeightRectangle(idxRectMaxHeight), this);
        }
    }

    /**
     * Returns the negated literal -[[v <= val]] = [[v >= val + 1]].
     *
     * @param v an integer variable
     * @param val an integer value
     * @return the negated literal -[[v <= val]] = [[v >= val + 1]]
     */
    private int getNegLeqLit(final IntVar v, final int val) {
        if (v.isInstantiated()) {
            return v.getValLit();
        }
        return v.hasEnumeratedDomain() ? v.getGELit(val + 1) : v.getMaxLit();
    }

    /**
     * Returns the negated literal -[[v >= val]] = [[ v <= val - 1]].
     *
     * @param v an integer variable
     * @param val an integer value
     * @return the negated literal -[[v >= val]] = [[ v <= val - 1]]
     */
    private int getNegGeqLit(final IntVar v, final int val) {
        if (v.isInstantiated()) {
            return v.getValLit();
        }
        return v.hasEnumeratedDomain() ? v.getLELit(val - 1) : v.getMinLit();
    }

    /**
     * Add the max literal of the capacity and the literals of tasks whose index is in the list in parameters. Four
     * literals are added : one for the task's start, one for the task's end, one for the task's duration and one for
     * the task's height.
     *
     * @param literals the list of literals
     * @param indexesTask the list of indexes of tasks
     * @param begin the begin instant
     * @param end then end instant
     */
    private void addLiteralsCapacityAndTasks(
            final TIntArrayList literals,
            final TIntArrayList indexesTask,
            final int begin,
            final int end
    ) {
        literals.add(capacity.getMaxLit());
        addLiteralsTasks(literals, indexesTask, begin, end);
    }

    /**
     * Add literals of tasks whose index is in the list in parameters. Four literals are added : one for the task's
     * start, one for the task's end, one for the task's duration and one for the task's height.
     *
     * @param literals the list of literals
     * @param indexesTask the list of indexes of tasks
     * @param begin the begin instant
     * @param end then end instant
     */
    private void addLiteralsTasks(
            final TIntArrayList literals,
            final TIntArrayList indexesTask,
            final int begin,
            final int end
    ) {
        for (int k = 0; k < indexesTask.size(); ++k) {
            final int i = indexesTask.getQuick(k);
            literals.add(getNegGeqLit(tasks[i].getEnd(), Math.min(tasks[i].getEnd().getLB(), end)));
            literals.add(getNegLeqLit(tasks[i].getStart(), Math.max(tasks[i].getStart().getUB(), begin)));
            literals.add(tasks[i].getDuration().getMinLit());
            literals.add(heights[i].getMinLit());
        }
    }

    /**
     * Builds a Reason from the literals in parameters.
     *
     * @param literals a list of literals
     * @return the reason
     */
    private Reason buildReason(final TIntArrayList literals) {
        final int[] ps = new int[literals.size() + 1];
        for (int i = 0; i < literals.size(); ++i) {
            ps[i + 1] = literals.getQuick(i);
        }
        return Reason.r(ps);
    }

    /**
     * Applies filtering according to the TimeTable rule, and returns whether the profile should be recomputed or not
     * (a task that must be performed and with a (potentially new) compulsory part has been filtered).
     *
     * @return true iff the profile should be recomputed
     * @throws ContradictionException when a filtering error is encountered
     */
    private boolean scalableTimeTableFilter() throws ContradictionException {
        boolean shouldRecomputeTimeTable = false;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            final Task task = tasks[i];
            if (mayBePerformed(task, heights[i])) {
                if (scalableTimeTableFilterEst(task, heights[i])) {
                    shouldRecomputeTimeTable |= task.hasCompulsoryPart() && mustBePerformed(task, heights[i]);
                }
                if (scalableTimeTableFilterLct(task, heights[i])) {
                    shouldRecomputeTimeTable |= task.hasCompulsoryPart() && mustBePerformed(task, heights[i]);
                }
            }
            if (!isActive(i)) {
                activeTasks.clear(i);
            }
        }
        return shouldRecomputeTimeTable;
    }

    /**
     * Applies filtering according to the TimeTable rule on the earliest start time, and returns whether the task or the
     * height variable have been filtered.
     *
     * @param task a task
     * @param height the height variable
     * @return true iff the task or the height variable have been filtered
     * @throws ContradictionException when a filtering error is encountered
     */
    private boolean scalableTimeTableFilterEst(final Task task, final IntVar height) throws ContradictionException {
        boolean hasFiltered = false;
        if (!task.getStart().isInstantiated()) {
            int j = profile.find(task.getEst());
            while (j < profile.size() && profile.getStartRectangle(j) < Math.min(task.getEct(), task.getLst())) {
                if (capacity.getUB() - height.getLB() < profile.getHeightRectangle(j)) {
                    final Reason reason;
                    if (lcg()) {
                        literals.clear();
                        final int end = Math.min(task.getEct(), profile.getEndRectangle(j));
                        literals.add(getNegGeqLit(task.getEnd(), end));
                        literals.add(task.getDuration().getMinLit());
                        literals.add(height.getMinLit());
                        addLiteralsCapacityAndTasks(literals, profile.getIndexesTaskRectangle(j), end - 1, end);
                        reason = buildReason(literals);
                    } else {
                        reason = Reason.undef();
                    }
                    hasFiltered |= filterEst(task, height, Math.min(task.getLst(), profile.getEndRectangle(j)), this, reason);
                }
                j++;
            }
        }
        return hasFiltered;
    }

    /**
     * Applies filtering according to the TimeTable rule on the latest completion time, and returns whether the task or
     * the height variable have been filtered.
     *
     * @param task a task
     * @param height the height variable
     * @return true iff the task or the height variable have been filtered
     * @throws ContradictionException when a filtering error is encountered
     */
    private boolean scalableTimeTableFilterLct(final Task task, final IntVar height) throws ContradictionException {
        boolean hasFiltered = false;
        if (!task.getEnd().isInstantiated()) {
            int j = profile.find(task.getLct() - 1);
            while (j >= 1 && profile.getEndRectangle(j) > Math.max(task.getLst(), task.getEct())) {
                if (capacity.getUB() - height.getLB() < profile.getHeightRectangle(j)) {
                    final Reason reason;
                    if (lcg()) {
                        literals.clear();
                        final int begin = Math.max(profile.getStartRectangle(j), task.getLst());
                        literals.add(getNegLeqLit(task.getStart(), begin));
                        literals.add(task.getDuration().getMinLit());
                        literals.add(height.getMinLit());
                        addLiteralsCapacityAndTasks(literals, profile.getIndexesTaskRectangle(j), begin, begin + 1);
                        reason = buildReason(literals);
                    } else {
                        reason = Reason.undef();
                    }
                    hasFiltered |= filterLct(task, height, Math.max(profile.getStartRectangle(j), task.getEct()), this, reason);
                }
                j--;
            }
        }
        return hasFiltered;
    }

    /**
     * Computes the ttAfter map, which, to a given time point, associates the total amount of energy in the profile
     * after said time point.
     */
    private void computeTtAfter() {
        ttAfter.clear();
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            if (mustBePerformed(tasks[i], heights[i])) {
                final int est = tasks[i].getEst();
                final int lct = tasks[i].getLct();
                if (!ttAfter.containsKey(est) || !ttAfter.containsKey(lct)) {
                    computeTtAfter(est, lct);
                }
            }
        }
    }

    /**
     * Computes and adds to the ttAfter map, the total amount of energy in the profile after the latest completion time
     * and the earliest start time of a task.
     *
     * @param est the earliest start time of a task
     * @param lct the latest completion time of a task
     */
    private void computeTtAfter(final int est, final int lct) {
        int ttAfterTime = 0;
        int idx = profile.size() - 1;
        while (0 <= idx && lct <= profile.getStartRectangle(idx)) {
            ttAfterTime += profile.getHeightRectangle(idx) * (profile.getEndRectangle(idx) - profile.getStartRectangle(idx));
            idx--;
        }
        if (0 <= idx && lct < profile.getEndRectangle(idx)) {
            ttAfter.put(lct, ttAfterTime + profile.getHeightRectangle(idx) * (profile.getEndRectangle(idx) - lct));
        } else {
            ttAfter.put(lct, ttAfterTime);
        }
        while (0 <= idx && est <= profile.getStartRectangle(idx)) {
            ttAfterTime += profile.getHeightRectangle(idx) * (profile.getEndRectangle(idx) - profile.getStartRectangle(idx));
            idx--;
        }
        if (0 <= idx && est < profile.getEndRectangle(idx)) {
            ttAfter.put(est, ttAfterTime + profile.getHeightRectangle(idx) * (profile.getEndRectangle(idx) - est));
        } else {
            ttAfter.put(est, ttAfterTime);
        }
    }

    /**
     * Computes the list of tasks that must be performed and with a non-null free duration, and sorts it.
     */
    private void computeTasksWithFreeParts() {
        tasksWithFreeParts.clear();
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            if (mustBePerformed(tasks[i], heights[i]) && getFreeDuration(tasks[i]) > 0) {
                tasksWithFreeParts.add(tasks[i]);
            }
        }
        // Sort free parts
        tasksWithFreeParts.sort(comparatorFreeParts);
    }

    /**
     * Applies the OverloadChecking rule. Should be called after {@link #scalableTimeTable()}.
     *
     * @throws ContradictionException when a filtering error is encountered
     */
    private void overloadChecking() throws ContradictionException {
        // From PropCumulativeVilim2011
        computeTtAfter();
        computeTasksWithFreeParts();
        int eEF;
        Task a;
        Task b;
        int lctB;
        for (int i = 0; i < tasksWithFreeParts.size(); i++) {
            b = tasksWithFreeParts.get(i);
            lctB = b.getLct();
            eEF = 0;
            for (int k = tasksWithFreeParts.size() - 1; k >= 0; k--) {
                a = tasksWithFreeParts.get(k);
                if (a.getLct() <= b.getLct()) {
                    eEF += getFreeDuration(a) * mapTaskToHeight.get(a).getLB();
                    if (capacity.getUB() * (lctB - a.getEst()) < eEF + ttAfter.get(a.getEst()) - ttAfter.get(lctB)) {
                        fails(); // TODO : should be explained
                        break;
                    }
                }
            }
        }
    }

    /**
     * Filters the heights variables based on the profile: the height of a task cannot be higher than the capacity
     * minus the maximum height of rectangles that the task intersects.
     * Should be called after {@link #scalableTimeTable()}.
     *
     * @throws ContradictionException when a filtering error is encountered
     */
    private void updateHeights() throws ContradictionException {
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            final Task task = tasks[i];
            if (task.getLst() < task.getEct() && task.mustBePerformed()) {
                final IntVar height = heights[i];
                int j = profile.find(task.getLst());
                while (j < profile.size() && profile.getStartRectangle(j) < task.getEct()) {
                    if (lcg()) {
                        literals.clear();
                        final int begin = Math.max(profile.getStartRectangle(j), task.getLst());
                        literals.add(getNegGeqLit(task.getEnd(), begin));
                        literals.add(task.getDuration().getMinLit());
                        literals.add(height.getMinLit());
                        addLiteralsCapacityAndTasks(literals, profile.getIndexesTaskRectangle(j), begin, begin + 1);
                        final Reason reason = buildReason(literals);
                        height.updateUpperBound(capacity.getUB() - (profile.getHeightRectangle(j) - height.getLB()), this, reason);
                    } else {
                        height.updateUpperBound(capacity.getUB() - (profile.getHeightRectangle(j) - height.getLB()), this);
                    }
                    j++;
                }
                if (!isActive(i)) {
                    activeTasks.clear(i);
                }
            }
        }
    }

    /**
     * Applies a naive version of the Energetic Reasoning rule.
     * TODO : should be completed/replaced with state-of-the-art filtering algorithm (with explanations)
     *
     * @throws ContradictionException when a filtering error is encountered
     */
    private void energyNaive() throws ContradictionException {
        int idx = 0;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            if (mustBePerformed(tasks[i], heights[i]) && tasks[i].getMinDuration() > 0) {
                sorArray[idx++] = i;
            }
        }
        sorter.sort(sorArray,idx,comparator);
        double xMin = Integer.MAX_VALUE / 2d;
        double xMax = Integer.MIN_VALUE / 2d;
        double surface = 0;
        double camax = capacity.getUB();
        for (int k = 0; k < idx; k++) {
            final int i = sorArray[k];
            xMax = Math.max(xMax, tasks[i].getLct());
            xMin = Math.min(xMin, tasks[i].getEst());
            if (xMax >= xMin) {
                final double availSurf = (xMax - xMin) * camax - surface;
                heights[i].updateUpperBound((int) Math.floor((availSurf / tasks[i].getMinDuration()) + 0.01), this);
                tasks[i].updateMaxDuration((int) Math.floor((availSurf / heights[i].getLB()) + 0.01), this);
                surface += (long) tasks[i].getMinDuration() * heights[i].getLB(); // potential overflow
                if (xMax > xMin) {
                    capacity.updateLowerBound((int) Math.ceil(surface / (xMax - xMin) - 0.01), this);
                }
                if (surface > (xMax - xMin) * camax) {
                    fails(); // TODO: could be more precise, for explanation purpose
                }
            }
        }
    }

    /**
     * Applies a naive version of the Energetic Reasoning rule for Disjunctive constraint.
     * TODO : should be completed/replaced with state-of-the-art filtering algorithm (with explanations)
     *
     * @throws ContradictionException when a filtering error is encountered
     */
    private void disjunctiveEnergyNaive() throws ContradictionException {
        // filtering algorithm for disjunctive constraint
        capacity.updateUpperBound(1, this);
        // remove tasks that do not consume any resource
        int tskSize = 0;
        for (int i = activeTasks.nextSetBit(0); i != -1; i = activeTasks.nextSetBit(i + 1)) {
            if (mustBePerformed(tasks[i], heights[i]) && tasks[i].getMinDuration() > 0) {
                tsks[tskSize++] = i;
            }
        }
        sort.sort(tsks, tskSize, comp);
        // run energetic reasoning
        for (int x = 0; x < tskSize; x++) {
            final int task1 = tsks[x];
            for (int y = 0; y < tskSize; y++) {
                if (x != y) {
                    final int task2 = tsks[y];
                    final int t1 = tasks[task1].getEst();
                    final int t2 = tasks[task2].getLct();
                    if (tasks[task1].getEct() > tasks[task2].getLst()) {
                        tasks[task1].updateEst(tasks[task2].getEct(), this);
                        tasks[task2].updateLct(tasks[task1].getLst(), this);
                    } else if (t1 < t2 && (t1 < tasks[task2].getEct() || t2 > tasks[task1].getLst())) {
                        int w = 0;
                        for (int z = 0; z < tskSize; z++) {
                            final int task3 = tsks[z];
                            if (task3 != task1 && task3 != task2) {
                                if (tasks[task3].getEst() >= t2) {
                                    break;
                                }
                                int pB = tasks[task3].getMinDuration() * heights[task3].getLB();
                                int pbt1 = Math.max(0, pB - Math.max(0, t1 - tasks[task3].getEst()));
                                int pbt2 = Math.max(0, pB - Math.max(0, tasks[task3].getLct() - t2));
                                int pbt = Math.min(pbt1, pbt2);
                                w += Math.min(t2 - t1, pbt);
                            }
                        }
                        if (w + tasks[task1].getMinDuration() + tasks[task2].getMinDuration() > t2 - t1) {
                            tasks[task1].updateEst(tasks[task2].getEct(), this);
                            tasks[task2].updateLct(tasks[task1].getLst(), this);
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns true iff the task at the index in parameter should be considered, <i>i.e.</i> the task may be performed,
     * its height variable's upper bound is strictly greater than 0, and the task duration's upper bound is strictly
     * greater than 0.
     *
     * @param idxTask the index of the task
     * @return true iff the task at the index in parameter should be considered
     */
    private boolean isActive(final int idxTask) {
        return mayBePerformed(tasks[idxTask], heights[idxTask]) && tasks[idxTask].getMaxDuration() > 0;
    }

    /**
     * Propagates the capacity constraint for the task at index i. More particularly, it enforces the following equation :
     * <p>
     * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
     *
     * @param i the index of the task
     * @throws ContradictionException whenever a filtering error occurs
     */
    private void propagateTaskHeight(final int i) throws ContradictionException {
        if (lcg()) {
            propTaskHeightExplained(i);
        } else {
            propTaskHeight(i);
        }
    }

    /**
     * Propagates the capacity constraint for the task at index i. More particularly, it enforces the following equation :
     * <p>
     * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
     *
     * @param i the index of the task
     * @throws ContradictionException whenever a filtering error occurs
     */
    private void propTaskHeight(final int i) throws ContradictionException {
        if (capacity.getUB() < heights[i].getLB()) {
            if (tasks[i].mustBePerformed()) {
                tasks[i].updateDuration(0, 0, this);
            } else {
                tasks[i].forceToBeOptional(this);
            }
        } else if (tasks[i].mustBePerformed() && tasks[i].getMinDuration() > 0) {
            heights[i].updateUpperBound(capacity.getUB(), this);
            capacity.updateLowerBound(heights[i].getLB(), this);
        }
    }

    /**
     * Propagates the capacity constraint for the task at index i with explanations. More particularly, it enforces the
     * following equation :
     * <p>
     * tasks[i].mustBePerformed() &Implies; (heights[i] &le; capacity &or; tasks[i].duration = 0).
     *
     * @param i the index of the task
     * @throws ContradictionException whenever a filtering error occurs
     */
    private void propTaskHeightExplained(final int i) throws ContradictionException {
        if (capacity.getUB() < heights[i].getLB()) {
            if (tasks[i].mustBePerformed()) {
                final Reason reason = tasks[i] instanceof OptionalTask
                        ? Reason.r(heights[i].getLELit(heights[i].getLB() - 1), capacity.getGELit(capacity.getUB() + 1), ((OptionalTask) tasks[i]).getPerformed().getEQLit(0))
                        : Reason.r(heights[i].getLELit(heights[i].getLB() - 1), capacity.getGELit(capacity.getUB() + 1));
                tasks[i].updateMaxDuration(0, this, reason);
            } else {
                tasks[i].forceToBeOptional(this, Reason.r(heights[i].getLELit(heights[i].getLB() - 1), capacity.getGELit(capacity.getUB() + 1)));
            }
        } else if (tasks[i].mustBePerformed() && tasks[i].getMinDuration() > 0) {
            final int durationLit = tasks[i].getDuration().getEQLit(0);
            final Reason reasonHeight;
            final Reason reasonCapacity;
            if (tasks[i] instanceof OptionalTask) {
                final int performedLit = ((OptionalTask) tasks[i]).getPerformed().getEQLit(0);
                reasonHeight = Reason.r(capacity.getGELit(capacity.getUB() + 1), durationLit, performedLit);
                reasonCapacity = Reason.r(heights[i].getLELit(heights[i].getLB() - 1), durationLit, performedLit);
            } else {
                reasonHeight = Reason.r(capacity.getGELit(capacity.getUB() + 1), durationLit);
                reasonCapacity = Reason.r(heights[i].getLELit(heights[i].getLB() - 1), durationLit);
            }
            heights[i].updateUpperBound(capacity.getUB(), this, reasonHeight);
            capacity.updateLowerBound(heights[i].getLB(), this, reasonCapacity);
        }
    }
}
