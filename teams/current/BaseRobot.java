package current;

import battlecode.common.Clock;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class BaseRobot {
	
	// Subsystems
	public final RobotController rc;
	public final Communicator comm;
	
	// Robot data
	public final RobotType myType;
	public final int id = -1;
	
	public final int spawnRound;

	BaseRobot(RobotController rc){
		this.rc = myRC;
		this.comm = new Communicator(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();
	}
	

}