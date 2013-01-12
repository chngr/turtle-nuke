package current;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {
  SoldierRobot(RobotController rc){
    super(rc);
  }

  public void run() throws GameActionException{
    if (rc.isActive()) {
      double whatDo = Math.random();
      int numNearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 24, this.enemyTeam).length;
      int numNearbyFriends = rc.senseNearbyGameObjects(Robot.class, 24, this.myTeam).length;
      int distHome = rc.getLocation().distanceSquaredTo(rc.senseHQLocation());
      Direction directionToEncampment;

      // Check if there are enemies close by
      if(numNearbyEnemies > 0 && numNearbyFriends > numNearbyEnemies)
        this.combat.fight();
      // If outnumbered, retreat
      else if(numNearbyEnemies > 0){
        Direction dir = rc.getLocation().directionTo(rc.senseHQLocation());
        if(rc.canMove(dir))
          rc.move(dir);
        else if(rc.canMove(dir.rotateLeft())){
          rc.move(dir.rotateLeft());
        }
        else if(rc.canMove(dir.rotateRight())){
          rc.move(dir.rotateRight());
        }
        else{
          this.combat.fight();
        }
      }
      // First, lay mine with small chance
      else if (whatDo < 0.01 && rc.senseMine(rc.getLocation()) == null)
        rc.layMine();
      // Otherwise, move
      else {
        Direction dir;
        MapLocation curLocation = rc.getLocation();
        // First, see if chance wills us to go home
        if(whatDo < 0.2){
          dir = curLocation.directionTo(rc.senseHQLocation());
        }
        // Maybe head around randomly
        else if(whatDo < 0.4){
          dir = Direction.values()[(int)(Math.random() * 8)];
        }
        // Otherwise go toward the enemy
        else{
          dir = curLocation.directionTo(rc.senseEnemyHQLocation());
        }
        if(rc.canMove(dir)) {
          MapLocation target = curLocation.add(dir);
        if (util.senseHostileMine(target)) {
          // Usually stop in caution and hope for better next turn
          if (whatDo > 0.2) {
            // Sometimes defuse
              if (whatDo < 0.9) {
              rc.defuseMine(target);
            }
            // Or charge blindly
            else {
            rc.move(dir);
            }
          }
        }
        // No hostile mine
          else {
            rc.move(dir);
          }
        }
      }
    }

    if (Math.random()<0.01 && rc.getTeamPower()>5) {
      // Write the number 5 to a position on the message board corresponding to the robot's ID
      rc.broadcast(rc.getRobot().getID()%GameConstants.BROADCAST_MAX_CHANNELS, 5);
    }
  }
}
