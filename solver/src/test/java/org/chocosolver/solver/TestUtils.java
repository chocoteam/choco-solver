package org.chocosolver.solver;

import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.Task;

import static org.testng.Assert.assertEquals;

public class TestUtils {
    public static int[] getDomain(IntVar variable) {
        int[] values = new int[variable.getDomainSize()];
        int i = 0;
        for (int v = variable.getLB(); v <= variable.getUB(); v = variable.nextValue(v)) {
            values[i++] = v;
        }
        return values;
    }

    public static void testBounds(IntVar variable, int lb, int ub) {
        assertEquals(variable.getLB(), lb);
        assertEquals(variable.getUB(), ub);
    }

    public static void testBounds(Task task, int est, int lst, int ect, int lct) {
        assertEquals(task.getEst(), est);
        assertEquals(task.getLst(), lst);
        assertEquals(task.getEct(), ect);
        assertEquals(task.getLct(), lct);
    }
}
