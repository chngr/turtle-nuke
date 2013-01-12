package current;

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
  private Direction[][] moveDirs = {{Direction.NORTH_WEST, Direction.NORTH, Direction.NORTH_EAST},
                                    {Direction.WEST, Direction.NONE, Direction.EAST},
                                    {Direction.SOUTH_WEST, Direction.SOUTH, Direction.SOUTH_EAST}};

  // Local map for combat
  private int[][] localMap = { {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0},
                               {0, 0, 0, 0, 0}};

  // Costs for moving adjacent to enemy or ally
  private int allyAdjacentCost = 30;
  private int enemyAdjacentCost = 60;
  private int enemyTile = 1000;
  private int allyTile = 500;

  Combat(BaseRobot robot){
    this.r = robot;
  }

  // Set the desire to clump together in combat
  public void setClumping(int clumpingFactor){
    allyAdjacentCost = clumpingFactor;
  }

  // Set the desire to get near enemies
  public void setAggression(int aggressionFactor){
    enemyAdjacentCost = (int)((r.rc.getEnergon()/40.0) * aggressionFactor);
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
    computeCosts();

    // Find the adjacent square with the minimal cost
    int cost = 1000;
    Direction dir = Direction.NONE;
    for(int i = 1; i < 4; i++){
      for(int j = 1; j < 4; j++){
        int localCost = localMap[i][j];
        if(localCost < cost){
          cost = localCost;
          dir = moveDirs[i-1][j-1];
        }
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

      // Check if within map
      if(Math.abs(dx) < 3 && Math.abs(dy) < 3){
        // Set the value of an occupied scared as + if team, - if enemy
        localMap[2 + dx][2 + dy] = ri.team == r.rc.getTeam() ? allyTile : enemyTile;
      }
    }
  }

  // Based on sensed map, shall compute the cost for each move
  // The idea would be to loop through the map, and if we see that there is
  // an enemy/friend at a given location, then add a cost to adjacent squares
  // in map.
  //
  // * Note very naive and would be well to think of better method
  private void computeCosts(){
    // Go through the map, and simply adjust adjacent squares
    for(int i = 0; i < 5; i++){
      for(int j = 0; j < 5; j++){
        // See if there is something there
        if(localMap[i][j] < 0){
          // Check if enemy or friend
          int adjacentCost = localMap[i][j] > 750 ? enemyAdjacentCost : allyAdjacentCost;

          // Fill all adjacent squares
          for(int x = -1; x < 2; x++){
            if(i + x < 0 || i + x > 5)
              continue;
            for(int y = -1; y < 2; y++){
              if(j + y < 0 || j + y > 5)
                continue;
              localMap[i][j] += adjacentCost;
            }
          }
        }
      }
    }
  }

}
