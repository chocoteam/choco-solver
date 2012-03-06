package solver.constraints.nary.alldifferent;

/**
 * Created by IntelliJ IDEA.
 * User: chameau
 * Date: 02/03/12
 */
public class CounterProba {

    long neq;
    long alldiff;
    long allProp;

    public CounterProba() {
        this.neq = 0;
        this.alldiff = 0;
        this.allProp = 0;
    }

    public void incrNeq() {
        this.neq++;
    }

    public void incrAllDiff() {
        this.alldiff++;
    }

    public void incrAllProp() {
        this.allProp++;
    }

    public long getNbNeq() {
        return neq;
    }

    public long getNbAllDiff() {
        return alldiff;
    }

    public long getNbProp() {
        return allProp;
    }
}
