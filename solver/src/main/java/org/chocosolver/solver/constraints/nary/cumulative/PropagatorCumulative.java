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
import org.chocosolver.sat.Reason;
import org.chocosolver.solver.constraints.Explained;
import org.chocosolver.solver.constraints.PropagatorPriority;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.events.PropagatorEventType;

import java.util.ArrayList;
import java.util.List;

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
public class PropagatorCumulative extends PropagatorResource {
    private static int getFreeDuration(Task task) {
        int pTT = Math.max(0, task.getEct() - task.getLst());
        return task.getDuration().getLB() - pTT;
    }

    private static int compareTaskWithFreeParts(List<Task> tasks, int i, int j) {
        if (tasks.get(i).getEst() == tasks.get(j).getEst()) {
            return tasks.get(i).getEst() + getFreeDuration(tasks.get(i))
                    - (tasks.get(j).getEst() + getFreeDuration(tasks.get(j)));
        }
        return tasks.get(i).getEst() - tasks.get(j).getEst();
    }

    private final Profile profile;
    // For the overloadChecking
    private final TIntIntHashMap ttAfter;
    private final List<Integer> tasksWithFreeParts;
    // For explanations
    private final TIntObjectHashMap<Reason> reasonRect;
    private final TIntObjectHashMap<Reason> reasonHeightRect;

    public PropagatorCumulative(Task[] tasks, IntVar[] heights, IntVar capacity) {
        super(false, tasks, heights, capacity, PropagatorPriority.QUADRATIC, true, false);
        profile = new Profile(tasks.length);
        tasksWithFreeParts = new ArrayList<>(tasks.length);
        ttAfter = new TIntIntHashMap(2 * tasks.length);
        reasonRect = new TIntObjectHashMap<>();
        reasonHeightRect = new TIntObjectHashMap<>();
    }

    private void scalableTimeTable(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        boolean hasFiltered;
        do {
            buildProfile(tasks, heights);
            hasFiltered = scalableTimeTableFilter(tasks, heights);
        } while (hasFiltered);
    }

    private void buildProfile(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        profile.buildProfile(tasks, heights);
        int idxRectMaxHeight = 0;
        for (int j = 0; j < profile.size(); j++) {
            if (profile.getHeightRectangle(idxRectMaxHeight) < profile.getHeightRectangle(j)) {
                idxRectMaxHeight = j;
            }
            if (this.getModel().getSettings().isLCG()) {
                reasonRect.put(j, buildReasonRectangle(j, tasks));
                reasonHeightRect.put(j, buildReasonHeightRectangle(j, tasks, heights));
            }
        }
        if (this.getModel().getSettings().isLCG()) {
            capacity.updateLowerBound(profile.getHeightRectangle(idxRectMaxHeight), this, reasonHeightRect.get(idxRectMaxHeight));
        } else {
            capacity.updateLowerBound(profile.getHeightRectangle(idxRectMaxHeight), this);
        }
    }

    private Reason buildReasonHeightRectangle(final int indexRect, final List<Task> tasks, final List<IntVar> heights) {
        final int startRectangle = profile.getStartRectangle(indexRect);
        final int endRectangle = profile.getEndRectangle(indexRect);
        final TIntArrayList indexesTask = profile.getIndexesTaskRectangle(indexRect);
        final int[] literals = new int[1 + 3 * indexesTask.size()];
        int idx = 0;
        for (int i = 0; i < indexesTask.size(); ++i) {
            final int idxTask = indexesTask.getQuick(i);
            literals[++idx] = tasks.get(idxTask).getEnd().getGELit(endRectangle);
            literals[++idx] = tasks.get(idxTask).getStart().getLELit(startRectangle);
            literals[++idx] = heights.get(idxTask).getGELit(heights.get(idxTask).getLB());
        }
        return Reason.r(literals);
    }

    private Reason buildReasonRectangle(final int indexRect, final List<Task> tasks) {
        final int startRectangle = profile.getStartRectangle(indexRect);
        final int endRectangle = profile.getEndRectangle(indexRect);
        final TIntArrayList indexesTask = profile.getIndexesTaskRectangle(indexRect);
        final int[] literals = new int[1 + 2 * indexesTask.size()];
        int idx = 0;
        for (int i = 0; i < indexesTask.size(); ++i) {
            literals[++idx] = tasks.get(indexesTask.getQuick(i)).getEnd().getGELit(endRectangle);
            literals[++idx] = tasks.get(indexesTask.getQuick(i)).getStart().getLELit(startRectangle);
        }
        return Reason.r(literals);
    }

    private void updateHeights(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        for (int i = 0; i < tasks.size(); i++) {
            final Task task = tasks.get(i);
            if (task.getLst() < task.getEct() && task.mustBePerformed()) {
                final IntVar height = heights.get(i);
                int j = profile.find(task.getLst());
                while (j < profile.size() && profile.getStartRectangle(j) < task.getEct()) {
                    if (this.getModel().getSettings().isLCG()) {
                        Reason reason = Reason.gather(
                                Reason.gather(reasonHeightRect.get(j), height.getGELit(height.getLB())),
                                capacity.getLELit(capacity.getUB())
                        );
                        height.updateUpperBound(capacity.getUB() - (profile.getHeightRectangle(j) - height.getLB()), this, reason);
                    } else {
                        height.updateUpperBound(capacity.getUB() - (profile.getHeightRectangle(j) - height.getLB()), this);
                    }
                    j++;
                }
            }
        }
    }

    private boolean scalableTimeTableFilter(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        // From PropCumulativeGay2015
        boolean hasFiltered = false;
        for (int i = 0; i < tasks.size(); i++) {
            Task task = tasks.get(i);
            if (scalableTimeTableFilterEst(task, heights.get(i))) {
                hasFiltered = true;
                task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            }
            if (scalableTimeTableFilterLct(task, heights.get(i))) {
                hasFiltered = true;
                task.propagate(PropagatorEventType.FULL_PROPAGATION.getMask());
            }
        }
        return hasFiltered;
    }

    private boolean scalableTimeTableFilterEst(final Task task, final IntVar height) throws ContradictionException {
        boolean hasFiltered = false;
        if (!task.getStart().isInstantiated()) {
            int j = profile.find(task.getEst());
            while (j < profile.size() && profile.getStartRectangle(j) < Math.min(task.getEct(), task.getLst())) {
                if (capacity.getUB() - height.getLB() < profile.getHeightRectangle(j)) {
                    if (this.getModel().getSettings().isLCG()) {
                        Reason reason = Reason.gather(reasonRect.get(j), task.getEnd().getGELit(profile.getStartRectangle(j) + 1));
                        hasFiltered |= PropagatorResource.filterEst(task, height, Math.min(task.getLst(), profile.getEndRectangle(j)), this, reason)
                                       && PropagatorResource.mustBePerformed(task, height);
                    } else {
                        hasFiltered |= PropagatorResource.filterEst(task, height, Math.min(task.getLst(), profile.getEndRectangle(j)), this)
                                       && PropagatorResource.mustBePerformed(task, height);
                    }
                }
                j++;
            }
        }
        return hasFiltered;
    }

    private boolean scalableTimeTableFilterLct(final Task task, final IntVar height) throws ContradictionException {
        boolean hasFiltered = false;
        if (!task.getEnd().isInstantiated()) {
            int j = profile.find(task.getLct() - 1);
            while (j >= 1 && profile.getEndRectangle(j) > Math.max(task.getLst(), task.getEct())) {
                if (capacity.getUB() - height.getLB() < profile.getHeightRectangle(j)) {
                    if (this.getModel().getSettings().isLCG()) {
                        Reason reason = Reason.gather(reasonRect.get(j), task.getStart().getLELit(profile.getEndRectangle(j) - 1));
                        hasFiltered |= PropagatorResource.filterLct(task, height, Math.max(profile.getStartRectangle(j), task.getEct()), this, reason)
                                       && PropagatorResource.mustBePerformed(task, height);
                    } else {
                        hasFiltered |= PropagatorResource.filterLct(task, height, Math.max(profile.getStartRectangle(j), task.getEct()), this)
                                       && PropagatorResource.mustBePerformed(task, height);
                    }
                }
                j--;
            }
        }
        return hasFiltered;
    }

    private void computeTtAfter(final List<Task> tasks) {
        ttAfter.clear();
        for (int i = 0; i < tasks.size(); ++i) {
            final int est = tasks.get(i).getEst();
            final int lct = tasks.get(i).getLct();
            if (!ttAfter.containsKey(est) || !ttAfter.containsKey(lct)) {
                computeTtAfter(est, lct);
            }
        }
    }

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

    private void computeTasksWithFreeParts(final List<Task> tasks, final List<IntVar> heights) {
        tasksWithFreeParts.clear();
        for (int i = 0; i < tasks.size(); i++) {
            if (PropagatorResource.mustBePerformed(tasks.get(i), heights.get(i)) && getFreeDuration(tasks.get(i)) > 0) {
                tasksWithFreeParts.add(i);
            }
        }
        // Sort free parts
        tasksWithFreeParts.sort((i, j) -> compareTaskWithFreeParts(tasks, i, j));
    }

    private void overloadChecking(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        // From PropCumulativeVilim2011
        computeTtAfter(tasks);
        computeTasksWithFreeParts(tasks, heights);
        int eEF;
        int a;
        int b;
        int lctB;
        for (int i = 0; i < tasksWithFreeParts.size(); i++) {
            b = tasksWithFreeParts.get(i);
            lctB = tasks.get(b).getLct();
            eEF = 0;
            for (int k = tasksWithFreeParts.size() - 1; k >= 0; k--) {
                a = tasksWithFreeParts.get(k);
                if (tasks.get(a).getLct() <= tasks.get(b).getLct() && PropagatorResource.mustBePerformed(tasks.get(a), heights.get(a))) {
                    eEF += getFreeDuration(tasks.get(a)) * heights.get(a).getLB();
                    if (capacity.getUB() * (lctB - tasks.get(a).getEst()) < eEF + ttAfter.get(tasks.get(a).getEst()) - ttAfter.get(lctB)) {
                        fails();
                        break;
                    }
                }
            }
        }
    }

    private void filter(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        scalableTimeTable(tasks, heights);
        overloadChecking(tasks, heights);
        updateHeights(tasks, heights);
    }

    @Override
    public void propagate(int evtmask) throws ContradictionException {
        computeMustBePerformedTasks();
        filter(performedAndOptionalTasks, tasksHeightsWithOptional);
    }
}
