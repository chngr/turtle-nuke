package blank;

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
                                  Direction.WEST, Direction.NONE, Direction.EAST,
                                  Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST};

  // Local map for combat
  private int[][] localMap = { {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0}};

  // Costs for moving adjacent to enemy or ally
  private int enemyTile = 100;
  private int allyTile = 50;
  private int mineCost = 10000;

  // Additional constants that affect how likely a robot will choose to go near
  // allies or enemies
  private int clumpingFactor = 3;
  private int hostilityFactor = 5;

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
    int[] directionCosts = computeCosts();

    // Find the adjacent square with the minimal cost
    int cost = 9999;
    Direction dir = Direction.NONE;

    for(int i = 0; i < 9; i++){
      if(directionCosts[i] < cost){
        dir = moveDirs[i];
        cost = directionCosts[i];
      }
    }

    // If the best thing to do is not move, yield for now
    if(dir == Direction.NONE)
      return;
    else if(r.rc.canMove(dir))
      r.rc.move(dir);
  }

  // Senses for nearby robots and updates map accordingly
  private void updateMap() throws GameActionException{
    Robot[] nearbyRobots = r.rc.senseNearbyGameObjects(Robot.class, 14);
    // Process nearby robots
    for(Robot robot: nearbyRobots){
      RobotInfo ri = r.rc.senseRobotInfo(robot);
      int dx = r.rc.getLocation().x - ri.location.x;
      int dy = r.rc.getLocation().y - ri.location.y;
      int energon = (int)ri.energon;

      // Check if within map
      if(-3 < dx && dx < 3 && -3 < dy && dy < 3){
        // Set the value of an occupied scared as + if team, - if enemy
        // The cost of the square is set proportional to the energon of the robot
        localMap[2 + dx][2 + dy] = ri.team == r.rc.getTeam() ?
                                              allyTile * energon * clumpingFactor:
                                              enemyTile * energon * hostilityFactor;
      }
    }

    // Find nearby mines and update map correspondingly
  }

  // Based on sensed map, shall compute the cost for each move
  // The idea would be to loop through the map, and if we see that there is
  // an enemy/friend at a given location, then add a cost to adjacent squares
  // in map.
  //
  // * Note very naive and would be well to think of better method
  private int[] computeCosts(){
    // Compute the cost by taking the weighted sum of the surrounding squares
    // Shall compute costs going along the edge inwards

    // Using the order
    // NORTH_WEST, NORTH, NORTH_EAST,
    // WEST,       NONE , EAST,
    // SOUTH_WEST, SOUTH, SOUTH_WEST
    // we compute the cost for moving a specific direction by summing the costs
    // adjacent squares
    int[] directionCosts = new int[9];
    int rx = r.rc.getLocation().x;
    int ry = r.rc.getLocation().y;
    Team team = r.rc.getTeam();

    if(r.rc.senseMine(new MapLocation(rx - 1, ry - 1)) != team)
      directionCosts[0] = localMap[0][0] + localMap[0][1] + localMap[0][2] +
                          localMap[1][0] + localMap[1][1] + localMap[1][1] +
                          localMap[2][0] + localMap[2][1] + localMap[2][2];
    else
      directionCosts[0] = mineCost;

    if(r.rc.senseMine(new MapLocation(rx, ry - 1)) != team)
      directionCosts[1] = localMap[0][1] + localMap[0][2] + localMap[0][3] +
                          localMap[1][1] + localMap[1][2] + localMap[1][3] +
                          localMap[2][1] + localMap[2][2] + localMap[2][3];
    else
      directionCosts[1] = mineCost;

    if(r.rc.senseMine(new MapLocation(rx + 1, ry - 1)) != team)
      directionCosts[2] = localMap[0][2] + localMap[0][3] + localMap[0][4] +
                          localMap[1][2] + localMap[1][3] + localMap[1][4] +
                          localMap[2][2] + localMap[2][3] + localMap[2][4];
    else
      directionCosts[2] = mineCost;

    if(r.rc.senseMine(new MapLocation(rx - 1, ry)) != team)
      directionCosts[3] = localMap[1][0] + localMap[1][1] + localMap[1][2] +
                          localMap[2][0] + localMap[2][1] + localMap[2][2] +
                          localMap[3][0] + localMap[3][1] + localMap[3][2];
    else
      directionCosts[3] = mineCost;

    directionCosts[4] = localMap[1][1] + localMap[1][2] + localMap[1][3] +
                        localMap[2][1] + localMap[2][2] + localMap[2][3] +
                        localMap[3][1] + localMap[3][2] + localMap[3][3];

    if(r.rc.senseMine(new MapLocation(rx + 1, ry)) != team)
      directionCosts[5] = localMap[1][1] + localMap[1][2] + localMap[1][4] +
                          localMap[2][1] + localMap[2][3] + localMap[2][4] +
                          localMap[3][1] + localMap[3][2] + localMap[3][4];
    else
      directionCosts[5] = mineCost;

    if(r.rc.senseMine(new MapLocation(rx - 1, ry + 1)) != team)
      directionCosts[6] = localMap[2][0] + localMap[2][1] + localMap[2][2] +
                          localMap[3][0] + localMap[3][1] + localMap[3][2] +
                          localMap[4][0] + localMap[4][1] + localMap[4][2];
    else
      directionCosts[6] = mineCost;

    if(r.rc.senseMine(new MapLocation(rx, ry + 1)) != team)
      directionCosts[7] = localMap[2][1] + localMap[2][2] + localMap[2][3] +
                          localMap[3][1] + localMap[3][2] + localMap[3][3] +
                          localMap[4][1] + localMap[4][2] + localMap[4][3];
    else
      directionCosts[7] = mineCost;

    if(r.rc.senseMine(new MapLocation(rx + 1, ry + 1)) != team)
      directionCosts[8] = localMap[2][2] + localMap[2][2] + localMap[2][4] +
                          localMap[3][2] + localMap[3][3] + localMap[3][4] +
                          localMap[4][2] + localMap[4][3] + localMap[4][4];
    else
      directionCosts[8] = mineCost;

    return directionCosts;
  }

}
