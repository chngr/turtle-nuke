package current;

import current.strategies.*;
import battlecode.common.*;

public class HQRobot extends BaseRobot {

  public int spawnTimeLeft;
  public int curSpawnDelay = 10; // change when upgrade suppliers
  
  
  private Strategy currentStrategy;
  
  // Strategies
  private TurtleNuke turtleNuke;
  

  HQRobot(RobotController rc){
    super(rc);
    turtleNuke = new TurtleNuke(this);
    currentStrategy = pickStrategy();
  }

  public void run() throws GameActionException {
	  //## periodically check if we should change our strategy
	  currentStrategy.run();
  }
  
  private Strategy pickStrategy(){
	  //##
	  return turtleNuke;
  }
  
  // Generalize to allow choosing preferred direction
  private boolean spawn() throws GameActionException{

      Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());

      // Need to do this first so the termination check works
      if(rc.canMove(dir)){
    	  rc.spawn(dir);
	      return true;
      }
      // Spawn as close to the desired direction as possible
	  Direction dirLeft = dir;
	  Direction dirRight = dir;
	
	  do {
		dirLeft = dirLeft.rotateLeft();
	    if (rc.canMove(dirLeft)) {
	      rc.spawn(dirLeft);
	      return true;
	    }
	    
	    dirRight = dirRight.rotateRight(); 
	    if (rc.canMove(dirRight)) {
	      rc.spawn(dirRight);
	      return true;
	    }
	        
	  } while (dirRight != dirLeft) ;
	  
	  return false;
  }

  private void spawnInDir(Direction dir) throws GameActionException{
    rc.spawn(dir);
    spawnTimeLeft = curSpawnDelay;
    rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
  }
}
