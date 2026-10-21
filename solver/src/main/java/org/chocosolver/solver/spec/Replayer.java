/*
 * This file is part of choco-solver, http://choco-solver.org/
 * Copyright (c) 1999, IMT Atlantique.
 * SPDX-License-Identifier: BSD-3-Clause.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.spec;

import org.chocosolver.sat.Literalizer;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.nary.cnf.ILogical;
import org.chocosolver.solver.constraints.nary.cnf.LogOp;
import org.chocosolver.solver.variables.Group;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.OptionalTask;
import org.chocosolver.solver.variables.Task;
import org.chocosolver.solver.variables.Variable;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * Replays the steps of a {@link ModelSpec} on a new model.
 * Not thread-safe: one replayer per instantiation.
 *
 * @author Charles Prud'homme
 */
final class Replayer extends IdTable {

    Replayer(Header header) {
        super(new Model(header.name(), header.settings()));
        model().setSeed(header.seed());
    }

    Resolver replay(List<Step> steps) {
        for (int i = 0; i < steps.size(); i++) {
            Step step = steps.get(i);
            try {
                apply(step);
            } catch (VirtualMachineError e) {
                // e.g., OutOfMemoryError: not a problem of the spec
                throw e;
            } catch (Throwable t) {
                throw new SpecException("Step #" + i + " (" + step + ") failed: " + t.getMessage(), t);
            }
        }
        return this;
    }

    private void apply(Step step) throws Throwable {
        Model model = model();
        if (step instanceof Step.Call c) {
            Class<?>[] types = c.method().rawParameterTypes();
            Arg[] args = c.rawArgs();
            Object[] values = new Object[args.length];
            for (int k = 0; k < args.length; k++) {
                values[k] = materialize(args[k], types[k]);
            }
            bindOutputs(c.rawOut(), c.method().invoke(model, values));
        } else if (step instanceof Step.Post p) {
            model.post(constraints(p.rawCstrs()));
        } else if (step instanceof Step.Reify r) {
            bind(r.out(), constraint(r.cstr()).reify());
        } else if (step instanceof Step.Link l) {
            Constraint c = constraint(l.cstr());
            switch (l.kind()) {
                case REIFY_WITH -> c.reifyWith(boolVar(l.bool()));
                case IMPLIES -> c.implies(boolVar(l.bool()));
                case IMPLIED_BY -> c.impliedBy(boolVar(l.bool()));
            }
        } else if (step instanceof Step.Unpost u) {
            model.unpost(constraints(u.rawCstrs()));
        } else if (step instanceof Step.NewTask t) {
            IntVar s = intVar(t.start());
            IntVar d = intVar(t.duration());
            IntVar e = intVar(t.end());
            bind(t.out(), t.performed() < 0 ? new Task(s, d, e) : new OptionalTask(s, d, e, boolVar(t.performed())));
        } else if (step instanceof Step.Custom c) {
            Constraint cstr = c.factory().apply(this);
            if (cstr == null) {
                throw new SpecException("Custom constraint " + c.name() + " built nothing");
            }
            if (c.out() >= 0) {
                bind(c.out(), cstr);
            }
        } else if (step instanceof Step.Hook h) {
            Object value = materialize(h.value(), Object.class);
            if (h.name() == null) {
                model.removeAllHooks();
            } else if (value == null) {
                model.removeHook(h.name());
            } else {
                model.addHook(h.name(), value);
            }
        } else if (step instanceof Step.Seed s) {
            model.setSeed(s.seed());
        } else if (step instanceof Step.Objective o) {
            if (o.var() < 0) {
                model.clearObjective();
            } else {
                model.setObjective(o.maximize(), get(o.var(), Variable.class));
            }
        } else if (step instanceof Step.Group g) {
            model.addGroup(new Group<>(g.name(), (Variable[]) materialize(g.vars(), Variable[].class)));
        } else {
            throw new SpecException("Unknown step " + step);
        }
    }

    private Constraint[] constraints(int[] cids) {
        Constraint[] cs = new Constraint[cids.length];
        for (int i = 0; i < cids.length; i++) {
            cs[i] = constraint(cids[i]);
        }
        return cs;
    }

    private Object materialize(Arg arg, Class<?> type) {
        Object value;
        if (arg instanceof Arg.Ref r) {
            value = get(r.id());
        } else if (arg instanceof Arg.RefArray a) {
            value = array(a.componentType(), a.rawIds());
        } else if (arg instanceof Arg.RefMatrix a) {
            int[][] rows = a.rawIds();
            Object[] matrix = (Object[]) Array.newInstance(a.componentType().arrayType(), rows.length);
            for (int i = 0; i < rows.length; i++) {
                matrix[i] = array(a.componentType(), rows[i]);
            }
            value = matrix;
        } else if (arg instanceof Arg.RefList l) {
            List<Object> list = new ArrayList<>(l.rawIds().length);
            for (int id : l.rawIds()) {
                list.add(get(id));
            }
            value = list;
        } else if (arg instanceof Arg.Const c) {
            value = c.value();
        } else if (arg instanceof Arg.Logic l) {
            value = logic(l);
        } else if (arg instanceof Arg.Literal l) {
            value = switch (l.kind()) {
                case BOOL -> new Literalizer.BoolLit(boolVar(l.var()));
                case INT_EQ -> new Literalizer.IntEqLit(intVar(l.var()), l.value());
                case INT_LE -> new Literalizer.IntLeLit(intVar(l.var()), l.value());
            };
        } else {
            throw new SpecException("Unknown argument " + arg);
        }
        if (value == null ? type.isPrimitive() : !Specs.wrap(type).isInstance(value)) {
            throw new SpecException("Argument " + arg + " is not a " + type.getSimpleName());
        }
        return value;
    }

    private Object[] array(Class<?> componentType, int[] aids) {
        Object[] array = (Object[]) Array.newInstance(componentType, aids.length);
        for (int i = 0; i < aids.length; i++) {
            try {
                array[i] = get(aids[i]);
            } catch (ArrayStoreException e) {
                throw new SpecException("Identifier " + aids[i] + " is bound to a "
                        + get(aids[i]).getClass().getSimpleName() + ", not a " + componentType.getSimpleName());
            }
        }
        return array;
    }

    private LogOp logic(Arg.Logic l) {
        Arg[] children = l.rawChildren();
        ILogical[] ops = new ILogical[children.length];
        for (int i = 0; i < children.length; i++) {
            ops[i] = children[i] instanceof Arg.Logic sub ? logic(sub) : boolVar(((Arg.Ref) children[i]).id());
        }
        if (l.and()) {
            return l.negated() ? LogOp.nand(ops) : LogOp.and(ops);
        }
        return l.negated() ? LogOp.nor(ops) : LogOp.or(ops);
    }

    private void bindOutputs(int[] out, Object result) {
        if (out.length == 0) {
            return;
        }
        if (result instanceof Object[] array) {
            List<Object> flat = new ArrayList<>(out.length);
            Specs.flatten(array, flat);
            if (flat.size() != out.length) {
                throw new SpecException("Expected " + out.length + " outputs, got " + flat.size());
            }
            for (int i = 0; i < out.length; i++) {
                if (out[i] >= 0) {
                    bind(out[i], flat.get(i));
                }
            }
        } else {
            if (out.length != 1) {
                throw new SpecException("Expected " + out.length + " outputs, got 1");
            }
            bind(out[0], result);
        }
    }

    private void bind(int id, Object o) {
        if (o == null) {
            throw new SpecException("Cannot bind identifier " + id + " to null");
        }
        Object prev = put(id, o);
        if (prev != null && prev != o) {
            throw new SpecException("Divergence: identifier " + id + " is bound to " + prev + ", now to " + o);
        }
    }
}
