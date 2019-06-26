/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2019, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.cumulative.literature.gingras2016;

import gnu.trove.list.array.TIntArrayList;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

/**
 * Data structure for the cumulative constraint, described in the following paper :
 * Gingras, V., Quimper, C.-G.: Generalizing the edge-finder rule for the cumulative constraint. In: Proceedings of the 25th International Joint Conference on Artificial Intelligence (IJCAI 2016), pp. 3103–3109 (2016). https://www.ijcai.org/Proceedings/16/Papers/440.pdf
 *
 * @author Arthur Godet <arth.godet@gmail.com>
 * @since 23/05/2019
 */
class Profile {
    class TimePoint {
        int startTime;
        int ov;
        int capacity;
        int deltaMax;
        int deltaReq;                     
        TimePoint previous;
        TimePoint next;

        TimePoint(int startTime, int capacity) {
            this.startTime = startTime;
            this.capacity = capacity;
            this.ov = 0;
            this.deltaMax = 0;
            this.deltaReq = 0;
        }
    }

    private Task[] tasks;
    private IntVar[] heights;
    private IntVar capacity;
    TimePoint first;
    TimePoint last;
    TimePoint sentinel;
    HashMap<Integer, TimePoint> T;
    ArrayList<TimePoint> list;

    // used for scheduleTasks() method
    private int[] scheduleTasksRes = new int[2];

    Profile(Task[] tasks, IntVar[] heights, IntVar capacity) {
        this.tasks = tasks;
        this.heights = heights;
        this.capacity = capacity;

        this.T = new HashMap<>(4*tasks.length+1);
        list = new ArrayList<>(4*tasks.length+1);

        int tmp = 0;
        for(Task t : tasks) {
            tmp = Math.max(t.getEnd().getUB(), tmp);
        }
        sentinel = new TimePoint(tmp+1, capacity.getUB());

        initialize();
    }

    void initialize() {
        T.clear();
        list.clear();
        for(Task task : tasks) {
            int t = task.getStart().getLB(); // est
            if(!T.containsKey(t)) {
                TimePoint tp = new TimePoint(t, capacity.getUB());
                T.put(t, tp);
                list.add(tp);
            }

            t = task.getEnd().getLB(); // ect
            if(!T.containsKey(t)) {
                TimePoint tp = new TimePoint(t, capacity.getUB());
                T.put(t, tp);
                list.add(tp);
            }

            t = task.getEnd().getUB(); // lct
            if(!T.containsKey(t)) {
                TimePoint tp = new TimePoint(t, capacity.getUB());
                T.put(t, tp);
                list.add(tp);
            }
        }

        sentinel.capacity = capacity.getUB();
        sentinel.deltaMax = 0;
        sentinel.deltaReq = 0;

        first = null;
        list.sort(Comparator.comparingInt(tp -> tp.startTime));
        for(TimePoint current : list) {
            if(first == null) {
                first = current;
            } else {
                current.previous = last;
                last.next = current;
            }
            last = current;
        }
        sentinel.previous = last;
        last.next = sentinel;
    }


    public int[] scheduleTasks(TIntArrayList theta, int c) {
        if(theta == null || theta.size() == 0) {
            throw new UnsupportedOperationException("theta should not be null or empty to scheduleTasks");
        }

        for(TimePoint tp : T.values()) {
            tp.deltaMax = 0;
            tp.deltaReq = 0;
            tp.capacity = capacity.getUB();
            tp.ov = 0;
        }

        int lctTheta = tasks[theta.getQuick(0)].getEnd().getUB();
        for(int i = 0; i<theta.size(); i++) {
            Task task = tasks[theta.getQuick(i)];
            int h = heights[theta.getQuick(i)].getLB();

            TimePoint t = T.get(task.getStart().getLB());
            t.deltaMax += h;
            t.deltaReq += h;

            T.get(task.getEnd().getUB()).deltaMax -= h;
            T.get(task.getEnd().getLB()).deltaReq -= h;

            lctTheta = Math.max(lctTheta, task.getEnd().getUB());
        }

        TimePoint t = first;
        int ov = 0, ect = Integer.MIN_VALUE, S = 0, hReq = 0;
        while(t.startTime != lctTheta) {
            t.ov = ov;
            int l = t.next.startTime - t.startTime;
            S += t.deltaMax;
            int hMax = Math.min(S, c);
            hReq += t.deltaReq;
            int hCons = Math.min(hReq+ov, hMax);
            if(0<ov && ov<(hCons-hReq)*l) {
                l = Math.max(1, ov/(hCons-hReq));
                if(!T.containsKey(t.startTime+l)) {
                    TimePoint tp = new TimePoint(t.startTime+l, t.capacity);
                    tp.previous = t;
                    tp.next = t.next;
                    t.next = tp;
                }
            }
            ov += (hReq-hCons)*l;
            t.capacity = c-hCons;
            if(t.capacity < c) {
                ect = t.next.startTime;
            }
            t = t.next;
        }
        t.ov = ov;
        int m = Integer.MAX_VALUE;
        while(t!=first && m>0) {
            m = Math.min(m, t.ov);
            t.ov = m;
            t = t.previous;
        }
        scheduleTasksRes[0] = ect;
        scheduleTasksRes[1] = ov;
        return scheduleTasksRes;
    }

    public TIntArrayList detectPrecedences(TIntArrayList omega, TIntArrayList theta, TIntArrayList lambdaH, int h, int lct) {
        for(TimePoint tp : T.values()) {
            tp.deltaMax = 0;
        }
        for(int i = 0; i<theta.size(); i++) {
            int taskIdx = theta.getQuick(i);
            T.get(tasks[taskIdx].getStart().getLB()).deltaMax -= heights[taskIdx].getLB();
            T.get(tasks[taskIdx].getEnd().getUB()).deltaMax += heights[taskIdx].getLB();
        }
        int minest = Integer.MAX_VALUE;
        for(int i = 0; i<lambdaH.size(); i++) {
            minest = Math.min(minest, tasks[lambdaH.getQuick(i)].getStart().getLB());
        }
        TimePoint t = T.get(lct).previous;
        omega.clear();
        int e = 0, ov = 0, hMax = h;
        while(t!=null && t.startTime >= minest) {
            int l = t.next.startTime - t.startTime;
            hMax += t.next.deltaMax;
            int c = Math.min(t.capacity, hMax-(capacity.getUB()-t.capacity));
            e += l*Math.min(c, h) + Math.max(0, Math.min(ov, (h-c)*l));
            ov = Math.max(0, ov+l*(c-h));
            for(int i = 0; i<lambdaH.size(); i++) {
                int j = lambdaH.getQuick(i);
                int ej = tasks[j].getDuration().getLB()*heights[j].getLB(),
                        estj = tasks[j].getStart().getLB(),
                        ectj = tasks[j].getEnd().getLB();
                if(estj==t.startTime && ej-Math.min(0, heights[j].getLB()*(ectj-lct))>e) {
                    omega.add(j);
                }
            }
            t = t.previous;
        }
        return omega;
    }

    public int computeBound(int i, TIntArrayList theta, int ovMax) {
        for(TimePoint tp : T.values()) {
            tp.deltaMax = 0;
            tp.deltaReq = 0;
        }
        for(int j = 0; j<theta.size(); j++) {
            int taskIdx = theta.getQuick(j);
            Task task = tasks[taskIdx];
            TimePoint tp = T.get(task.getStart().getLB());
            tp.deltaMax += heights[taskIdx].getLB();
            tp.deltaReq += heights[taskIdx].getLB();

            T.get(task.getEnd().getUB()).deltaMax -= heights[taskIdx].getLB();
            T.get(task.getEnd().getLB()).deltaReq -= heights[taskIdx].getLB();
        }
        TimePoint t = first;
        while(t.next != null && t.ov <= 0) {
            t = t.next;
        }
        int ov = 0, dTotal = 0, S = 0, hReq = 0;
        while(t.next != null) {
            int l = t.next.startTime - t.startTime;
            S += t.deltaMax;
            int hMax = Math.min(S, capacity.getUB());
            hReq += t.deltaReq;
            int hCons = Math.min(hReq+ov, hMax);
            if(0<ov && ov<(hCons-hReq)*l) {
                l = Math.max(ov/(hCons-hReq), 1);
            }
            int d = (hCons-capacity.getUB()+heights[i].getLB())*l;
            d = Math.max(0, Math.min(Math.min(d, ovMax-dTotal), t.next.ov-dTotal));
            if(dTotal+d == ovMax) {
                if(hCons-(capacity.getUB()-heights[i].getLB()) == 0) {
                    return t.next.startTime;
                } else {
                    return Math.min(t.next.startTime, t.startTime+(int)Math.ceil(d/(hCons-(capacity.getUB()-heights[i].getLB()))));
                }
            }
            dTotal += d;
            ov += (hReq-hCons)*l;
            if(t.startTime+l<t.next.startTime) {
                t.startTime = t.startTime+l;
            } else {
                t = t.next;
            }
        }

        return -Integer.MAX_VALUE;
    }

}
