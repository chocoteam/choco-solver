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

