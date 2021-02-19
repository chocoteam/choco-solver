/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.constraints.nary.sat;

import gnu.trove.map.hash.TIntIntHashMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.search.loop.monitors.NogoodFromRestarts;
import org.chocosolver.solver.variables.Variable;

/**
 * This class manages no-goods sharing among models involved in a {@link
 * org.chocosolver.solver.ParallelPortfolio}. In this, we make the following hypothesis: all models
 * were created following the very same steps. The consequence is that a variable has the same ID in
 * all models.
 * <p>
 * Project: choco.
 *
 * @author Charles Prud'homme
 * @since 10/02/2020.
 */
public class NogoodStealer {

    /**
     * A singleton that steals nothing
     */
    public static final NogoodStealer NONE = new NogoodStealer() {
        @Override
        public void add(Model model) {
            // void
        }

        @Override
        public synchronized void nogoodStealing(Model model, NogoodFromRestarts caller) {
            // void
        }

        @Override
        public <V extends Variable> V getById(V var, Model model) {
            return var;
        }
    };

    /**
     * List of models to steal nogoods from
     */
    private final List<Model> models;

    /**
     * Maintain relation between id of a variable and its position in a model.
     * @implSpec This is a strong assumption that all models are equivalent (ie, each variable
     * has the same ID among models).
     */
    private final TIntIntHashMap id2pos;

    /**
     * Create a class that steal nogoods (based on decision path) from models and store them in
     * another one.
     */
    public NogoodStealer() {
        this.models = new ArrayList<>();
        this.id2pos = new TIntIntHashMap(10,.5f,-1,-1);
    }

    /**
     * Add a model to steal nogood from (based on decision path)
     * @param model
     */
    public void add(Model model) {
        assert valid(model): "Cannot share nogoods between non equivalent models";
        this.models.add(model);
    }

    private boolean valid(Model model) {
        if (this.models.size() > 0) {
            Variable[] vars0 = this.models.get(0).getVars();
            Variable[] vars1 = model.getVars();
            if (vars0.length == vars1.length) {
                for (int i = 0; i < vars0.length; i++) {
                    if (vars0[i].getId() != vars1[i].getId()
                        || !vars0[i].getName().equals(vars1[i].getName())
                        || vars0[i].getNbProps() != vars1[i].getNbProps()
                    ) {
                        return false;
                    }
                }
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Extract nogoods from decision paths of all models but <i>model</i>.
     * @param model the model to skip
     * @param caller nogoods extractor
     */
    public synchronized void nogoodStealing(Model model, NogoodFromRestarts caller) {
        for (Model m : models) {
            if (m != model) {
                caller.extractNogoodFromPath(m.getSolver().getDecisionPath());
            }
        }
    }

    /**
     * @param <V> type of variable to find
     * @param var variable to look for (based on its ID)
     * @param model prop
     * @return the variable equivalent to <i>var</i> to declared in <i>png</i>
     * (ie, from another model)
     */
    public <V extends Variable> V getById(V var, Model model) {
        int p = id2pos.get(var.getId());
        if(p == -1){
            p = binarySearch(model, var.getId());
        }
        //noinspection unchecked
        return (V) model.getVar(p);
    }

    /**
     * Adapted from {@link java.util.Arrays#binarySearch(Object[], Object, Comparator)}
     */
    private static <T> int binarySearch(Model model, int key) {
        int low = 0;
        int high = model.getNbVars();

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Variable midVal = model.getVar(mid);
            int cmp = midVal.getId() - key;
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid; // key found
            }
        }
        return -(low + 1);  // key not found.
    }

}
