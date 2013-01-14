package test;

import battlecode.common.*;

public class BaseRobot
{
	// Subsystems
	public final RobotController rc;
	public final Navigator nav;
	public final Utilities util;
	
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
		// Initialize subsystems
		this.rc = myRC;
		this.nav = new Navigator(this);
		this.util = new Utilities(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();
		
		// Initialize state variables
		this.maph = rc.getMapHeight();
		this.mapw = rc.getMapWidth();
	}
	
	public void run() throws GameActionException{
		this.curLoc = rc.getLocation();
	}

}
