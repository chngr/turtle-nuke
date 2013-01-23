package current.behaviors;

import current.*;
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
  private MapLocation captureGoal = r.curLoc;
  private boolean goalSet = false;

  public CaptureBehavior(SoldierRobot r){
    super(r);
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

  private void initializeCaptureBehaviour(){
  }

  // Main capture behaviour
  private void capture() throws GameActionException{
    if(r.curLoc == captureGoal){
      if(r.rc.senseEncampmentSquare(r.curLoc) && r.rc.senseCaptureCost() < 1.1 * r.rc.getTeamPower()){
        r.rc.captureEncampment(chooseType());
      }
    } else {
      if(r.rc.senseObjectAtLocation(captureGoal).getTeam() != r.myTeam){
        r.nav.moveTo(captureGoal);
      } else {
        goalSet = false;
      }
    }
  }

  // Sets the state goal and returns a boolean indicating success or failure
  private boolean setCaptureGoal() throws GameActionException{
    MapLocation[] nearbyEncampmentSquares = r.rc.senseEncampmentSquares(r.curLoc, 100000, Team.NEUTRAL);
    if(nearbyEncampmentSquares.length != 0){
      MapLocation goal = nearbyEncampmentSquares[0];
      int dist = r.curLoc.distanceSquaredTo(goal);

      // Find the closest encampment square
      for(MapLocation loc : nearbyEncampmentSquares){
        int newDist = r.curLoc.distanceSquaredTo(loc);
        if(newDist < dist){
          dist = newDist;
          goal = loc;
        }
      }
      captureGoal = goal;
      return true;
    } else {
      return false;
    }
  }

  // Decides which type of encampment to build
  private RobotType chooseType() throws GameActionException{
    MapLocation[] alliedEncampments = r.rc.senseAlliedEncampmentSquares();

    int distToEnemyHQ = r.curLoc.distanceSquaredTo(r.rc.senseEnemyHQLocation());
    int distToTeamHQ = r.curLoc.distanceSquaredTo(r.rc.senseEnemyHQLocation());
    if(distToEnemyHQ <= 63) {
      return RobotType.ARTILLERY;
    } else if(Math.random() < 0.5) {
      return RobotType.GENERATOR;
    } else {
      return RobotType.SUPPLIER;
    }
  }
}
