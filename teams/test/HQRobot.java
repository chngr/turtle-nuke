package test;

import battlecode.common.*;

public class HQRobot extends BaseRobot {

	HQRobot(RobotController rc){
		super(rc);
	}
	
	public void run() throws GameActionException
	{
		if (rc.isActive())
		{
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir))
				rc.spawn(dir);
		}
	}
}