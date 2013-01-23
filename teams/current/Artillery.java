package current;

import battlecode.common.*;

public class Artillery extends BaseRobot {

	Artillery(RobotController rc){
		super(rc);
	}
	
	public void run() throws GameActionException
	{
		rc.suicide();
	}

	@Override
	protected int processMessage(char[] data, int startIdx) { return 0; }
}
