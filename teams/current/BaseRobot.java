package current;

import battlecode.common.*;

public abstract class BaseRobot
{
	// Subsystems
	public final RobotController rc;
	public final Utilities util;
	public final Communicator comm;
	public final Navigator nav; // We should put all the move functions in Navigator, i.e it should include SimpleNavigator, tunneling...
								// They can then be called when the situation warrants, as nav.tunnelTo(loc)
	public final Combat combat;
	
	// State data
	public final int maph, mapw;
	
	// Robot data
	public final int id;
	public final int spawnRound;
	public final RobotType myType;
	
	// Team constants
	public final Team myTeam;
	public final Team enemyTeam;
	
	// Headquarters locations
	public final MapLocation HQ;
	public final MapLocation eHQ;
	
	// State variables
	public int curRound;
	public MapLocation curLoc;
	
	// Subscribed stickyspaces
	// If we ever need to remove subscriptions, we'll want a better data structure
	public int[] subs;
	public int numSubs = 0;

	BaseRobot(RobotController myRC){
		// Initialize subsystems
		this.rc = myRC;
		this.util = new Utilities(this);
		this.comm = new Communicator(this);
		this.nav = new Navigator(this);
		this.combat = new Combat(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = this.curRound = Clock.getRoundNum();
		this.myType = rc.getType();
		this.curLoc = rc.getLocation();
		
		// Initialize team objects
		this.myTeam = rc.getTeam();
		this.enemyTeam = myTeam.opponent();
		
		// Initialize headquarter locations
		this.HQ = rc.senseHQLocation();
		this.eHQ = rc.senseEnemyHQLocation();
		
		// Initialize state variables
		this.maph = rc.getMapHeight();
		this.mapw = rc.getMapWidth();
		
		// Initialize subscribed sticky spaces 
		subs = new int[64];
		subscribe(1); // Subscribed to general by default
		
	}
	
	public void run() throws GameActionException{
		this.curLoc = rc.getLocation();
		this.curRound = Clock.getRoundNum();
	}
	
	

	// --- Message parsing ---
	
	protected void readAllMessages() throws GameActionException{
		for(int i=0; i<numSubs; i++)
			readMessages(comm.getSticky(subs[i]));
		readMessages(comm.receive());
	}
	
	protected void readMessages(char[] messageData) throws GameActionException{
		int msgIdx = 0;
		while(msgIdx < messageData.length-1){
			msgIdx += processMessage(messageData, msgIdx);
		}
	}
	// Return the length of the message
	protected abstract int processMessage(char[] data, int startIdx) throws GameActionException;
	
	protected void subscribe(int stickyNum){
		subs[numSubs++] = stickyNum;
	}
	
	
	public int age(){
		return curRound - spawnRound;
	}
	
	
	private static final int ARTILLERY_DETECTED_MSG = 6;
	public char[] buildArtilleryDetectedMessage(MapLocation loc){ //## stupid naming scheme
		return new char[] {ARTILLERY_DETECTED_MSG, (char) loc.x, (char) loc.y};
	}

}
