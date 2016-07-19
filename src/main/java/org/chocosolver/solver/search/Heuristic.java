/**
 * 
 */
package org.chocosolver.solver.search;

import java.util.stream.Stream;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.strategy.strategy.AbstractStrategy;
import org.chocosolver.solver.search.strategy.strategy.StrategiesSequencer;

/**
 * A heuristic is an interface which produces a strategy from a {@link Model}
 * <p>
 * The {@link #chain(Heuristic<T>...)} method allow to chain several heuristic together. The resulting heuristic will
 * produce a sequence strategy of its elements' strategy
 * </p>
 *
 * @author Guillaume Le Louët [guillaume.lelouet@gmail.com] 2016
 */
@FunctionalInterface
public interface Heuristic<T extends Model> {

	public AbstractStrategy<?> makeStrat(T sc);

	public static class Chain<T extends Model> implements Heuristic<T> {

		private Heuristic<T>[] elements;

		@SafeVarargs
		public Chain(Heuristic<T>... elements) {
			this.elements = elements;
		}

		@Override
		public AbstractStrategy<?> makeStrat(T sc) {
			return new StrategiesSequencer(Stream.of(elements).map(h -> h.makeStrat(sc)).toArray(AbstractStrategy[]::new));
		}

	}

	/**
	 * chain this heuristic with another, or some other, ones. Perform checks to ensure there is no nested chain.
	 *
	 * @param followers
	 *          the heuristics to chain after this one ; can be null, in this case will return this.
	 * @return a new heuristic chaining this to the followers if followers are not null, this if no follower.
	 */
	@SuppressWarnings("unchecked")
	public default Heuristic<T> chain(Heuristic<T>... followers) {
		if (followers == null || followers.length == 0) {
			return this;
		}
		return new Heuristic.Chain<>(Stream.concat(Stream.of(this), Stream.of(followers))
				.flatMap(h -> h instanceof Heuristic.Chain ? Stream.of(((Heuristic.Chain<T>) h).elements) : Stream.of(h))
				.toArray(Heuristic[]::new));
	}
}