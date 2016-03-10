package org.chocosolver.util.objects.setDataStructures.backtrackable;

import org.chocosolver.util.objects.setDataStructures.SetFactory;
import org.chocosolver.util.objects.setDataStructures.SetType;
import org.testng.annotations.BeforeMethod;

/**
 * @author Alexandre LEBRUN
 */
public class StoredBipartiteSetTest extends StoredBitSetTest {

    @BeforeMethod
    @Override
    public void setup() {
        super.setup();
        this.set = SetFactory.makeStoredSet(SetType.BIPARTITESET, 0, model);
    }
}
