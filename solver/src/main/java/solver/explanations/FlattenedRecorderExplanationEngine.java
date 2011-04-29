package solver.explanations;

import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 1 nov. 2010
 * Time: 09:27:43
 */
public class FlattenedRecorderExplanationEngine extends RecorderExplanationEngine {
    @Override
    public Deduction explain(IntVar var, int val) {
         return database.get(getValueRemoval(var, val));
    }
}
