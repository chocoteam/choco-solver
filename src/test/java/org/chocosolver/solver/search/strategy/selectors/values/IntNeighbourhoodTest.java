package org.chocosolver.solver.search.strategy.selectors.values;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Test for the neighbourhood search strategy
 *
 * @author Pierre Tassel
 * @since 10/10/2019
 */
public class IntNeighbourhoodTest {

    Model model;
    IntVar x;
    IntDomainMin valueSelector;
    Map<Integer, Integer> initialValue;

    @BeforeMethod
    public void setUp() {
        model = new Model();
        x = model.intVar("x", new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9});
        valueSelector = new IntDomainMin();
        initialValue = new HashMap<>();
    }

    @Test
    public void notInTheDomain(){
        initialValue.put(x.getId(), -1);
        IntNeighbourhood neighbourhood = new IntNeighbourhood(valueSelector, initialValue);
        assertTrue(x.getDomainSize() > 0, "x needs to have at least a value");
        int selected = neighbourhood.selectValue(x);
        assertEquals(initialValue.get(x.getId()).intValue(), -1, "the initial value attributed to x is -1");
        assertNotEquals(selected, -1, "-1 is not present in the domain");
        assertEquals(selected, 0, "we select the min of the domain if the default value is not present in the domain");
        assertEquals(selected, valueSelector.selectValue(x), "if the initial value is not in the domain, we use the selected strategy");
    }

    @Test
    public void inTheDomain() throws ContradictionException {
        initialValue.put(x.getId(), 5);
        IntNeighbourhood neighbourhood = new IntNeighbourhood(valueSelector, initialValue);
        assertTrue(x.getDomainSize() > 0, "x needs to have at least a value");
        int selected = neighbourhood.selectValue(x);
        assertEquals(initialValue.get(x.getId()).intValue(), 5, "the initial value attributed to x is 5");
        assertTrue(x.contains(5), "The domain of x contains 5");
        assertEquals(selected, 5, "We select the initial value");
        assertNotEquals(selected, valueSelector.selectValue(x), "as the initial value is in the domain, we use it and not the strategy");
        // we remove the initial value of the domain
        x.removeValue(5, Cause.Null);
        selected = neighbourhood.selectValue(x);
        assertEquals(initialValue.get(x.getId()).intValue(), 5, "the initial value attributed to x is still 5");
        assertFalse(x.contains(5), "The domain of x does not contain 5 anymore");
        assertNotEquals(selected, 5, "The initial value is not selected anymore");
        assertEquals(selected, valueSelector.selectValue(x), "we use the selected strategy as the initial value is not in the domain anymore");
    }

    @Test
    public void noInitialValue(){
        IntNeighbourhood neighbourhood = new IntNeighbourhood(valueSelector, initialValue);
        assertTrue(x.getDomainSize() > 0, "x needs to have at least a value");
        int selected = neighbourhood.selectValue(x);
        assertEquals(initialValue.size(), 0, "there is no initial value");
        assertEquals(selected, valueSelector.selectValue(x), "as we don't have an initial value, we use the selected strategy");
    }

    @Test
    public void notSelected(){
        IntNeighbourhood neighbourhood = new IntNeighbourhood(valueSelector, initialValue);
        assertTrue(x.getDomainSize() > 0, "x needs to have at least a value");
        int selected = neighbourhood.selectValue(x);
        assertEquals(initialValue.size(), 0, "there is no initial value");
        assertEquals(selected, valueSelector.selectValue(x), "as we don't have an initial value, we use the selected strategy");
    }

}