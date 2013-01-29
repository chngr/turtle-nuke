package current.behaviors;

import current.*;
import battlecode.common.*;

// Artillery detection:
// We don't need to notify when locations are checked, when one
// robot can see it all the others will have a chance to sense
// before that robot would move away
// ## New robots won't know the current progress, and progress is lost with
// the original swarm; HQ sensing through them after all? then no destroyed msg !! @*

// ## Also if the lone sensor is destroyed immediately;
// probably not critical

//## when we get to the end of possible locs go back to normal swarm

//## send destroyed message where we detect destroyed (not in clearArt...)

// !! ## How does the cycling possible locations interact with combat? still need to do it, how to organise?
// call combat.fight() [combat.fightInDir(dir)] from here and switch to swarm mode when we recieve artillery detected?
// merge (i.e. copy and don't call original) combat behavior into swarm? but the stalemate is useful for rush...

public class SwarmBehavior extends Behavior {

	// Destination location
	public MapLocation target;
	
	// Artillery hunting variables
	public boolean artilleryHunting; //##?
	private MapLocation artilleryDetection; // Where the artillery was detected
	private MapLocation[] possibleArtilleryLocs;
	private int trialIdx;
	public MapLocation artilleryLoc; // Once we've found the actual artillery
	
	//##shield loc?
	
	public SwarmBehavior(SoldierRobot r){
		super(r);
	}
	
	public void run() throws GameActionException{
		if(artilleryHunting){
			if(artilleryLoc == null){
				searchForArtillery(); // Always search, so we don't miss someone finding it
			}
			// Only hunt the artillery if somewhat nearby; don't want to mess up rallying
			//##? 4*artilleryRadSquared, max danger zone - will this be a problem on small maps? !! almost; splash
			if(r.curLoc.distanceSquaredTo(artilleryDetection) < 256){ 
				if(artilleryLoc != null){
					// [fightInDir,?] artilleryLoc
				} else {
					// [fightInDir,?] possibleArtilleryLocs[trialIdx]
				}
			}
		} else {
			r.nav.tunnelTo(target);
		}
	}
	
	public void checkBehaviorChange(){
		if(r.util.senseDanger()){
			r.setBehavior(r.combatBehavior);
		}
	}
	
	public void detectedArtillery(MapLocation detectionLoc) throws GameActionException{
		// Get a list of all neutral/enemy encampment squares in artillery range
		possibleArtilleryLocs = r.rc.senseEncampmentSquares(detectionLoc, 64, Team.NEUTRAL); //## !! add one real square to rad for splash
		trialIdx = 0; //## probably unnecessary, just to be safe for now
	}
	public void clearArtilleryMode(){ //? name?
		artilleryLoc = null;
		trialIdx = 0;
		artilleryHunting = false;
	}
	
	//##
	private void searchForArtillery() throws GameActionException{
		while(r.rc.canSenseSquare(possibleArtilleryLocs[trialIdx])){
			Robot e = (Robot) r.rc.senseObjectAtLocation(possibleArtilleryLocs[trialIdx]);
			if(e != null){
				RobotInfo ri = r.rc.senseRobotInfo(e);
				if(ri.type == RobotType.ARTILLERY){
					artilleryLoc = ri.location;
					//## @* do next phase now
					break;
				}
			}
			trialIdx++; //## should we have a trialLoc after all?
			
			// Tried all of them; must have missed the artillery's death
			if(trialIdx == possibleArtilleryLocs.length){ 
				clearArtilleryMode();
				break;
			}
		}
	}
}
