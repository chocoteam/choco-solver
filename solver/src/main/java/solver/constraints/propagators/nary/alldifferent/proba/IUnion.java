package solver.constraints.propagators.nary.alldifferent.proba;

import solver.variables.IntVar;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public interface IUnion {

    void remove(int value, IntVar var);

    int[] instantiatedValue(int value, IntVar var);

    int getSize();

    int getOccOf(int value);

}
