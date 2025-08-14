package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import java.util.List;

public class Profile {
    private final int[] timePoints;
    private final int[] heights;
    private final EventPointSeries eventPointSeries;
    private int idx;

    public Profile(int nbTasks) {
        idx = 0;
        timePoints = new int[2 * (nbTasks + 1)];
        heights = new int[2 * (nbTasks + 1)];
        eventPointSeries = new EventPointSeries(nbTasks, 2);
    }

    public void clear() {
        idx = 0;
    }

    public int size() {
        return idx - 2;
    }

    public int getStartRectangle(int j) {
        return timePoints[j];
    }

    public int getEndRectangle(int j) {
        return timePoints[j + 1];
    }

    public int getHeightRectangle(int j) {
        return heights[j];
    }

    public int buildProfile(List<Task> tasks, List<IntVar> tasksHeights) {
        clear();
        timePoints[idx] = Integer.MIN_VALUE;
        heights[idx] = 0;
        idx++;
        int maxHeight = 0;
        eventPointSeries.generateEvents(tasks, false, false, false);
        if (!eventPointSeries.isEmpty()) {
            int h = 0;
            while (!eventPointSeries.isEmpty()) {
                timePoints[idx] = eventPointSeries.getEvent().getDate();
                while (!eventPointSeries.isEmpty() && eventPointSeries.getEvent().getDate() == timePoints[idx]) {
                    Event event = eventPointSeries.removeEvent();
                    h += (event.getType() == Event.SCP ? 1 : -1) * tasksHeights.get(event.getIndexTask()).getLB();
                }
                heights[idx] = h;
                idx++;
                maxHeight = Math.max(maxHeight, h);
                assert h >= 0;
            }
            assert h == 0;
        }
        timePoints[idx] = Integer.MAX_VALUE;
        heights[idx++] = 0;
        return maxHeight;
    }

    public int find(int date) {
        int i1 = 0;
        int i2 = idx - 2;
        while (i1 < i2) {
            int im = (i1 + i2) / 2;
            if (timePoints[im] <= date && date < timePoints[im + 1]) {
                i1 = im;
                i2 = im;
            } else if (timePoints[im] < date) {
                i1 = im + 1;
            } else if (timePoints[im] > date) {
                i2 = im - 1;
            }
        }
        return i1;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Profile[");
        if (size() > 0) {
            for (int i = 0; i < size(); i++) {
                sb.append("<").append(timePoints[i]).append(",").append(timePoints[i + 1]).append(",").append(heights[i]).append(">");
                if (i < size() - 1) {
                    sb.append(",");
                }
            }
            sb.append("<").append(timePoints[size()]).append(",").append(Integer.MAX_VALUE).append(",").append(0).append(">");
        } else {
            sb.append("<").append(Integer.MIN_VALUE).append(",").append(Integer.MAX_VALUE).append(",").append(0).append(">");
        }
        sb.append("]");
        return sb.toString();
    }
}
