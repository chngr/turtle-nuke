package team097.behaviors;

import team097.*;
import battlecode.common.*;


/*****************************************************************************
 * Capture Behaviour --- Basic Encampment Capturing.
 *
 * The core of the behaviour is dedicated to locating and capturing nearby
 * encampment locations. There are two basic modes for which this can happen:
 *
 *  a. The robot may be instructed by the HQ to capture a specific location
 *  b. The robot may be autonomous, and look for the nearest location to capture
 *
 *  In either case, the behaviour is outlined as follows:
 *
 *  1. Check whether or not goal has been reached
 *    1a. If the goal is reached:
 *    2a. Check that the encampment is not captured already;
 *    3a. Check that we have enough power to capture the goal;
 *    4a. Capture the goal.
 *
 *    1b. If the goal is not reached:
 *    2b. Check that the goal is not reached by any other robot;
 *    3b. Run the Navigation module toward the goal;
 *    4b. Proceed toward the goal.
 *
 *  The behaviour then returns to the soldierRobot base to run post behaviour code.
 ****************************************************************************/
public class CaptureBehavior extends Behavior {
  private MapLocation captureGoal;
  private boolean goalSet = false;
  
  // Encampment type to build; chooses type independently iff this is null
  public RobotType encampmentType;
  

  public CaptureBehavior(SoldierRobot r){
    super(r);
  }
  
  
  public void checkBehaviorChange() {
  	if(r.util.senseDanger()) r.setBehavior(r.combatBehavior);
  }
  

  public void run() throws GameActionException{
    if(goalSet){
      capture();
    } else {
      goalSet = setCaptureGoal();
      if(goalSet){
        capture();
      } else {
        r.rc.yield();
      }
    }
  }


  // Main capture behaviour
  private void capture() throws GameActionException{
    if(r.curLoc.equals(captureGoal)){
      if(r.rc.senseEncampmentSquare(r.curLoc) && r.rc.senseCaptureCost() < 0.95 * r.rc.getTeamPower()){
        if(encampmentType != null) r.rc.captureEncampment(encampmentType);
        else r.rc.captureEncampment(chooseType());
      }
    } else {
      if(r.rc.canSenseSquare(captureGoal)){
    	  GameObject o = r.rc.senseObjectAtLocation(captureGoal);
    	  if(o != null && o.getTeam() == r.myTeam) goalSet = false;
    	  else r.nav.tunnelTo(captureGoal); //## moveTo once fixed
      } else {
    	  r.nav.tunnelTo(captureGoal);
      }
    }
  }

  // Sets the state goal and returns a boolean indicating success or failure
  private boolean setCaptureGoal() throws GameActionException{
    MapLocation[] nearbyEncampmentSquares = r.rc.senseEncampmentSquares(r.curLoc, 100000, Team.NEUTRAL);
    if(nearbyEncampmentSquares.length != 0){
      MapLocation goal = null;
      int dist = 10000;

      // Find the closest unoccupied encampment square that doesn't box in the HQ
      for(MapLocation loc : nearbyEncampmentSquares){
        int newDist = r.util.getDistanceBetween(r.curLoc, loc);
        if(newDist < dist && !boxIn(loc)){
          if(r.rc.canSenseSquare(loc)){
	          GameObject o = r.rc.senseObjectAtLocation(loc);
	      	  if(o != null && o.getTeam() == r.myTeam) continue;
          }

          dist = newDist;
          goal = loc;
        }
      }
      if(goal != null){
	      captureGoal = goal;
	      return true;
      }
    }
    return false; 
  }
  // Special case: don't box in our HQ with encampments (they do this deliberately with the maps)
  private boolean boxIn(MapLocation loc) throws GameActionException{
	  if(r.util.getDistanceBetween(loc, r.HQ) > 2) return false;
	  int freeCount = 0;
	  for(Direction d : r.util.movableDirs){
		  if(r.util.squareFree(r.HQ.add(d))) freeCount++;
		  if(freeCount > 1) return false;
	  }
	  return true;
  }


  // Decides which type of encampment to build
  // Should generally be decided by HQ
  private RobotType chooseType() throws GameActionException{
   // MapLocation[] alliedEncampments = r.rc.senseAlliedEncampmentSquares();

    int distToEnemyHQ = r.curLoc.distanceSquaredTo(r.eHQ);
    int distToTeamHQ = r.curLoc.distanceSquaredTo(r.HQ);
    if(distToEnemyHQ <= 64 || distToTeamHQ <= 64) {
      return RobotType.ARTILLERY;
    } else if(Math.random() < 0.4) {
      return RobotType.GENERATOR;
    } else {
      return RobotType.SUPPLIER;
    }
  }

}
