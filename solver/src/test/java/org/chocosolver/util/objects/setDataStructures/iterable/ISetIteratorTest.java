package org.chocosolver.util.objects.setDataStructures.iterable;

import org.chocosolver.solver.Model;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_BitSet;
import org.chocosolver.util.objects.setDataStructures.bitset.Set_Std_BitSet;
import org.chocosolver.util.objects.setDataStructures.constant.Set_CstInterval;
import org.chocosolver.util.objects.setDataStructures.constant.Set_FixedArray;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class ISetIteratorTest {
    @DataProvider()
    public Object[][] collect() {
        Model model = new Model();

        ISet set = new Set_BitSet(0);
        set.add(1);
        set.add(2);
        set.add(3);
        set.add(4);
        ISet set2 = new Set_Std_BitSet(model.getEnvironment(), 0);
        set2.add(1);
        set2.add(2);
        set2.add(3);
        set2.add(4);
        ISet set3 = new Set_CstInterval(1, 4);
        ISet set4 = new Set_FixedArray(new int[]{1, 2, 3, 4});
        ISet set5 = new IntIterableRangeSet(new int[]{1, 2, 3, 4});
        ISet set6 = new Set_BitSet(0);
        set6.add(2);
        set6.add(3);
        set6.add(5);
        set6.add(6);
        set6.add(11);
        ISet set7 = new Set_Std_BitSet(model.getEnvironment(), 0);
        set7.add(2);
        set7.add(3);
        set7.add(5);
        set7.add(6);
        set7.add(11);
        ISet set8 = new Set_FixedArray(new int[]{2, 3, 5, 6, 11});
        ISet set9 = new IntIterableRangeSet(new int[]{2, 3, 5, 6, 11});
        return new Object[][]{
                {set, "Set_BitSet", RESULT1},
                {set2, "Set_Std_BitSet", RESULT1},
                {set3, "Set_CstInterval", RESULT1},
                {set4, "Set_FixedArray", RESULT1},
                {set5, "IntIterableRangeSet", RESULT1},
                {set6, "Set_BitSet", RESULT2},
                {set7, "Set_Std_BitSet", RESULT2},
                {set8, "Set_FixedArray", RESULT2},
                {set9, "IntIterableRangeSet", RESULT2}
        };
    }

    private static final String RESULT1 = "(1,1)(1,2)(1,3)(1,4)" +
                                          "(2,1)(2,2)(2,3)(2,4)" +
                                          "(3,1)(3,2)(3,3)(3,4)" +
                                          "(4,1)(4,2)(4,3)(4,4)";
    private static final String RESULT2 = "(2,2)(2,3)(2,5)(2,6)(2,11)" +
                                          "(3,2)(3,3)(3,5)(3,6)(3,11)" +
                                          "(5,2)(5,3)(5,5)(5,6)(5,11)" +
                                          "(6,2)(6,3)(6,5)(6,6)(6,11)" +
                                          "(11,2)(11,3)(11,5)(11,6)(11,11)";

    @Test(groups = "1s", dataProvider = "collect")
    public void testSetIterator(ISet set, String type, String result) {
        StringBuilder sb = new StringBuilder();
        for (int v : set) {
            for (int w : set) {
                sb.append(String.format("(%d,%d)", v, w));
            }
        }
        Assert.assertEquals(sb.toString(), result);
    }
}
