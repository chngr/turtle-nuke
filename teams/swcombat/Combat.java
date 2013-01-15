package swcombat;

/**********************************
Combat.java - Combat interface

This module exports basic functionality for combat and fighting. The
main method that this does export is

    Combat.fight();

which controls immediate combat maneuvering. All calculations and
movement will be controlled purely by the Combat class through that
function.

Some methods to help control the behaviour of fighting shall e

    Combat.setClumping(int clumpingFactor);
    Combat.setAgression(int aggressionFactor);

which allow one to set the tendency for robots to clump compared to
tendency to engage enemy robots.
**********************************/

import battlecode.common.*;

public class Combat{
  private BaseRobot r;

  // Hard-coded values for possible move directions
  private Direction[] moveDirs = {Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST,
                                  Direction.WEST,                 Direction.EAST,
                                  Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST};

  // Local map for combat
  private double[][] localMap = new double[7][7];

  // Costs for moving adjacent to enemy or ally
  private double enemyTile = -4.0;
  private double allyTile = -1.0;

  // Additional constants that affect how likely a robot will choose to go near
  // allies or enemies
  private double clumpingFactor = 0.2;
  private double hostilityFactor = 1.0;

  Combat(BaseRobot robot){
    this.r = robot;
  }

  // Methods to set some of the constants
  public void setClumpingFactor(int factor){
    this.clumpingFactor = factor;
  }

  public void setHostilityFactor(int factor){
    this.hostilityFactor = factor;
  }

  // Basic combat method:
  //
  // SENSE & UPDATE MAP<-
  //       |             |
  // CALCULATE COSTS     |
  //       |             |
  // FIND MINIMAL MOVE---|
  //
  public void fight() throws GameActionException{
    // Senses and populates local map
    updateMap();

    // Calculate the cost for moving to an adjacent square
    double[] directionCosts = computeCosts();

    // Find the adjacent square with the minimal cost
    double cost = 100000.0;
    Direction dir = r.rc.getLocation().directionTo(r.rc.senseEnemyHQLocation());
    int directionOffset = 0;

    for(int i = 0; i < 8; i++){
      if(directionCosts[i] <= cost){
        dir = moveDirs[i];
        cost = directionCosts[i];
        directionOffset = i;
      }
    }

    String str = "";
    for(int i = 0; i < 8; i++){
      str += Double.toString(directionCosts[i]) + " ";
    }
    String dirStr = dir.toString();
    r.rc.setIndicatorString(0,str +  dirStr);

    // If the best thing to do is not move, yield for now
    if(r.rc.canMove(dir) && r.rc.senseMine(r.rc.getLocation().add(dir)) == null)
      r.rc.move(dir);
    else{
      int[] offsets = {1, -1, 2, -2};
      for(int i = 0; i < 4; i++){
        int idx = (directionOffset + offsets[i] % 8) + 8;
        dir = moveDirs[idx % 8];
        if(r.rc.canMove(dir) && r.rc.senseMine(r.rc.getLocation().add(dir)) == null){
          r.rc.move(dir);
          r.rc.setIndicatorString(0, str + dir.toString());
          break;
        }
      }
    }
  }

  // Senses for nearby robots and updates map accordingly
  private void updateMap() throws GameActionException{
    Robot[] nearbyRobots = r.rc.senseNearbyGameObjects(Robot.class, 14);
    // Process nearby robots
    for(Robot robot: nearbyRobots){
      RobotInfo ri = r.rc.senseRobotInfo(robot);
      int dx = r.rc.getLocation().x - ri.location.x;
      int dy = r.rc.getLocation().y - ri.location.y;
      double energonScale = r.rc.getEnergon() / ri.energon;

      // Set the value of an occupied scared as + if team, - if enemy
      // The cost of the square is set proportional to the energon of the robot
      if(ri.team == r.rc.getTeam())
        localMap[3 - dx][3 - dy] = allyTile * energonScale * clumpingFactor;
      else
        localMap[3 - dx][3 - dy] = enemyTile * energonScale * energonScale * hostilityFactor;
    }
  }

  // Based on sensed map, shall compute the cost for each move
  // The idea would be to loop through the map, and if we see that there is
  // an enemy/friend at a given location, then add a cost to adjacent squares
  // in map.
  //
  // * Note very naive and would be well to think of better method
  private double[] computeCosts(){
    // Compute the cost by taking the weighted sum of the surrounding squares
    // Shall compute costs going along the edge inwards

    // Using the order
    // NORTH_WEST, NORTH, NORTH_EAST,
    // WEST,       NONE , EAST,
    // SOUTH_WEST, SOUTH, SOUTH_WEST
    // we compute the cost for moving a specific direction by summing the costs
    // adjacent squares
    double[] directionCosts = new double[8];
    double twoFactor = 0.5;
    double adjFactor = 0.9;
    double locFactor = 0.2;

    directionCosts[0] = crossCost(2, 2, twoFactor, adjFactor, locFactor);
    directionCosts[1] = crossCost(3, 2, twoFactor, adjFactor, locFactor);
    directionCosts[2] = crossCost(4, 2, twoFactor, adjFactor, locFactor);
    directionCosts[3] = crossCost(2, 3, twoFactor, adjFactor, locFactor);
    directionCosts[4] = crossCost(4, 3, twoFactor, adjFactor, locFactor);
    directionCosts[5] = crossCost(2, 4, twoFactor, adjFactor, locFactor);
    directionCosts[6] = crossCost(3, 4, twoFactor, adjFactor, locFactor);
    directionCosts[7] = crossCost(4, 4, twoFactor, adjFactor, locFactor);

    return directionCosts;
  }

  // Computes the cost around a given square in a diamond shape
  //          #
  //         ###
  //        #####
  //         ###
  //          #
  // where the squares two away from the centre are weighted by twoFactor,
  // those that are one away from the centre by adjFactor, and the middle
  // square by locFactor
  private double crossCost(int x, int y, double tf, double af, double lf){
    return tf*localMap[x][y-2] +
           af*localMap[x-1][y-1] + af*localMap[x][y-1] + af*localMap[x+1][y+1] +
           tf*localMap[x-2][y] + af*localMap[x-1][y] + lf*localMap[x][y] + af*localMap[x+1][y] + tf*localMap[x+2][y] +
           af*localMap[x-1][y+1] + af*localMap[x][y+1] + af*localMap[x+1][y+1] +
           tf*localMap[x][y+2];
  }

}
