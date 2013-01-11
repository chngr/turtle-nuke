package current;

import battlecode.common.*;

public class BaseRobot
{
	// Subsystems
	public final RobotController rc;
	public final Communicator comm;
	public final Navigator nav;
	
	// Robot data
	public final int id;
	public final int spawnRound;
	public final RobotType myType;
	
	// State variables
	public int curRound;

	BaseRobot(RobotController myRC){
		this.rc = myRC;
		this.comm = new Communicator(this);
		this.nav = new Navigator(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();
	}
	
	public void run() throws GameActionException{
		
	}
}
