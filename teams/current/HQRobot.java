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
  public boolean spawn() throws GameActionException{
      Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());

      // Need to do this first so the termination check works
      if(rc.canMove(dir) && !util.senseHostileMine(rc.getLocation().add(dir))){ // At least one map starts with a mine towards the enemy HQ
    	  spawnInDir(dir);
	      return true;
      }
      // Spawn as close to the desired direction as possible
	  Direction dirLeft = dir;
	  Direction dirRight = dir;	
	  do {
		dirLeft = dirLeft.rotateLeft();
	    if (rc.canMove(dirLeft)) {
	      spawnInDir(dirLeft);
	      return true;
	    }
	    
	    dirRight = dirRight.rotateRight(); 
	    if (rc.canMove(dirRight)) {
	      spawnInDir(dirRight);
	      return true;
	    }
	        
	  } while (dirRight != dirLeft) ;
	  
	  return false;
  }

  private void spawnInDir(Direction dir) throws GameActionException{
    rc.spawn(dir);
    spawnTimeLeft = curSpawnDelay;
  }

  
  
  //##
  @Override
  protected int processMessage(char[] data, int startIdx) {
	return 0; 
  }
  
  private static final int FORTIFY_MSG = 1;
  public char[] buildFortifyMessage(){
	  return new char[] {FORTIFY_MSG,0,0};
  }
  
  public void sendInitializeMessage(char[] data) throws GameActionException{
	  comm.putSticky(-2, data);
  }
}
