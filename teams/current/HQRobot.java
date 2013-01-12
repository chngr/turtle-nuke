package current;

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
			} else {
				while (true) {
					dirl = dirl.rotateLeft();
					if (rc.canMove(dirl)) {
						rc.spawn(dirl);
						spawnTimeLeft = curSpawnDelay;
						rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
						break;
					} else {
						dirr = dirr.rotateRight();
						if (dirr == dirl)
							break;
						if (rc.canMove(dirr)) {
							rc.spawn(dirr);
							spawnTimeLeft = curSpawnDelay;
							rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
							break;
						}
					}
				}
			}
				
		} else {
			spawnTimeLeft--;
			rc.setIndicatorString(0, "Spawning in " + spawnTimeLeft);
		}
		if (curRound % 24 == 0)
		{
			char[] testmsg = {'a'};
			comm.send(comm.IDtoNextFreq(-1), testmsg, 1);
		}
	}
}