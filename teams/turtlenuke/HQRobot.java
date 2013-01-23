package turtlenuke;

import battlecode.common.*;

public class HQRobot extends BaseRobot {

  
  //@@ TurtleNuke strategy vars
  private static final int FORTIFY_RADIUS = 2; // May want something more sophisticated, e.g. layers
  private int stage = 0; // Strategy stage; have this?

  HQRobot(RobotController rc){
    super(rc);
  }

  private int turnsToPickaxe = Upgrade.PICKAXE.numRounds;
  public void run() throws GameActionException{
    if (rc.isActive()){
      if(stage == 0){
    	  rc.researchUpgrade(Upgrade.PICKAXE);
    	  if(--turnsToPickaxe == 0) stage = 1;
      } else if(stage == 1){
    	  Robot[] defenders = rc.senseNearbyGameObjects(Robot.class, 8, myTeam); //5x5; ## depends on FORTIFY_RADIUS
    	  if(defenders.length >= 16){ 
    		  rc.researchUpgrade(Upgrade.NUKE);
    	  } else {
    		  if(!spawn()){ //spawn failed
    			  rc.researchUpgrade(Upgrade.NUKE);
    		  }
    	  }
    	  // ## in actual strategy, will need to instruct new robot to defend
      }
    }
  }
  
  private boolean spawn() throws GameActionException{

      Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());

      // Need to do this first so the termination check works
      if(rc.canMove(dir)){
    	  rc.spawn(dir);
	      return true;
      }
      // Spawn as close to the desired direction as possible
	  Direction dirLeft = dir.rotateLeft();
	  Direction dirRight = dir.rotateRight();
	
	  while (dirRight != dirLeft) {    
	    if (rc.canMove(dirLeft)) {
	      rc.spawn(dirLeft);
	      return true;
	    }
	    dirLeft = dirLeft.rotateLeft();
	      
	    if (rc.canMove(dirRight)) {
	      rc.spawn(dirRight);
	      return true;
	    }
	    dirRight = dirRight.rotateRight();     
	  }
	  
	  return false;
  }

}
