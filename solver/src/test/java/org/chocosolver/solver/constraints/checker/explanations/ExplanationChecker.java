/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.checker.explanations;

import org.chocosolver.solver.Cause;
import org.chocosolver.solver.ICause;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.constraints.Operator;
import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.constraints.binary.PropGreaterOrEqualX_YC;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.constraints.extension.TuplesFactory;
import org.chocosolver.solver.constraints.nary.clauses.ClauseBuilder;
import org.chocosolver.solver.constraints.nary.clauses.PropSignedClause;
import org.chocosolver.solver.constraints.nary.sum.PropSum;
import org.chocosolver.solver.constraints.reification.PropXeqYCReif;
import org.chocosolver.solver.constraints.reification.PropXltYCReif;
import org.chocosolver.solver.constraints.ternary.PropMaxBC;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.learn.*;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.ESat;
import org.chocosolver.util.objects.ValueSortedMap;
import org.chocosolver.util.objects.setDataStructures.iterable.IntIterableRangeSet;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <br/>
 *
 * @author Charles Prud'homme
 * @since 24/08/2020
 */
public class ExplanationChecker {

    private static final boolean DEBUG = false;

        @DataProvider(name = "propagators")
        public Object[][] getPropagators() {
            return new Object[][]{
                    {PropGreaterOrEqualX_YC.class, new Class[]{IntVar[].class, int.class}, new Object[]{2, null}},
                    {PropSum.class, new Class[]{IntVar[].class, int.class, Operator.class, int.class}, new Object[]{3, 1, "=", 0}},
                    {PropMaxBC.class, new Class[]{IntVar.class, IntVar.class, IntVar.class}, new Object[]{null}},
                    {PropXeqYCReif.class, new Class[]{IntVar.class, IntVar.class, int.class, BoolVar.class}, new Object[]{null, null,  null, null}},
                    {PropXltYCReif.class, new Class[]{IntVar.class, IntVar.class, int.class, BoolVar.class}, new Object[]{null, null,  null, null}},
            };
        }

        @Test(groups={"1s", "expl"}, timeOut=60000, dataProvider = "propagators")
        public void testClauses(Class<? extends Propagator<IntVar>> prop, Class[] parameterTypes, Object[] info)
                throws NoSuchMethodException,
                IllegalAccessException,
                InvocationTargetException,
                InstantiationException {
            PropagatorReflectorToolBox.mainLoop(DEBUG, prop, parameterTypes, info, 100,
                    this::checkClause);
        }


        private void checkClause(Propagator<IntVar> propagator, List<IntVar> variables, List<IntIterableRangeSet> domains) {
            if(DEBUG)System.out.printf("Propagator : %s, {%s}\n", propagator.toString(), domains.stream().map(IntIterableRangeSet::toString).collect(Collectors.toList()));
            Model model = propagator.getModel();
            Solver solver = model.getSolver();
            ContradictionException cex = solver.getContradictionException();
            // CONFIGURATION starts
            // 1. post the constraint
            model.post(new Constraint("To check", propagator));
            // 2. declare the explanation engine : to make sure events are correctly recorded
            // in the implications graph
            EventRecorder eventRecorder = new EventRecorder(solver);
            model.getClauseBuilder();
            model.getClauseConstraint();
            LazyImplications ig = (LazyImplications) eventRecorder.getGI().get();
            ExplanationForSignedClause tight = new ExplanationForSignedClause(ig);
            ExplanationForSignedClause general = new ExplanationForSignedClause(ig);
            // CONFIGURATION ends

            model.getEnvironment().worldPush();
            boolean failed = false;
            try {
                solver.propagate();
                // to make sure an explanation can be computed
            } catch (ContradictionException e) {
                if(DEBUG)System.out.print("trivially incorrect ...\n");
                failed = true;
            }
            int nevt = ig.size();
            int nvar = variables.size();
            if(nevt == nvar){
                if(DEBUG)System.out.print("no event generated ...\n");
                return;
            }
            if(DEBUG)System.out.printf("%d evts to check ...\n", nevt - nvar);
            int k = 0;
            int nb = nevt - nvar;
            HashMap<IntVar, IntIterableRangeSet>[][] literals = new HashMap[2][nb];
            int[] pivots = new int[nb];
            while(nevt > nvar) {
                // otherwise, compute one explanation per event triggered
                nevt--;
                //if(DEBUG)System.out.printf("%d. evaluate %s ...\n\n", (k + 1), ig.getEntry(nevt));
                cex.c = propagator;
                pivots[k] = nevt;
                cex.v = ig.getIntVarAt(nevt);

                // general explanation then
                literals[0][k] = explain(general, ig, cex, nevt, true);
                // tight explanation first
                literals[1][k] = explain(tight, ig, cex, nevt, false);
                // to make sure we iterate over all generated events
    //            ig.undoLastEvent();
                k++;
            }
            model.getEnvironment().worldPop();
            solver.setEventObserver(AbstractEventObserver.SILENT_OBSERVER);
            for(int i = 0 ; i < nb; i++){
                for(int j = 0; j < 2; j++) {
                    if(DEBUG)System.out.printf("check %s clause:\n", j == 0?"General":"Tight");
                    if(DEBUG)System.out.printf("evaluate %s ...\n\n", literals[j][i]);
                    checkProp1(model, propagator, variables, literals[j][i]);
                    checkProp2(model, propagator, variables, literals[j][i], ig, pivots[i], failed);
                }
            }
            if(!failed){
                solver.setEventObserver(eventRecorder);
                checkPropagation(solver);
            }
            if(DEBUG)System.out.print("\n");
        }

        private HashMap<IntVar, IntIterableRangeSet> explain(ExplanationForSignedClause e, LazyImplications ig,
                                                             ContradictionException cex, int p, boolean general){
            e.recycle();
            ValueSortedMap<IntVar> front = e.getFront();
            HashSet<IntVar> literals = e.getLiterals();

    //        ig.collectNodesFromConflict(cex, front);
            assert ig.getIntVarAt(p) == cex.v;
            front.put((IntVar) cex.v, p);
            int current = front.pollLastValue();
            ig.predecessorsOf(current, front);
            if(XParameters.PROOF){
                System.out.printf("%s explanation:", general?"General":"Tight");
                System.out.printf("\nCstr: %s\n", ig.getCauseAt(current));
                System.out.printf("Pivot: %s = %s\n", ig.getIntVarAt(current).getName(),
                        ig.getDomainAt(current));
            }
            ICause cause = ig.getCauseAt(current);
            if(current == -1 || general
                    && Propagator.class.isAssignableFrom(cause.getClass())
                    && !PropSignedClause.class.isAssignableFrom(cause.getClass())){
                Propagator<IntVar> propagator = (Propagator<IntVar>) cause;
                Propagator.defaultExplain(propagator, current, e);
            }else {
                cause.explain(current, e);
            }
            // todo: check reification
    //        front.removeIf(v -> !literals.containsKey(v));
            if(XParameters.PROOF){
                literals.forEach(v -> System.out.printf("(%s \u2208 %s) \u2228 ", v.getName(), v.getLit()));
                System.out.print("\n\n");
            }
            return literals.stream().collect(
                    Collectors.toMap(
                            v -> v,
                            v -> v.getLit().export(),
                            (s1, s2) -> {
                                s1.addAll(s2);
                                return s1;
                            },
                            HashMap::new
                    ));
        }


        private void checkProp1(Model model,
                                Propagator<IntVar> propagator,
                                List<IntVar> variables,
                                HashMap<IntVar, IntIterableRangeSet> clause){
            ClauseBuilder ngb = model.getClauseBuilder();
            // 1. check that all assignments that falsify the clause also falsify the propagator
            int[][] doms = new int[variables.size()][];
            for(int i = 0 ; i < variables.size(); i++){
                IntVar var = variables.get(i);
                IntIterableRangeSet set = new IntIterableRangeSet();
                IntIterableRangeSet cl = clause.get(var);
                IntIterableRangeSet dom = ngb.getInitialDomain(var);
                if(cl != null && !cl.isEmpty()){
                    set.copyFrom(cl);
                    set.flip(dom.min(), dom.max());
                    if(set.isEmpty())return; // we're ok,there couldn't be solution
                }else{
                    set = dom;
                }
                doms[i] = set.toArray();
            }
            Tuples tuples = TuplesFactory.generateTuples((t)-> true, true, doms);
            for(int[] t: tuples.toMatrix()){
    //            if(DEBUG)System.out.printf("check failure of %s ...\n", Arrays.toString(t));
                model.getEnvironment().worldPush();
                try{
                    for(int j = 0; j < t.length; j++){
                        variables.get(j).instantiateTo(t[j], Cause.Null);
                    }
                    Assert.assertEquals(propagator.isEntailed(), ESat.FALSE);
                }catch (ContradictionException cex){}
                model.getEnvironment().worldPop();
            }

        }


        private void checkProp2(Model model,
                                Propagator propagator,
                                List<IntVar> variables,
                                HashMap<IntVar, IntIterableRangeSet> clause,
                                LazyImplications ig,
                                int pivot, boolean hasFailed){
            if(clause.size() == 0)return;
            // 1. check that all assignments that satisfy the clause lead to the same conclusion
            ValueSortedMap<IntVar> front = new ValueSortedMap<>();
            ig.predecessorsOf(pivot, front);
            IntIterableRangeSet[] doms = new IntIterableRangeSet[variables.size()];
            for (int i = 0; i < variables.size(); i++) {
                doms[i] = new IntIterableRangeSet();
            }
            main:
            for(IntVar var : clause.keySet()) {
                for (int i = 0; i < variables.size(); i++) {
                    doms[i].copyFrom(ig.getDomainAt(front.getValue(variables.get(i))));
                    if (var == variables.get(i)) {
                        doms[i].retainAll(clause.get(var));
                    }
                    if (doms[i].isEmpty()) {
                        continue main;
                    }
                }
                // didn't skip, then, same cause leads to same consequence...
    //            if(DEBUG)System.out.printf("check failure of %s ...\n", Arrays.toString(t));
                model.getEnvironment().worldPush();
                try {
                    for (int j = 0; j < variables.size(); j++) {
                        variables.get(j).removeAllValuesBut(doms[j], Cause.Null);
                    }
                    propagator.setActive();
                    propagator.propagate(2); // todo check
                    Assert.assertNotEquals(propagator.isEntailed(), ESat.FALSE, "incorrect: "+propagator + " on " + Arrays.toString(doms));
                    Assert.assertFalse(hasFailed, "not failed: "+ propagator + " on " + Arrays.toString(doms));
                    IntIterableRangeSet ndom = new IntIterableRangeSet(var);
                    IntIterableRangeSet edom = ig.getDomainAt(pivot);
                    if (DEBUG) System.out.printf("comparing %s and %s ...\n", edom, ndom);
                } catch (ContradictionException cex) {
                    Assert.assertTrue(hasFailed, "failure: "+ propagator + " on " + Arrays.toString(doms));
                }
                model.getEnvironment().worldPop();
            }
        }

        private void checkPropagation(Solver solver) {
            if(DEBUG)System.out.printf("check nb of solutions ...\n");
            AbstractEventObserver evtObs = solver.getEventObserver();
            solver.hardReset();
            while(solver.solve()) {
            }
            long nsol = solver.getSolutionCount();
            long nnod = solver.getNodeCount();
            solver.hardReset();
            solver.setEventObserver(evtObs);
            solver.setLearningSignedClauses();
            while(solver.solve()) {
            }
            long nsole = solver.getSolutionCount();
            long nnode = solver.getNodeCount();
            Assert.assertEquals(nsole, nsol);
            if(DEBUG)System.out.printf("%d vs %d ...\n", nsol, nsole);
            if(DEBUG)System.out.printf("%d vs %d ...\n", nnod, nnode);
        }
}
