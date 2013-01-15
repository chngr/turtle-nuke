package swarmwave;

import battlecode.common.*;

public class Artillery extends BaseRobot {

	Artillery(RobotController rc){
		super(rc);
	}
	
	public void run() throws GameActionException
	{
		rc.suicide();
	}
}
