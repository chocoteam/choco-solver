package solver.explanations;

import solver.variables.Variable;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 30 oct. 2010
 * Time: 16:26:28
 */
public class VariableAssignment extends Deduction {
    Variable var;
    int val;

    public VariableAssignment(Variable v, int vl) {
        this.var = v;
        this.val = vl;
    }
}
