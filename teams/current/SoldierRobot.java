package current;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {
  SoldierRobot(RobotController rc){
    super(rc);
  }

  public void run() throws GameActionException{
    if (rc.isActive()) {
      double whatDo = Math.random();
      int numNearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 14, this.enemyTeam).length;

      // Check if there are enemies close by
      if(numNearbyEnemies > 0)
        this.combat.fight();
      // First, lay mine with small chance
      else if (whatDo < 0.05 && rc.senseMine(rc.getLocation()) == null)
        rc.layMine();
      // Otherwise, move
      else {
        Direction dir;
        // First, see if chance wills us to go home
        if(whatDo < 0.2){
          dir = rc.getLocation().directionTo(rc.senseHQLocation());
        }
        // Maybe head around randomly
        else if(whatDo < 0.6){
          dir = Direction.values()[(int)(Math.random() * 8)];
        }
        // Otherwise go toward the enemy
        else{
          dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
        }
        if(rc.canMove(dir)) {
          rc.move(dir);
        }
      }
    }

    if (Math.random()<0.01 && rc.getTeamPower()>5) {
      // Write the number 5 to a position on the message board corresponding to the robot's ID
      rc.broadcast(rc.getRobot().getID()%GameConstants.BROADCAST_MAX_CHANNELS, 5);
    }
  }

}
