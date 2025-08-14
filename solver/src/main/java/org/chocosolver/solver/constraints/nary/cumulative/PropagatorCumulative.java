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

import gnu.trove.map.hash.TIntIntHashMap;
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
 *
 * <a href="https://doi.org/10.1007/978-3-319-23219-5_11">Gay, S., Hartert, R., and Schaus, P.: “Simple and Scalable Time-Table Filtering for the Cumulative Constraint”. In: Principles and Practice of Constraint Programming - 21st International Conference, CP 2015, Cork, Ireland, August 31 - September 4, 2015, Proceedings. Ed. by Gilles Pesant. Vol. 9255. Lecture Notes in Computer Science. Springer, 2015, pp. 149–157</a>
 * <br>
 * <a href="https://doi.org/10.1007/978-3-642-21311-3_22">Petr Vilım: “Timetable Edge Finding Filtering Algorithm for Discrete Cumulative Resources”. In: Integration of AI and OR Techniques in Constraint Programming for Combinatorial Optimization Problems - 8th International Conference, CPAIOR 2011, Berlin, Germany, May 23-27, 2011. Proceedings. Ed. by Tobias Achterberg and J. Christopher Beck. Vol. 6697. Lecture Notes in Computer Science. Springer, 2011, pp. 230–245</a>
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 19/10/2023
 */
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

    protected final Profile profile;
    // Pour l'overloadChecking
    protected final TIntIntHashMap ttAfter;
    protected final List<Integer> tasksWithFreeParts;

    public PropagatorCumulative(Task[] tasks, IntVar[] heights, IntVar capacity) {
        super(false, tasks, heights, capacity, PropagatorPriority.QUADRATIC, true, false);
        profile = new Profile(tasks.length);
        tasksWithFreeParts = new ArrayList<>(tasks.length);
        ttAfter = new TIntIntHashMap(2 * tasks.length);
    }

    protected void scalableTimeTable(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        boolean hasFiltered;
        do {
            buildProfile(tasks, heights);
            hasFiltered = scalableTimeTableFilter(tasks, heights);
        } while (hasFiltered);
    }

    protected void buildProfile(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        profile.buildProfile(tasks, heights);
        int maxHeight = profile.getHeightRectangle(0);
        for (int j = 1; j < profile.size(); j++) {
            maxHeight = Math.max(maxHeight, profile.getHeightRectangle(j));
        }
        capacity.updateLowerBound(maxHeight, this);
    }

    protected void updateHeights(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
        for (int i = 0; i < tasks.size(); i++) {
            final Task task = tasks.get(i);
            if (task.getLst() < task.getEct() && task.mustBePerformed()) {
                final IntVar height = heights.get(i);
                int j = profile.find(task.getLst());
                while (j < profile.size() && profile.getStartRectangle(j) < task.getEct()) {
                    height.updateUpperBound(capacity.getUB() - (profile.getHeightRectangle(j) - height.getLB()), this);
                    j++;
                }
            }
        }
    }

    protected boolean scalableTimeTableFilter(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
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

    protected boolean scalableTimeTableFilterEst(final Task task, final IntVar height) throws ContradictionException {
        boolean hasFiltered = false;
        if (!task.getStart().isInstantiated()) {
            int j = profile.find(task.getEst());
            while (j < profile.size() && profile.getStartRectangle(j) < Math.min(task.getEct(), task.getLst())) {
                if (capacity.getUB() - height.getLB() < profile.getHeightRectangle(j)) {
                    hasFiltered |= PropagatorResource.filterEst(task, height, Math.min(task.getLst(), profile.getEndRectangle(j)), this)
                            && PropagatorResource.mustBePerformed(task, height);
                }
                j++;
            }
        }
        return hasFiltered;
    }

    protected boolean scalableTimeTableFilterLct(final Task task, final IntVar height) throws ContradictionException {
        boolean hasFiltered = false;
        if (!task.getEnd().isInstantiated()) {
            int j = profile.find(task.getLct() - 1);
            while (j >= 1 && profile.getEndRectangle(j) > Math.max(task.getLst(), task.getEct())) {
                if (capacity.getUB() - height.getLB() < profile.getHeightRectangle(j)) {
                    hasFiltered |= PropagatorResource.filterLct(task, height, Math.max(profile.getStartRectangle(j), task.getEct()), this)
                            && PropagatorResource.mustBePerformed(task, height);
                }
                j--;
            }
        }
        return hasFiltered;
    }

    protected void computeTtAfter(final List<Task> tasks) {
        ttAfter.clear();
        for (int i = 0; i < tasks.size(); ++i) {
            final int est = tasks.get(i).getEst();
            final int lct = tasks.get(i).getLct();
            if (!ttAfter.containsKey(est) || !ttAfter.containsKey(lct)) {
                computeTtAfter(est, lct);
            }
        }
    }

    protected void computeTtAfter(final int est, final int lct) {
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

    protected void computeTasksWithFreeParts(final List<Task> tasks, final List<IntVar> heights) {
        tasksWithFreeParts.clear();
        for (int i = 0; i < tasks.size(); i++) {
            if (PropagatorResource.mustBePerformed(tasks.get(i), heights.get(i)) && getFreeDuration(tasks.get(i)) > 0) {
                tasksWithFreeParts.add(i);
            }
        }
        // Sort free parts
        tasksWithFreeParts.sort((i, j) -> compareTaskWithFreeParts(tasks, i, j));
    }

    protected void overloadChecking(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
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

    protected void filter(final List<Task> tasks, final List<IntVar> heights) throws ContradictionException {
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
