package solver.search.strategy.enumerations.values.predicates;

import solver.search.strategy.enumerations.values.domains.HeuristicValDomain;
import solver.search.strategy.enumerations.values.domains.HeuristicValDomainImpl;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.variables.domain.IIntDomain;

public class Member extends Predicate {
    HeuristicValDomain domain;

    public Member(IIntDomain d) {
        super();
        domain = new HeuristicValDomainImpl(d);
    }

    public Member(IIntDomain d, Action action) {
        super(action);
        domain = new HeuristicValDomainImpl(d);
    }

    @Override
    public void update(Action action) {
        if (action.equals(this.action)) {
            domain.update();
        }
    }

    public boolean eval(int i) {
        return domain.contains(i);
    }
}
