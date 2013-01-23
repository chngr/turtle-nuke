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
	
	// Headquarter locations
	public final MapLocation HQ;
	public final MapLocation eHQ;
	
	// State variables
	public int curRound;
	public MapLocation curLoc;

	BaseRobot(RobotController myRC){
		// Initialize subsystems
		this.rc = myRC;
		this.util = new Utilities(this);
		this.comm = new Communicator(this);
		this.nav = new Navigator(this);
		this.combat = new Combat(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();
		
		// Initialize team objects
		this.myTeam = rc.getTeam();
		this.enemyTeam = myTeam.opponent();
		
		// Initialize headquarter locations
		this.HQ = rc.senseHQLocation();
		this.eHQ = rc.senseEnemyHQLocation();
		
		// Initialize state variables
		this.maph = rc.getMapHeight();
		this.mapw = rc.getMapWidth();
	}
	
	public void run() throws GameActionException{
		this.curLoc = rc.getLocation();
		this.curRound = Clock.getRoundNum();
	}
	
	

	// --- Message parsing ---
	//
	// Header byte contains guard conditions and message type
	// Format:
	//    4    4
	// |guard|type|
	// If more message types are needed, the guard could be reduced to 3 bits
	//
	// Messages are limited to a single 3 byte group (2 bytes of data)
	// If you need more than this, split it into a separate setParam message
	
	protected void readMessages(char[] messageData){
		int msgIdx = 0;
		while(msgIdx < messageData.length){
			if( checkGuard(messageData[msgIdx]) ){ // @ Guard is probably only relevant for sticky
				processMessage(messageData, msgIdx);
			}
			msgIdx += 3;
		}
	}
	
	protected abstract void processMessage(char[] data, int startIdx);

	// Return whether or not the message is for us
	private boolean checkGuard(char header){
		
		// @ Instead could have bits indicate conditions, combinatorially
		switch(header >> 4){
		case 0: // No guard
			break;
		case 1: // Initialization message (HQ can't get the id of spawned robot)
			if(age() > 1) return false;
			return true;
			
		default:
			System.out.println("Unrecognised guard condition"); //DEBUG
		}
		return true;
	}
	
	
	
	public int age(){
		return curRound - spawnRound;
	}

}
