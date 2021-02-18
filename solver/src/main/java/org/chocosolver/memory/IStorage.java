/*
 * This file is part of choco-solver, http://choco-solver.org/
 *
 * Copyright (c) 2021, IMT Atlantique. All rights reserved.
 *
 * Licensed under the BSD 4-clause license.
 *
 * See LICENSE file in the project root for full license information.
 */
package org.chocosolver.memory;




/**
 * An interface for classes implementing trails of modifications to objects.
 * <p/>
 * Toutes les classes doivent implementer les fonctions de l'interface pour
 * permettre a l'environnement de deleguer la gestion des mondes pour chaque type
 * de donnee.
 */
public interface IStorage  {

    /**
     * Moving up to the next world.
     * <p/>
     * Cette methode doit garder l'etat de la variable avant la modification
     * de sorte a la remettre en etat le cas echeant.
     *
     * @param worldIndex current world index
     */

    void worldPush(int worldIndex);


    /**
     * Moving down to the previous world.
     * <p/>
     * Cette methode reattribute a la variable ou l'element d'un tableau sa valeur
     * precedente.
     *
     * @param worldIndex current world index
     */

    void worldPop(int worldIndex);


    /**
     * Comitting the current world: merging it with the previous one.
     * <p/>
     * Not used yet.
     */

    void worldCommit(int worldIndex);

}

