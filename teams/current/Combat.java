package current;

/**********************************
Combat.java - Combat interface
**********************************/

public class Combat{
  private BaseRobot rc;

  // Array of possible movement directions
  // 0 - EAST, 1 - NONE, 2 - NORTH, 3 - NORTH_EAST, 4 - NORTH_WEST, 5 - SOUTH
  // 6 - SOUTH_EAST, 7 - SOUTH-WEST, 8 - WEST
  private static Directions[] moveDirs = [Direction.NORTH, Direction,NORTH_EAST,
                                          Direction.EAST, Direction.SOUTH_EAST,
                                          Direction.SOUTH, Direction.SOUTH_WEST,
                                          Direction.WEST, Direction.NORTH_WEST]

  Combat(BaseRobot robot){
    this.rc = robot;
  }

  public void formSquare(){
  }

  public void squadMove(){
    // If head, then move according to strategy
    if(rc.squadID == rc.getRobot().getID())
      rc.fight();
    // Otherwise, squad member, so query squad channel for order
    else{
      int orders = rc.Communicator.receive(rc.squadID);
    }
  }
  // Rotation based on the position of the robot
  //      [2]
  //  [1][0][2]
  //     [1]
  // The position is adjusted accordingly
  public void squadRotate(){
    int pos = rc.squadPos;
    Directions[] rotateDirs = [Direction.NONE, Direction.NORTH_EAST, Direction.SOUTH_WEST];

    // Check if able to move, then move accordingly
    if(rc.canMove(rotateDirs[pos]))
      rc.move[pos]

    // Adjust position based on move
    if(pos == 1)
      rc.setSquadPos = 2;
    else if(pos == 2)
      rc.setSquadPos = 1;
  }

  private

  public void fight(){
    // Get and process the list of robots, allied and enemy
    Robot[] nearbyEnemyRobots = rc.senseNearbyGameObjects(Robot.class,16,rc.team().opponent());
    Robot[] nearbyAllyRobots = rc.senseNearbyGameObjects(Robot.class, 16, rc.team());
    int numEnemies = nearbyEnemyRobots.length;
    int numFriends = nearbyAllyRobots.length;

    MapLocation closestFriend = findClosestRobot(nearbyAllyRobots);
    MapLocation closestEnemy = findClosestRobot(nearbyEnemyRobots);
    // Figure out the best move
    moveDirs = bestMove();
  }

  // Given an array of robots, finds the closest robot to self
  private MapLocation findClosestRobot(Robot[] nearbyRobots){
    int closestDistance = 100000;
    MapLocation m;

    for(Robot r : nearbyRobots){
      RobotInfo ri = rc.senseRobotInfo(r);
      int dist = rc.getLocation().distanceSquaredTo(ri.location());

      if(dist < closestDistance){
        m = ri.location();
        closestDistance = dist;
      }
    }
    return m;
  }

}
