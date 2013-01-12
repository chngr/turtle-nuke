package test;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {

	private MapLocation goal;
	
	SoldierRobot(RobotController rc){
		super(rc);
		MapLocation HQ = rc.senseHQLocation();
		MapLocation eHQ = rc.senseEnemyHQLocation(); 
		goal = new MapLocation((HQ.x+3*eHQ.x)/4, (HQ.y+3*eHQ.y)/4);
	}

	public void run() throws GameActionException
	{
		if(!rc.getLocation().equals(goal))
			nav.moveTo(goal);
	}
}
