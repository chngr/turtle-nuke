package jammer;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {

	
	SoldierRobot(RobotController rc){
		super(rc);
	}

	public void run() throws GameActionException
	{
		int my_bots = rc.senseNearbyGameObjects(Robot.class, 10000, myTeam).length;
		int enemy_bots = rc.senseNearbyGameObjects(Robot.class, 14, enemyTeam).length;
		rc.setIndicatorString(0, "My stuff "+my_bots+ " Their Stuff: "+enemy_bots);
		
		if (rc.isActive()) {
			if (enemy_bots != 0) {
				combat.fight();
			}
			else {
				rc.layMine();
			}
		}
		
		rc.setIndicatorString(1, "Bytecodes| used:"+Clock.getBytecodeNum()+" left: "+Clock.getBytecodesLeft());
		int msgs_sent = 0;
		while (Clock.getBytecodesLeft() > 200 && 
			   rc.getTeamPower() > my_bots) {
			// Write the number 0 to a random position on the message board
			if (id % 3 != 0) {
				rc.broadcast((int)(Math.random()*GameConstants.BROADCAST_MAX_CHANNELS), 0);
			} else {
				rc.broadcast((int)(Math.random()*GameConstants.BROADCAST_MAX_CHANNELS), (int)(Math.random()*Integer.MAX_VALUE));
			}
			msgs_sent++;
		}
		rc.setIndicatorString(2, "Sent: "+msgs_sent);
	}
}
