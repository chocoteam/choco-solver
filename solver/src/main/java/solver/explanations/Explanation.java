package solver.explanations;

import solver.constraints.propagators.Propagator;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: njussien
 * Date: 26 oct. 2010
 * Time: 12:54:50
 * An explanation
 */
public class Explanation extends Deduction {
    Set<Deduction> deductions;
    Set<Propagator> contraintes;

    public Explanation(Set<Deduction> p, Set<Propagator> e ) {
        this.deductions = p;
        this.contraintes = e;
    }

    public void add(Explanation expl) {
        if (this.deductions == null) this.deductions = expl.deductions;
        else if (expl.deductions != null) this.deductions.addAll(expl.deductions);
        if (this.contraintes == null) this.contraintes = expl.contraintes;
        else if (expl.contraintes != null) this.contraintes.addAll(expl.contraintes);
    }

    public void add(Propagator p) {
        if (this.contraintes == null) {
            this.contraintes = new HashSet<Propagator>();
        }
        this.contraintes.add(p);
    }

    public void add(Deduction d) {
        if (this.deductions == null) {
            this.deductions = new HashSet<Deduction>();
        }
        this.deductions.add(d);
    }

    public void reset() {
        this.contraintes = null;
        this.deductions = null;
    }

}
