package jammer;

import battlecode.common.*;

public class HQRobot extends BaseRobot {
	
	public int spawnTimeLeft;
	public int curSpawnDelay; // change when upgrade suppliers
	
	HQRobot(RobotController rc){
		super(rc);
		spawnTimeLeft = 0;
		curSpawnDelay = GameConstants.HQ_SPAWN_DELAY;
	}
	
	public void run() throws GameActionException
	{
		if (rc.isActive())
		{
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			Direction dirl = dir;
			Direction dirr = dir;
			if (rc.canMove(dir)) {
				rc.spawn(dir);
				spawnTimeLeft = curSpawnDelay;
				rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
				spawnTimeLeft--;
			} else {
				while (true) {
					dirl = dirl.rotateLeft();
					if (rc.canMove(dirl)) {
						rc.spawn(dirl);
						spawnTimeLeft = curSpawnDelay;
						rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
						spawnTimeLeft--;
						break;
					} else {
						dirr = dirr.rotateRight();
						if (dirr == dirl)
							break;
						if (rc.canMove(dirr)) {
							rc.spawn(dirr);
							spawnTimeLeft = curSpawnDelay;
							rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
							spawnTimeLeft--;
							break;
						}
					}
				}
			}
			if (spawnTimeLeft == 0) {
				// spawn unsuccessful
				Upgrade[] ups = {Upgrade.FUSION, Upgrade.PICKAXE, Upgrade.NUKE};
				for (Upgrade up : ups) {
					if (rc.checkResearchProgress(up) < up.numRounds) {
						rc.researchUpgrade(up);
						rc.setIndicatorString(0, "Researching");
						return;
					}
				}
			}
				
		} else {
			rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
			spawnTimeLeft--;
		}
	}
}