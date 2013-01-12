package current;

import battlecode.common.*;

public class HQRobot extends BaseRobot {

	public int spawnTimeLeft;
	public int curSpawnDelay; // change when upgrade suppliers
  private Upgrade[] upgrades = {Upgrade.FUSION, Upgrade.DEFUSION, Upgrade.PICKAXE, Upgrade.VISION, Upgrade.NUKE};
	private int upgradeNumber = 0;
  private int timeSinceLastUpgrade = 0;
  private double upgradeThreshold = 0.0005;

	HQRobot(RobotController rc){
		super(rc);
		spawnTimeLeft = 0;
		curSpawnDelay = GameConstants.HQ_SPAWN_DELAY;
	}

	public void run() throws GameActionException{
		if (rc.isActive()){
      // Randomly decide when to upgrade based on thresholds
      double whatDo = Math.random();
      if(whatDo < upgradeThreshold * timeSinceLastUpgrade){
        if(!rc.hasUpgrade(upgrades[upgradeNumber])){
          rc.researchUpgrade(upgrades[upgradeNumber]);
        }
        else{
          timeSinceLastUpgrade = 0;
          upgradeNumber++;
        }
      }
      // Otherwise, the HQ will default to spawn
      else{
        spawn();
      }
		}
    // If inactive, check out when it is next active
    else {
			spawnTimeLeft--;
			rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
		}
    timeSinceLastUpgrade++;
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
