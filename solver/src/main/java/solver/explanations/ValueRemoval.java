package solver.explanations;

import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 13:01:46
 */
public class ValueRemoval extends Deduction {
    IntVar var;
    int val ;

    public ValueRemoval(IntVar v, int n) {
        this.var = v;
        this.val = n;
    }
}
