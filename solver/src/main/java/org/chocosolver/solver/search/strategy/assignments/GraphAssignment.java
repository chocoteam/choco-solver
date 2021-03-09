/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.search.strategy.assignments;

import org.chocosolver.solver.variables.GraphVar;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.exception.ContradictionException;

import java.io.Serializable;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 03/02/11
 */
public abstract class GraphAssignment implements Serializable {

	public abstract void apply(GraphVar var, int node, ICause cause) throws ContradictionException;

	public abstract void unapply(GraphVar var, int node, ICause cause) throws ContradictionException;

	public abstract void apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException;

	public abstract void unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException;

	public abstract String toString();

	public static GraphAssignment graph_enforcer = new GraphAssignment() {

		@Override
		public void apply(GraphVar var, int node, ICause cause) throws ContradictionException {
			if (node < var.getNbMaxNodes()) {
				var.enforceNode(node, cause);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public void unapply(GraphVar var, int node, ICause cause) throws ContradictionException {
			if (node < var.getNbMaxNodes()) {
				var.removeNode(node, cause);
			} else {
				throw new UnsupportedOperationException();
			}
		}

		@Override
		public void apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			if (from == -1 || to == -1) {
				throw new UnsupportedOperationException();
			}
			var.enforceEdge(from, to, cause);
		}

		@Override
		public void unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			if (from == -1 || to == -1) {
				throw new UnsupportedOperationException();
			}
			var.removeEdge(from, to, cause);
		}

		@Override
		public String toString() {
			return " enforcing ";
		}
	};

	public static GraphAssignment graph_remover = new GraphAssignment() {
		@Override
		public void apply(GraphVar var, int value, ICause cause) throws ContradictionException {
			graph_enforcer.unapply(var, value, cause);
		}

		@Override
		public void unapply(GraphVar var, int value, ICause cause) throws ContradictionException {
			graph_enforcer.apply(var, value, cause);
		}

		@Override
		public void apply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			graph_enforcer.unapply(var, from, to, cause);
		}

		@Override
		public void unapply(GraphVar var, int from, int to, ICause cause) throws ContradictionException {
			graph_enforcer.apply(var, from, to, cause);
		}

		@Override
		public String toString() {
			return " removal ";
		}
	};
}
