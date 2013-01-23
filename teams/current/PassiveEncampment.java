package current;

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

	@Override
	protected void processMessage(char[] data, int startIdx) { }
}
