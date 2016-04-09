/**
 * Copyright (c) 2015, Ecole des Mines de Nantes
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. All advertising materials mentioning features or use of this software
 *    must display the following acknowledgement:
 *    This product includes software developed by the <organization>.
 * 4. Neither the name of the <organization> nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY <COPYRIGHT HOLDER> ''AS IS'' AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.chocosolver.solver.constraints.nary.cumulative;

import org.chocosolver.solver.constraints.Propagator;
import org.chocosolver.solver.exception.ContradictionException;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.util.objects.setDataStructures.ISet;
import org.chocosolver.util.objects.setDataStructures.SetFactory;

/**
 * Time Table Edge Finding filtering algorithm for cumulative
 * @author Alban DERRIEN
 */
public class TTEFCumulFilter extends CumulFilter{

    //***********************************************************************************
   	// VARIABLES
   	//***********************************************************************************

    protected int nbTasks;
    private int[] varsHauteur, varsDuree, varsStartLB, varsStartUB, varsEndLB, varsEndUB;
    private int[] incStartLBIndex,incStartUBIndex,incEndLBIndex,incEndUBIndex;
    private int[] tmp;
    private int deltaMax; // valeur pour la symŽtrie pour faire le filtrage sur le max.

    private int[] ttAfterEst,ttAfterLct;
    private boolean[] isInTtt;
    private boolean[] isInTef;
    private int[][] eEFmatrix;

    private int [] startLbUpdate;
    int eEF,iota;
    private int []pEF;

    private IntVar[] vars;
    private IntVar capacity;
    private ISet tasksToFilter;

    //***********************************************************************************
   	// CONSTRUCTOR
   	//***********************************************************************************

    public TTEFCumulFilter(int nbMaxTasks, Propagator cause) {
        super(nbMaxTasks,cause);
        vars = new IntVar[nbMaxTasks*4];
        tasksToFilter = SetFactory.makeBipartiteSet(0);

        nbTasks = nbMaxTasks;
        varsHauteur = new int[nbTasks];
        varsDuree = new int[nbTasks];
        varsStartLB = new int[nbTasks];
        varsStartUB = new int[nbTasks];
        varsEndLB = new int[nbTasks];
        varsEndUB = new int[nbTasks];
        startLbUpdate = new int[nbTasks];
        ttAfterEst = new int[nbTasks];
        ttAfterLct = new int[nbTasks];

        pEF = new int[nbTasks];
        isInTtt = new boolean[nbTasks]; //si la tache est dans Ttt (a un partie obligatoire)
        isInTef = new boolean[nbTasks]; //si la tache est dans Tef (a un partie libre)
        eEFmatrix = new int[nbTasks][nbTasks];
    }

    @Override
    public void filter(IntVar[] s, IntVar[] d, IntVar[] e, IntVar[] h, IntVar capa, ISet tasks) throws ContradictionException {
        if(!capa.isInstantiated())return;
        int idx = 0;
        tasksToFilter.clear();
        for(int i:tasks){
            if(d[i].getLB()>0 && h[i].getLB()>0){
                tasksToFilter.add(i);
            }
        }
        nbTasks = tasksToFilter.getSize();
        if(nbTasks == 0)return;
        incStartLBIndex = new int[nbTasks];
        incStartUBIndex = new int[nbTasks];
        incEndLBIndex = new int[nbTasks];
        incEndUBIndex = new int[nbTasks];
        tmp  = new int[nbTasks];
        for(int i:tasksToFilter){
            vars[idx] = s[i];
            vars[idx+2*nbTasks] = e[i];
            varsDuree[idx] = d[i].getLB();
            varsHauteur[idx] = h[i].getLB();
            idx++;
        }
        capacity = capa;
        deltaMax=0;
        for(int i=0; i<nbTasks;i++){
            assert varsDuree[i] > 0;
            assert varsHauteur[i] > 0;
            deltaMax = Math.max(deltaMax, vars[i+2*nbTasks].getUB());
        }
        while(core(true) | core(false));
    }

    //***********************************************************************************
   	// METHODS
   	//***********************************************************************************

    private boolean core(boolean minOrMax) throws ContradictionException {
        boolean modified = false;
        if(minOrMax) {
            for (int i = 0; i < nbTasks; i++) {
                varsStartLB[i] = vars[i].getLB();
                varsStartUB[i] = vars[i].getUB();
                varsEndLB[i] = vars[i + 2 * nbTasks].getLB();
                varsEndUB[i] = vars[i + 2 * nbTasks].getUB();
            }
        }else {
            for (int i = 0; i < nbTasks; i++) {
                varsStartLB[i] = deltaMax - vars[i + 2 * nbTasks].getUB();
                varsStartUB[i] = deltaMax - vars[i + 2 * nbTasks].getLB();
                varsEndLB[i] = deltaMax - vars[i].getUB();
                varsEndUB[i] = deltaMax - vars[i].getLB();
            }
        }
        computeUpdate();
        for(int i= 0; i< nbTasks;i++){
            if(startLbUpdate[i]> varsStartLB[i]){
                modified = true;
                if(minOrMax) {
                    vars[i].updateLowerBound(startLbUpdate[i], aCause);
                }else{
                    vars[i+2*nbTasks].updateUpperBound(deltaMax-startLbUpdate[i], aCause);
                }
            }
        }
        return modified;
    }

    private void buildEEFmatrix() throws ContradictionException {
        for(int i=0;i<nbTasks;i++){
            for(int j=0;j<nbTasks;j++){
                eEFmatrix[i][j]=0;
            }
        }
        for(int b = 0; b<nbTasks ; b++){
            eEF = 0;
            if(isInTef[b]){ //b doit etre dans tEF
                int capa = capacity.getUB();
                for(int ia = nbTasks-1; ia>=0 ; ia--){
                    int a = incStartLBIndex[ia]; //a  in non-increasing StartTime (osef mais pour info: + breaked ties (on est+pEF))
                    if(isInTef[a]){ //a doit etre dans tEF
                        if(varsEndUB[a]<= varsEndUB[b]){
                            eEF += pEF[a]*varsHauteur[a];
                        }else{
                            //eEF += varsHauteur[a] * Math.max(0, varsEndUB[b] - (varsEndUB[a]-pEF[a]));
                            eEF += sureIn(varsEndUB[b],a);
                        }
                        eEFmatrix[a][b] = eEF;
                        //TODO remove!
                        int min = Math.min(varsStartLB[a],varsStartLB[b]);
                        int max = Math.max(varsEndUB[a], varsEndUB[b]);
                        if(capa*(max-min)<eEF) aCause.fails();
                        //System.out.println("------------"+eEFmatrix[a][b]+ "\t("+a+","+b+")");
                    }
                }
            }
        }
    }

    private void computeUpdate() throws ContradictionException{
        buildTT();
        buildEEFmatrix();
        int capa = capacity.getUB();

        for(int i=0;i<nbTasks;i++){
            startLbUpdate[i]= varsStartLB[i];
        }

        for(int b = 0; b<nbTasks ; b++){
            if(isInTef[b]){ //b doit etre dans tEF
                //cases "Inside" and "Right"
                eEF = 0;
                iota = -1;
                int myFirstElem = nbTasks-1;
                while(myFirstElem >0 && varsStartLB[incStartLBIndex[myFirstElem]] >= varsEndUB[b]) {
                    myFirstElem--;
                }

                for(int ia = myFirstElem; ia>= 0 ; ia--){
                    int a = incStartLBIndex[ia]; //a  in non-increasing StartTime
                    if(isInTef[a]){ //a doit etre dans tEF
                        if(varsEndUB[a]<=varsEndUB[b]){
                            eEF += pEF[a]*varsHauteur[a];
                        }else if(iota == -1 ||	varsHauteur[a]		* Math.min(pEF[a],		varsEndUB[b]-varsStartLB[a])   -sureIn(varsEndUB[b],a)   >
                                varsHauteur[iota]	* Math.min(pEF[iota],	varsEndUB[b]-varsStartLB[iota])-sureIn(varsEndUB[b],iota)){
                            iota = a;
                        }
                        //int reserve = capacity * (varsEndUB[b]-varsStartLB[a]) - eEF - (ttAfterEst[a] - ttAfterLct[b]);
                        int reserve = capa *(varsEndUB[b]-varsStartLB[a]) - eEFmatrix[a][b] - (ttAfterEst[a] - ttAfterLct[b]);
                        if(eEFmatrix[a][b] < eEF)
                            System.out.println(eEF + "  <  "+eEFmatrix[a][b]+ "\t("+a+","+b+")");
                        //if(iota != -1 && reserve < varsHauteur[iota] * (Math.min(pEF[iota],varsEndUB[b]-varsStartLB[iota]))){
                        if(iota != -1 && reserve < varsHauteur[iota] * (Math.min(pEF[iota],varsEndUB[b]-varsStartLB[iota])) -sureIn(varsEndUB[b],iota)){
                            //int update = varsEndUB[b] - mandatoryIn(varsStartLB[a],varsEndUB[b], iota) - reserve/varsHauteur[iota];
                            int update = varsEndUB[b] - mandatoryIn(varsStartLB[a],varsEndUB[b], iota) - (reserve+sureIn(varsEndUB[b],iota))/varsHauteur[iota];
                            if(startLbUpdate[iota]< update){
                                startLbUpdate[iota] = update;
                            }
                        }
                    }
                }//end for all a int non increasing order f start time in tEF

                //case "Through"
                iota = -1;
                for(int ia = 0; ia<nbTasks ; ia++){
                    int a = incStartLBIndex[ia]; //a  in non-decreasing StartTime + breaked ties (on est+pEF)
                    if(isInTef[a]){ //a doit etre dans tEF
                        if(varsEndUB[a]<=varsEndUB[b]){
                            int reserve = capa*(varsEndUB[b]-varsStartLB[a]) - eEF - (ttAfterEst[a] - ttAfterLct[b]);
                            if(iota != -1 && reserve < varsHauteur[iota] * (varsEndUB[b]-varsStartLB[a])){
                                int update =  varsEndUB[b] - mandatoryIn(varsStartLB[a],varsEndUB[b], iota) - reserve/varsHauteur[iota];
                                if(startLbUpdate[iota]< update){
                                    startLbUpdate[iota] = update;
                                }
                            }
                            eEF -= varsHauteur[a] * pEF[a];
                        }
                        if(varsStartLB[a] + pEF[a] >= varsEndUB[b]  && (iota == -1 || varsHauteur[a]>varsHauteur[iota])){
                            iota = a;
                        }
                    }
                }//end for all a int non decreasing order f start time in tEF
            }
        } //fin for all b + if inTEF

        //cases "Left"
        for(int a = 0; a<nbTasks ; a++){
            if(isInTef[a]){ //a doit etre dans tEF
                eEF = 0;
                iota = -1;
                //creation de Q contenant les activitŽs  dans tEF, par ordre croissant de est+pEF
                HeapIncreasingOrder Q = new HeapIncreasingOrder(nbTasks);
                for(int i= 0;i<nbTasks; i++){
                    if(isInTef[i]){
                        Q.add(varsStartLB[i]+pEF[i], i);
                    }
                }
                //Q est crŽe

                for(int ib = 0; ib<nbTasks ; ib++){
                    int b = incEndUBIndex[ib]; //b  in non-decreasing lct
                    if(isInTef[b]){ //a doit etre dans tEF
                        if(varsStartLB[a]<=varsStartLB[b]){
                            eEF += varsHauteur[b] * pEF[b];
                            int qTop = Q.getHeadTask();
                            while((!Q.isEmpty()) && varsStartLB[qTop] + pEF[qTop]< varsEndUB[b]){
                                Q.remove();
                                if(varsStartLB[qTop] < varsStartLB[a] && varsStartLB[a] < varsStartLB[qTop]+pEF[qTop] &&
                                        (iota == -1 || 	varsHauteur[qTop] * (varsStartLB[qTop] + pEF[qTop] - varsStartLB[a]) >
                                                varsHauteur[iota]  * (varsStartLB[iota]  + pEF[iota]  - varsStartLB[a]))){
                                    iota = qTop;
                                }
                                qTop =Q.getHeadTask();
                            }
                            int reserve = capa * (varsEndUB[b] - varsStartLB[a]) - eEF - (ttAfterEst[a] - ttAfterLct[b]);
                            if(iota != -1 && reserve < varsHauteur[iota] * (varsStartLB[iota] + pEF[iota] - varsStartLB[a])){
                                int update =  varsEndUB[b] - mandatoryIn(varsStartLB[a],varsEndUB[b], iota) - reserve/varsHauteur[iota];
                                if(startLbUpdate[iota]< update){
                                    startLbUpdate[iota] = update;
                                }
                            }
                        }
                    }
                }
            }
        }//end for all a in tEF
    }

    private int mandatoryIn(int start, int end, int activity){
        return Math.max(0, Math.min(end, varsEndLB[activity]) - Math.max(start, varsStartUB[activity]));
    }

    private int sureIn(int end, int activity){
        return varsHauteur[activity] * Math.max(0, end - (varsEndUB[activity]-pEF[activity]));
    }

    private void buildTT() throws ContradictionException{
        //on creer la liste des taches ayant une partie obligatoire
        for(int i=0;i<nbTasks;i++){
            if(varsStartUB[i]<varsEndLB[i]){//a une PO
                isInTtt[i] = true;
                if(varsStartLB[i]<varsStartUB[i]){//n'est pas fixŽ
                    isInTef[i] = true;
                    pEF[i] = varsStartUB[i]-varsStartLB[i];
                }else{
                    isInTef[i] = false;
                    pEF[i] = 0;
                }
            }else{
                isInTef[i] = true;
                isInTtt[i] = false;
                pEF[i] = varsDuree[i];
            }
        }
        //on commence par trier les taches par leurs 4 valeurs.
        for(int i=0;i<nbTasks;i++){
            incStartLBIndex[i]=i;
            tmp[i] = varsStartLB[i];
        }
        incMultipleSort(tmp,incStartLBIndex);
        breakTiesOnNonIncESTplusPEF();
        for(int i=0;i<nbTasks;i++){
            incStartUBIndex[i]=i;
            tmp[i] = varsStartUB[i];
        }
        incMultipleSort(tmp,incStartUBIndex);
        for(int i=0;i<nbTasks;i++){
            incEndLBIndex[i]=i;
            tmp[i] = varsEndLB[i];
        }
        incMultipleSort(tmp,incEndLBIndex);
        for(int i=0;i<nbTasks;i++){
            incEndUBIndex[i]=i;
            tmp[i] = varsEndUB[i];
        }
        incMultipleSort(tmp,incEndUBIndex);

        //puis on pars les evenements, et on construit le profile TT.
        int iSlb=nbTasks-1,iSub=nbTasks-1,iElb=nbTasks-1,iEub=nbTasks-1;
        //en parcourant les evenements par ordre anti chronologique.
        boolean continuer = true;
        int iBest;
        int tMax;
        int tPreced  = varsEndUB[incEndUBIndex[iEub]]; //la date la plus grande est forcement celle ci, on init le balayge ici.
        int ttAftert = 0;
        int penteAtt = 0;
        int capa = capacity.getUB();
        while(continuer){
            if(iSlb>=0){
                iBest = 1;//
                tMax = varsStartLB[incStartLBIndex[iSlb]];
            }else{
                //si on a pas pu l'init, on le met a -1 (il sera init apres)
                iBest = -1;
                tMax = -1;
            }
            if(iSub>=0 && varsStartUB[incStartUBIndex[iSub]]>tMax){
                tMax = varsStartUB[incStartUBIndex[iSub]];
                iBest = 2;
            }
            if(iElb>=0 && varsEndLB[incEndLBIndex[iElb]]>tMax){
                tMax = varsEndLB[incEndLBIndex[iElb]];
                iBest = 3;
            }
            if(iEub>=0 && varsEndUB[incEndUBIndex[iEub]]>tMax){
                tMax = varsEndUB[incEndUBIndex[iEub]];
                iBest = 4;
            }
            //on sait qu'elle est le temps max (l'evenement a traiter donc)
            if(iBest == -1){
                break;//si on a rien trouvŽ, alors, on a fini le profile => on sort.
            }
            ttAftert += penteAtt * (tPreced - tMax);//on met a jour TT
            tPreced = tMax;//on met a jour t
            if(iBest == 1){
                //cas 1 c'est startLB => on saisie l'info.
                ttAfterEst[incStartLBIndex[iSlb]] = ttAftert;
                //System.out.println("au temps:"+tMax+"(est"+incStartLBIndex[iSlb]+")\tle TT vaut:"+ttAftert+" "+vars[incStartLBIndex[iSlb]]);
                iSlb--;

            }else if(iBest == 2){
                //StartUB, on change le profile. si la tache en question a une Partie Obligatoire.
                if(isInTtt[incStartUBIndex[iSub]]){
                    penteAtt -= varsHauteur[incStartUBIndex[iSub]];
                    //System.out.println("au temps:"+tMax+"(lst"+incStartUBIndex[iSub]+")\tla pente vaut:"+penteAtt+" :-"+varsHauteur[incStartUBIndex[iSub]]);
                }
                iSub--;

            }else if(iBest == 3){
                if(isInTtt[incEndLBIndex[iElb]]){
                    penteAtt += varsHauteur[incEndLBIndex[iElb]];
                    //System.out.println("au temps:"+tMax+"(ect"+incEndLBIndex[iElb]+")\tla pente vaut:"+penteAtt+" :+"+varsHauteur[incEndLBIndex[iElb]]);
                }
                iElb--;
            }else { // equals 4
                //cas 1 c'est endUB => on saisie l'info.
                ttAfterLct[incEndUBIndex[iEub]] = ttAftert;
                iEub--;
            }
            if( penteAtt >capa){
                aCause.fails();
            }
        }
    }

    private void breakTiesOnNonIncESTplusPEF(){
        int iA,iB;
        for(iA=0;iA<nbTasks;iA++){
            int a = incStartLBIndex[iA];
            //on recherche l'indice du plus grand ayant une date differente, pour effectuŽ le trie sur la sous partie.
            for(iB=iA+1 ;iB<nbTasks;iB++){
                if(varsStartLB[a] != varsStartLB[incStartLBIndex[iB]]){
                    // si c'est different, on arrete
                    break;
                }
            }
            iB--;//pour retomber sur le dernier identique.

            for(int iU=iB; iU>=iA;iU--){
                int u = incStartLBIndex[iU];
                for(int iV=iA;iV<iU;iV++){
                    int v = incStartLBIndex[iV];
                    //on fait remontŽ le plus petit
                    if(pEF[u] < pEF[v]){
                        int tmp = incStartLBIndex[iU];
                        incStartLBIndex[iU] = incStartLBIndex[iV];
                        incStartLBIndex[iV] = tmp;
                    }
                }
            }
        }
    }

    /*
	 * sort the mainTab in increasing order,
	 * and do the same swap to other tabs
	 */
    private void incMultipleSort(int []mainTab, int[] ...otherTabs){
        int size = mainTab.length;
        int[] range = new int[size + 1];
        range[0] = size - 1;
        int i, j, sortedCount = 0;
        while (sortedCount < size) {
            for (i = 0; i < size; i++)
                if (range[i] >= i) {
                    j = range[i];
                    if (j - i < 7) {
                        // selectionsort the elements from date[i] to date[j]
                        // inclusive
                        // and set all their ranges to -((j+1)-k)
                        for (int m = i; m <= j; m++) {
                            for (int n = m; n > i && mainTab[n - 1] > mainTab[n]; n--)
                                swap(n, n - 1,mainTab,otherTabs);
                            range[m] = -((j + 1) - m);
                            sortedCount++;
                        }
                        i = j;
                    } else {
                        for (; i <= j; i++) {
                            int p = partitionInc(i, j,mainTab,otherTabs);
                            sortedCount++;
                            if (p > i)
                                range[i] = p - 1;
                            if (p < j)
                                range[p + 1] = j;
                            range[i = p] = -1; // sorted
                        }
                    }
                } else {
                    // skip[i] += skip[i + skip[i]];
                    while ((j = range[i - range[i]]) < 0)
                        range[i] += j;
                    i += -range[i] - 1;
                }
        }
    }

    private int partitionInc(int left, int right, int[]mainTab,int[] ...otherTabs) {
        // DK: added check if (left == right):
        if (left == right)
            return left;
        int i = left - 1;
        int j = right;
        while (true) {
            while (mainTab[++i] < mainTab[right])
                // find item on left to swap
                ; // a[right] acts as sentinel
            while (mainTab[right] < mainTab[--j])
                // find item on right to swap
                if (j == left)
                    break; // don't go out-of-bounds
            if (i >= j)
                break; // check if pointers cross
            swap(i, j,mainTab,otherTabs); // swap two elements into place
        }
        swap(i, right,mainTab,otherTabs); // swap with partition element
        return i;
    }

    private void swap(int i, int j,int[]mainTab,int[] ...otherTabs) {
        int tmp = mainTab[i];
        mainTab[i] = mainTab[j];
        mainTab[j] = tmp;
        for(int k=0; k<otherTabs.length;k++){
            tmp = otherTabs[k][i];
            otherTabs[k][i] = otherTabs[k][j];
            otherTabs[k][j] = tmp;
        }
    }
}