/**
 *  Copyright (c) 1999-2011, Ecole des Mines de Nantes
 *  All rights reserved.
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 *      * Redistributions of source code must retain the above copyright
 *        notice, this list of conditions and the following disclaimer.
 *      * Redistributions in binary form must reproduce the above copyright
 *        notice, this list of conditions and the following disclaimer in the
 *        documentation and/or other materials provided with the distribution.
 *      * Neither the name of the Ecole des Mines de Nantes nor the
 *        names of its contributors may be used to endorse or promote products
 *        derived from this software without specific prior written permission.
 *
 *  THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS ``AS IS'' AND ANY
 *  EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 *  DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS BE LIABLE FOR ANY
 *  DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 *  ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package memory.trailing.trail;

import java.io.Serializable;


/**
 * An interface for classes implementing trails of modifications to objects.
 * <p/>
 * Toutes les classes doivent implementer les fonctions de l'interface pour
 * permettre a l'environnement de deleguer la gestion des mondes pour chaque type
 * de donnee.
 */
public interface ITrailStorage extends Serializable {

    /**
     * Moving up to the next world.
     * <p/>
     * Cette methode doit garder l'etat de la variable avant la modification
     * de sorte a la remettre en etat le cas echeant.
     *
     * @param worldIndex
     */

    void worldPush(int worldIndex);


    /**
     * Moving down to the previous world.
     * <p/>
     * Cette methode reattribute a la variable ou l'element d'un tableau sa valeur
     * precedente.
     *
     * @param worldIndex
     */

    void worldPop(int worldIndex);


    /**
     * Comitting the current world: merging it with the previous one.
     * <p/>
     * Not used yet.
     */

    void worldCommit();


    /**
     * Retrieving the size of the trail (number of saved past values).
     */

    int getSize();

    /**
     * increase the capacity of the environment to a given number of worlds
     *
     * @param newWorldCapacity
     */
    void resizeWorldCapacity(int newWorldCapacity);


}

