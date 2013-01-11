package current;

import battlecode.common.*;

public class BaseRobot
{
	// Subsystems
	public final RobotController rc;
	public final Communicator comm;
	public final Navigator nav;
	
	// State data
	public final int maph, mapw;
	
	// Robot data
	public final int id;
	public final int spawnRound;
	public final RobotType myType;
	
	// State variables
	public int curRound;
	public MapLocation curLoc;

	BaseRobot(RobotController myRC){
		this.rc = myRC;
		this.comm = new Communicator(this);
		this.nav = new Navigator(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();
		
		this.maph = rc.getMapHeight();
		this.mapw = rc.getMapWidth();
	}
	
	public void run() throws GameActionException{
		this.curLoc = rc.getLocation();
	}
}
