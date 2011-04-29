package solver.explanations;

import solver.ICause;
import solver.variables.IntVar;

import java.io.Serializable;
import java.util.BitSet;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:34:02
 *
 * A class to manage explanations. The default behavior is to do nothing !
 */
public class ExplanationEngine implements Serializable {


    public void removeValue(IntVar var, int val, ICause cause) {}
    public void updateLowerBound(IntVar intVar, int old, int value, ICause cause) {}
    public void updateUpperBound(IntVar intVar, int old, int value, ICause cause) {}
    public void instantiateTo(IntVar var, int val, ICause cause) {}

    public BitSet getRemovedValues(IntVar v) { return null; }

    public Deduction explain(IntVar var, int val) {return null; }



}
