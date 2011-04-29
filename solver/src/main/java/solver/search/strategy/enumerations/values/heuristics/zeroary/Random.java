package solver.search.strategy.enumerations.values.heuristics.zeroary;

import gnu.trove.THashMap;
import gnu.trove.TIntHashSet;
import solver.search.strategy.enumerations.values.heuristics.Action;
import solver.search.strategy.enumerations.values.heuristics.HeuristicVal;
import solver.variables.domain.IIntDomain;

public class Random extends HeuristicVal {
    long seed;
    IIntDomain domain;
    java.util.Random generator;
    TIntHashSet elts;

    private Random(Action action) {
        super(action);
    }

    public Random(IIntDomain domain) {
        this(domain, System.currentTimeMillis());
    }

    public Random(IIntDomain domain, Action action) {
        this(domain, System.currentTimeMillis(), action);
    }

    Random(IIntDomain domain, long seed) {
        super();
        this.seed = seed;
        this.domain = domain;
        this.generator = new java.util.Random(seed);
        elts = new TIntHashSet();
    }

    Random(IIntDomain domain, long seed, Action action) {
        super(action);
        this.seed = seed;
        this.domain = domain;
        this.generator = new java.util.Random(seed);
        elts = new TIntHashSet();
    }

    public boolean hasNext() {
        return domain.getSize() > elts.size();
    }

    public int next() {
        int n = generator.nextInt(domain.getSize() - elts.size());
        int j = domain.getLB();
        for (int i = 0; i < n;) {
            if (!elts.contains(j)) {
                i++;
            }
            j = domain.nextValue(j);
        }
        elts.add(j);
        return j;
    }

    public void remove() {
        throw new UnsupportedOperationException("Random.remove not implemented");
    }

    @Override
    protected void doUpdate(Action action) {
        this.generator = new java.util.Random(seed);
        elts.clear();
    }

    @Override
    public HeuristicVal duplicate(THashMap<HeuristicVal, HeuristicVal> map) {
        if (map.containsKey(this)) {
            return map.get(this);
        } else {
            Random duplicata = new Random(this.action);
            duplicata.seed = this.seed;
            //BEWARE: Cannot clone() generator...!! INCOHERENCE is at our door!
            duplicata.generator = this.generator;
            duplicata.elts = this.elts;
            map.put(this, duplicata);
            return duplicata;
        }
    }
}
