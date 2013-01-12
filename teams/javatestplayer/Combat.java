package testjavaplayer;

/**********************************
Combat.java - Combat interface

The basic logic exports only the function

    Combat.fight();

that controls everything based on a simple
local cost calculation.
**********************************/

import battlecode.common.*;

public class Combat{
  private RobotController rc;

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
  private int allyAdjacentCost = -15;
  private int enemyAdjacentCost = 60;
  private int enemyTile = 1000;
  private int allyTile = 500;

  Combat(RobotController robot){
    this.rc = robot;
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
    int cost = 0;
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
    if(dir == Direction.NONE)
      rc.setIndicatorString(0, dir.toString());
    else if(rc.canMove(dir))
      rc.move(dir);
  }

  // Senses for nearby robots and updates map accordingly
  private void updateMap() throws GameActionException{
    Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 14);
    // Process nearby robots
    for(Robot r: nearbyRobots){
      RobotInfo ri = rc.senseRobotInfo(r);
      int dx = rc.getLocation().x - ri.location.x;
      int dy = rc.getLocation().y - ri.location.y;

      // Check if within map
      if(Math.abs(dx) < 3 && Math.abs(dy) < 3){
        // Set the value of an occupied scared as + if team, - if enemy
        localMap[2 + dx][2 + dy] = ri.team == rc.getTeam() ? allyTile : enemyTile;
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