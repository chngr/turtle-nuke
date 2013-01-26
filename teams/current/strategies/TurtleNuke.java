package current.strategies;

import current.*;
import battlecode.common.*;

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
	
}
