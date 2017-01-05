/**
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2017, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.solver.features;

import gnu.trove.map.hash.TObjectDoubleHashMap;
import org.chocosolver.solver.Model;

import java.util.Arrays;

/**
 * <p>
 * Project: choco-solver.
 *
 * @author Charles Prud'homme
 * @author Arnaud Malapert
 * @since 04/05/2016.
 */
public class Features implements IFeatures {

    /**
     * For serialization purpose
     */
    private static final long serialVersionUID = 1323411473753839215L;
    /**
     * Name of the model from which the features are extracted
     */
    private final String modelName;

    /**
     * List of attributes extracted on creation
     */
    private final TObjectDoubleHashMap<Attribute> values;

    /**
     * Make a "snapshot" of a model's features
     * @param model model to extract features from
     * @param attrs list of attribute to extract
     */
    public Features(Model model, Attribute... attrs) {
        this.modelName = model.getName();
        values = new TObjectDoubleHashMap<>(15, 05f);
        Arrays.stream(attrs).forEach(a -> values.put(a, a.evaluate(model)));
    }


    @Override
    public String getModelName() {
        return modelName;
    }

    @Override
    public int getNbVars() {
        return (int) getValue(Attribute.NV);
    }

    @Override
    public int getNbCstrs() {
        return (int) getValue(Attribute.NC);
    }

    @Override
    public double getValue(Attribute attribute) {
        return values.get(attribute);
    }

}
