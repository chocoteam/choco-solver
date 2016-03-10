package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.Test;

/**
 * @author Alexandre LEBRUN
 */
public class SortedSetFactoryTest {


    @Test(groups = "1s", timeOut = 60000, expectedExceptions = UnsupportedOperationException.class)
    public void testCstIntervalSet() {
        SetFactory.makeStoredSet(SetType.FIXED_INTERVAL, 0, new Model());
    }

    @Test(groups = "1s", timeOut = 60000, expectedExceptions = UnsupportedOperationException.class)
    public void testCstArraySet() {
        SetFactory.makeStoredSet(SetType.FIXED_ARRAY, 0, new Model());
    }


}
