/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2022, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.util.objects;

/**
 *
 * <p>
 * Project: choco-solver.
 * @author Charles Prud'homme
 * @since 30/10/2018.
 */
public interface IVal {

    double activity(int value);

    void setactivity(int value, double activity);

    void update(int nb_probes);

    void transfer();
}
