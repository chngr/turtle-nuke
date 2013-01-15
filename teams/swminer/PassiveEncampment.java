package swminer;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class PassiveEncampment extends BaseRobot {
	
	PassiveEncampment(RobotController rc){
		super(rc);
	}

	public void run() throws GameActionException
	{
		rc.yield();
	}
}
