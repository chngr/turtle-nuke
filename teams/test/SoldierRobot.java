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
    Robot[] nearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 32, rc.getTeam().opponent());
    if(nearbyEnemies.length > 0)
      this.combat.fight();
    else if(!rc.getLocation().equals(goal))
			nav.moveTo(goal);
	}
}
