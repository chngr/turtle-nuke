package swcombat;

import battlecode.common.*;

public class HQRobot extends BaseRobot {

  public int spawnTimeLeft;
  public int curSpawnDelay; // change when upgrade suppliers
  private Upgrade[] upgrades = {Upgrade.FUSION, Upgrade.DEFUSION, Upgrade.PICKAXE, Upgrade.VISION, Upgrade.NUKE};
  private int upgradeNumber = 0;
  private double spawnMinPower = GameConstants.HQ_POWER_PRODUCTION + 10; // should adjust for generators

  HQRobot(RobotController rc){
    super(rc);
    spawnTimeLeft = 0;
    curSpawnDelay = GameConstants.HQ_SPAWN_DELAY;
  }

  public void run() throws GameActionException{
    if (rc.isActive()){
      // If we're running out of power, we don't need more robots
      if(rc.getTeamPower() > spawnMinPower){
          spawn();
      } else {
        if(rc.hasUpgrade(upgrades[upgradeNumber])){
        	upgradeNumber++;
        }
        rc.researchUpgrade(upgrades[upgradeNumber]);
      }

    }
    // If inactive, check out when it is next active
    else {
      spawnTimeLeft--;
      rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
    }
  }
  private void spawn() throws GameActionException{
    // Spawn a soldier
    Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());

    // Spawns soldier if possible to move
    if (rc.canMove(dir)) {
      spawnInDir(dir);
    }
    // Try to move around to spawn in different direction
    else {
      Direction dirLeft = dir;
      Direction dirRight = dir;

      while (dirRight != dirLeft) {
        dirLeft = dirLeft.rotateLeft();
        if (rc.canMove(dirLeft)) {
          spawnInDir(dirLeft);
          break;
        }
        else {
          dirRight = dirRight.rotateRight();
          if (rc.canMove(dirRight)) {
            spawnInDir(dirRight);
            break;
          }
        }
      }
    }
  }

  private void spawnInDir(Direction dir) throws GameActionException{
    rc.spawn(dir);
    spawnTimeLeft = curSpawnDelay;
    rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
  }
}
