package solver.constraints.nary.alldifferent;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 02/03/12
 */
public class CounterProba {

    long trigger; // number of times the proba says there is nothing to do.

    public CounterProba() {
        this.trigger = 0;
    }

    public void incr() {
        this.trigger++;
    }

    public long getValue() {
        return trigger;
    }
}
