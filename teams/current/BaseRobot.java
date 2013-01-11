package current;

import battlecode.common.*;

public class BaseRobot
{
	// Subsystems
	public final RobotController rc;
	public final Communicator comm;
	
	// Robot data
	public final RobotType myType;
	public final int id;
	public final int spawnRound;
	
	// State variables
	public int curRound;

	BaseRobot(RobotController myRC){
		this.rc = myRC;
		this.comm = new Communicator(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();
	}
	
	public void run() throws GameActionException{
		
	}
}
