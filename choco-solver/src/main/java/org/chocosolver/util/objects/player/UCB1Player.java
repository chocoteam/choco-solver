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
/**
 * 
 */

/**
 * @author Amine Balafrej
 *
 */

package org.chocosolver.util.objects.player;




public class UCB1Player extends MultiArmedBanditPlayer{
	

	//score + margeErreur : average reward + sqrt(2 ln ...)
	private double ucb1Eval(int armIndex){ 
		return scors[armIndex]/armsTemps[armIndex]+Math.sqrt((2*Math.log(Temps))/armsTemps[armIndex]);
	}
    

    public UCB1Player(int nbArms, long seed){
    	super(nbArms, seed);
    }

    
    
    
    @Override
    public int chooseArm(){
    	//////////////////////////////////////
    	//shuffle(armsOrder);
    	//////////////////////////////////////
    	
	    int indexBestArm=armsOrder[0];
	    double UCB1OfBestArm=ucb1Eval(indexBestArm); // average reward + sqrt(2 ln ...)
	    double UCB1tmp;
	    for(int i=1;i<nbArms;i++){
	    	UCB1tmp=ucb1Eval(armsOrder[i]);
	    	if(UCB1tmp>UCB1OfBestArm){
	    		indexBestArm=armsOrder[i];
	    		UCB1OfBestArm=UCB1tmp;
	    	}
	    }	
		return indexBestArm;
    }

    @Override
    public void update(int arm, double reward){
    	Temps++;
		armsTemps[arm]++;
		scors[arm]+=reward;
    }

}

