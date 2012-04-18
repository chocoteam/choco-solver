package solver.constraints.propagators.nary.alldifferent.proba;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 */
public interface IUnion {

    void remove(int value);

    void instantiatedValue(int value, int low, int upp);

    int getSize();

    int getOccOf(int value);

    int getLastInstValuePos();

    int getLastLowValuePos();

    int getLastUppValuePos();
}
