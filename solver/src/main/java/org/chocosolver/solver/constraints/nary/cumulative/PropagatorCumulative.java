/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2025, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;
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
    // For explanations
    private final TIntObjectHashMap<Reason> reasonRect;

    public PropagatorCumulative(final Task[] tasks, final IntVar[] heights, final IntVar capacity) {
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
        reasonRect = new TIntObjectHashMap<>();

        for (int i = 0; i < tasks.length; ++i) {
            if (!tasks[i].mustBePerformed() && tasks[i].mayBePerformed()) {
                mapPerformedVarToIdxTask.put(((OptionalTask) tasks[i]).getPerformed(), i);
            }
            mapTaskToHeight.put(tasks[i], heights[i]);
        }
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
                heights[i].updateLowerBound(0, this);
                tasks[i].updateMinDuration(0, this);
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
                if (tasks[i].mustBePerformed()) {
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
            if (lcg()) {
                reasonRect.put(j, buildReasonRectangle(j));
            }
        }
        if (lcg()) {
            capacity.updateLowerBound(profile.getHeightRectangle(idxRectMaxHeight), this, reasonRect.get(idxRectMaxHeight));
        } else {
            capacity.updateLowerBound(profile.getHeightRectangle(idxRectMaxHeight), this);
        }
    }

    /**
     * Builds and returns the reason explaining the rectangle at the index in the profile.
     *
     * @param indexRect the index of the rectangle in the profile
     * @return the reason explaining the rectangle
     */
    private Reason buildReasonRectangle(final int indexRect) {
        final int startRectangle = profile.getStartRectangle(indexRect);
        final int endRectangle = profile.getEndRectangle(indexRect);
        final TIntArrayList indexesTask = profile.getIndexesTaskRectangle(indexRect);
        final int[] literals = new int[2 + 3 * indexesTask.size()];
        int idx = 0;
        literals[++idx] = capacity.getLELit(capacity.getUB());
        for (int i = 0; i < indexesTask.size(); ++i) {
            final int idxTask = indexesTask.getQuick(i);
            literals[++idx] = tasks[idxTask].getEnd().getGELit(endRectangle);
            literals[++idx] = tasks[idxTask].getStart().getLELit(startRectangle);
            literals[++idx] = heights[idxTask].getGELit(heights[idxTask].getLB());
        }
        return Reason.r(literals);
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
                        reason = Reason.gather(reasonRect.get(j), task.getEnd().getGELit(profile.getStartRectangle(j) + 1));
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
                        reason = Reason.gather(reasonRect.get(j), task.getStart().getLELit(profile.getEndRectangle(j) - 1));
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
                        final Reason reason = Reason.gather(reasonRect.get(j), height.getGELit(height.getLB()));
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
                        ? Reason.r(heights[i].getGELit(capacity.getUB()), ((OptionalTask) tasks[i]).getPerformed().getMinLit())
                        : Reason.r(heights[i].getGELit(capacity.getUB()));
                tasks[i].updateDuration(0, 0, this, reason);
            } else {
                tasks[i].forceToBeOptional(this, Reason.r(heights[i].getGELit(capacity.getUB())));
            }
        } else if (tasks[i].mustBePerformed() && tasks[i].getMinDuration() > 0) {
            final int durationLit = tasks[i].getDuration().getGELit(1);
            final Reason reasonHeight;
            final Reason reasonCapacity;
            if (tasks[i] instanceof OptionalTask) {
                final int performedLit = ((OptionalTask) tasks[i]).getPerformed().getMinLit();
                reasonHeight = Reason.r(capacity.getMaxLit(), durationLit, performedLit);
                reasonCapacity = Reason.r(heights[i].getMinLit(), durationLit, performedLit);
            } else {
                reasonHeight = Reason.r(capacity.getMaxLit(), durationLit);
                reasonCapacity = Reason.r(heights[i].getMinLit(), durationLit);
            }
            heights[i].updateUpperBound(capacity.getUB(), this, reasonHeight);
            capacity.updateLowerBound(heights[i].getLB(), this, reasonCapacity);
        }
    }
}
