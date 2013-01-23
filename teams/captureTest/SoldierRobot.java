package captureTest;

import battlecode.common.*;

// FortifyBehavior notes:
// - We will probably eventually want to add some way of
//   moving through our own fortification barriers
// - Some way of communicating breach?
// - Minesweep neutrals first? Minesweep behavior?


public class SoldierRobot extends BaseRobot {

  private MapLocation captureGoal = this.curLoc;
  private boolean goalSet = false;

  SoldierRobot(RobotController rc){
    super(rc);
  }

  public void run() throws GameActionException{
    if(rc.isActive()){
      if(goalSet){
        capture();
      } else {
        goalSet = setCaptureGoal();
        if(goalSet){
          capture();
        }
      }
      //jam();
      rc.setIndicatorString(0, Boolean.toString(goalSet) + captureGoal.toString());
      rc.yield();
    }
  }

  // Main capture behaviour
  private void capture() throws GameActionException{
    if(rc.getLocation() == captureGoal){
      if(rc.senseEncampmentSquare(rc.getLocation()) && rc.senseCaptureCost() < 1.1 * rc.getTeamPower()){
        rc.captureEncampment(chooseType());
      }
    } else {
      this.nav.moveTo(captureGoal);
    }
  }

  // Sets the state goal and returns a boolean indicating success or failure
  private boolean setCaptureGoal() throws GameActionException{
    MapLocation[] nearbyEncampmentSquares = rc.senseAllEncampmentSquares();
    if(nearbyEncampmentSquares != null && nearbyEncampmentSquares.length > 0){
      MapLocation goal = nearbyEncampmentSquares[0];
      int dist = rc.getLocation().distanceSquaredTo(goal);

      // Find the closest encampment square
      for(MapLocation loc : nearbyEncampmentSquares){
        int newDist = rc.getLocation().distanceSquaredTo(loc);
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
    MapLocation[] alliedEncampments = rc.senseAlliedEncampmentSquares();

    int distToEnemyHQ = this.curLoc.distanceSquaredTo(rc.senseEnemyHQLocation());
    int distToTeamHQ = this.curLoc.distanceSquaredTo(rc.senseEnemyHQLocation());
    if(distToEnemyHQ <= 63) {
      return RobotType.ARTILLERY;
    } else if(Math.random() < 0.5) {
      return RobotType.GENERATOR;
    } else {
      return RobotType.SUPPLIER;
    }
  }
}
