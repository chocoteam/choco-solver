/**
 * 
 */

/**
 * @author Amine Balafrej
 *
 */

package org.chocosolver.util.objects.player;



public class CPUPlayer extends UCB1Player{
	double MAX;
	
	
    public CPUPlayer(int nbArms){
    	super(nbArms);
    }

    @Override
    public void update(int arm, double CPUtime){ 
		MAX=Math.max(MAX, CPUtime);
		super.update(arm, 1.0-(CPUtime/MAX));
    }

  
}

