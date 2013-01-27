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

  Combat(BaseRobot robot){
    this.r = robot;
  }

  /***************************************************************************
   * Constants and Parameters for Combat.java
   **************************************************************************/
  // Hard-coded values for possible move directions
  private Direction[] moveDirs = {Direction.NORTH_WEST,
                                  Direction.NORTH,
                                  Direction.NORTH_EAST,
                                  Direction.WEST,
                                  Direction.EAST,
                                  Direction.SOUTH_WEST,
                                  Direction.SOUTH,
                                  Direction.SOUTH_EAST};

  // Local map for combat
  private double[][] localMap = new double[7][7];

  // Costs for moving adjacent to enemy or ally
  private double enemyTile = -4.0;
  private double allyTile = -1.0;

  // Scaling constants for computing the costs of a square, based on nearby ones
  private double oneAway = 0.9;
  private double twoAway = 0.6;
  private double zerAway = 0.2;

  public void setOneAway(int factor){
    this.oneAway = factor;
  }
  public void setTwoAway(int factor){
    this.twoAway = factor;
  }
  public void setZerAway(int factor){
    this.zerAway = factor;
  }

  // Constants that affect how likely a robot will choose to go near allies/enemies
  private double clumpingFactor = 0.2;
  private double hostilityFactor = 1.0;

  public void setClumpingFactor(int factor){
    this.clumpingFactor = factor;
  }
  public void setHostilityFactor(int factor){
    this.hostilityFactor = factor;
  }

  /***************************************************************************
   * Primary public method for the package; interface for the combat algorithm.
   *
   * The basic procedure for the fight() routine is as follows:
   *
   *  1. Sense environment for nearby game objects
   *  2. Update localMap variable with calculated costs
   *  3. Choose the lowest cost direction, and
   *  4. Move in chosen direction
   *
   *  The specific behaviour of the robot can be changed primarily from the
   *  constants specified above, though tweaks to the cost calculations are
   *  possible as well.
   ***************************************************************************/
  public void fight() throws GameActionException{
    // 1. Senses and populates local map
    updateMap();

    // 2. Calculate the cost for moving to an adjacent square
    // ## also compute costs for current square
    double[] directionCosts = computeCosts();

    // 3. Find the adjacent square with the minimal cost
    double cost = 100000.0;
    int directionOffset = 0;
    Direction dir = r.curLoc.directionTo(r.rc.senseEnemyHQLocation());

    for(int i = 0; i < 8; i++){
      if(directionCosts[i] <= cost){
        dir = moveDirs[i];
        cost = directionCosts[i];
        directionOffset = i;
      }
    }

    // 4. Move in chosen direction, provided possible and no mines
    // ## need to consider this while choosing direction, close the optimal direction might be bad @1
    if(r.rc.canMove(dir) && !r.util.senseHostileMine(r.curLoc.add(dir)))
      r.rc.move(dir);
    else{
      int[] offsets = {1, -1, 2, -2};
      for(int i = 0; i < 4; i++){
        int idx = (directionOffset + offsets[i] % 8) + 8;
        dir = moveDirs[idx % 8];
        if(r.rc.canMove(dir) && !r.util.senseHostileMine(r.curLoc.add(dir))){
          r.rc.move(dir);
          break;
        }
      }
    }
  }

  /**************************************************************************
   * Core Functions of Combat.java
   **************************************************************************/
  //## !! reset local map each time
  private void updateMap() throws GameActionException{
    Robot[] nearbyRobots = r.rc.senseNearbyGameObjects(Robot.class, 14);
    double ownEnergon = r.rc.getEnergon();
    for(Robot robot: nearbyRobots){
      RobotInfo ri = r.rc.senseRobotInfo(robot);
      int dx = r.curLoc.x - ri.location.x;
      int dy = r.curLoc.y - ri.location.y;
      double energonScale = ownEnergon / ri.energon;

      // The cost of the square is set proportional to the energon of the robot
      if(ri.team == r.myTeam)
        localMap[3 - dx][3 - dy] = allyTile * energonScale * clumpingFactor;
      else
        localMap[3 - dx][3 - dy] = enemyTile * energonScale * energonScale * hostilityFactor;
    }
  }

  // Computes cost based on a square around the tile
  private double[] computeCosts(){
    double[] directionCosts = new double[8];

    directionCosts[0] = crossCost(2, 2);
    directionCosts[1] = crossCost(3, 2);
    directionCosts[2] = crossCost(4, 2);
    directionCosts[3] = crossCost(2, 3);
    directionCosts[4] = crossCost(4, 3);
    directionCosts[5] = crossCost(2, 4);
    directionCosts[6] = crossCost(3, 4);
    directionCosts[7] = crossCost(4, 4);

    return directionCosts;
  }

  /***************************************************************************
  * Computes the cost around a given square in a diamond shape
  *          fff
  *         faaaf
  *         fazaf
  *         faaaf
  *          fff
  *  which is then scaled by the constants twoAway, oneAway, zeroAway.
  ***************************************************************************/
  private double crossCost(int x, int y){
    // Adds all the costs in squares 'f' around the centre
    double farSquares = localMap[x-1][y-2] + localMap[x][y-2] + localMap[x+1][y-2] +
                        localMap[x-2][y-1] + localMap[x-1][y] + localMap[x-1][y+1] +
                        localMap[x+2][y-1] + localMap[x+2][y] + localMap[x+2][y+1] +
                        localMap[x-1][y+2] + localMap[x][y+2] + localMap[x+1][y+2];
    // Adds all the costs in squares 'a' around the centre
    double adjSquares = localMap[x-1][y-1] + localMap[x][y-1] + localMap[x+1][y+1] +
                        localMap[x-1][y] + localMap[x+1][y] + localMap[x-1][y+1] +
                        localMap[x][y+1] + localMap[x+1][y+1];
    // The cost in the current square
    double curSquare = localMap[x][y]; //## @1 if not 0, can't move, immediately return high cost

    return (twoAway * farSquares) + (oneAway * adjSquares) + (zerAway * curSquare);
  }

}
