package team097.strategies;

import team097.*;
import battlecode.common.*;

// Notes: this should be something like the team "Vegan Police"

public class TurtleNuke extends Strategy {

	private static final int FORTIFY_RADIUS = 2; // May want something more sophisticated, e.g. layers ## not currently used
	private int stage = 0; // Strategy stage; have this?
	
	public TurtleNuke(HQRobot HQ){
		super(HQ);
	}
	
	private int turnsToPickaxe = Upgrade.PICKAXE.numRounds;
	public void run() throws GameActionException {
		if (HQ.rc.isActive()) {
			if(stage == 0){
		    	  HQ.rc.researchUpgrade(Upgrade.PICKAXE);
		    	  if(--turnsToPickaxe == 0) stage = 1;
		      } else if(stage == 1){
		    	  Robot[] defenders = HQ.rc.senseNearbyGameObjects(Robot.class, 18, HQ.myTeam); //5x5; ## properly depends on FORTIFY_RADIUS
		    	  if(defenders.length >= 9){ //## ?
		    		  HQ.rc.researchUpgrade(Upgrade.NUKE);
		    	  } else {
		    		  if(HQ.spawn()){
		    			  HQ.sendInitializeMessage( HQ.buildFortifyMessage() );
		    		  } else {
		    			  HQ.rc.researchUpgrade(Upgrade.NUKE);
		    		  }
		    	  }
		    	  // ## instruct new robot to fortify
		     }
		 }
	}
	
	@Override
	public void checkStrategyChange() throws GameActionException {
		if(HQ.rc.senseEnemyNukeHalfDone() && HQ.rc.checkResearchProgress(Upgrade.NUKE) < Upgrade.NUKE.numRounds/2)
			HQ.setStrategy(HQ.rush);
			HQ.rush.researchDefusion = true; //##maybe?
	}

	@Override
	public void begin() {
		// TODO Auto-generated method stub
		
	}
	
}
