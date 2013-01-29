package team097;

import battlecode.common.*;

public class ArtilleryDetector {

	private BaseRobot r;
	
	
	private boolean on; // Whether we team097ly care about artillery at all
	
	public boolean artilleryDetected;
	public MapLocation detectionLoc; // Where the artillery was detected
	private MapLocation[] possibleArtilleryLocs;
	private int trialIdx;
	private MapLocation artilleryLoc; // Once we've found the actual artillery
	
	
	ArtilleryDetector(BaseRobot robot){
		this.r = robot;
	}
	
	public boolean isOn(){
		return on;
	}
	public void turnOn(){
		on = true;
	}
	public void turnOff(){
		clearDetector();
		on = false;
	}
	
	
	public void update() throws GameActionException{
		if(artilleryDetected){
			if(artilleryLoc != null){
				if(!artilleryAlive()) clearDetector(); // It was there previously, so we've successfully killed it
			} else {
				searchForArtillery();
			}
		}
		// ## put damage detection here?
	}
	
	public MapLocation getTargetLocation(){
		if(artilleryLoc != null) return artilleryLoc;
		else return possibleArtilleryLocs[trialIdx];
	}
	
	
	public void detectedArtilleryAt(MapLocation dLoc) throws GameActionException{
		if(on && !artilleryDetected){
			artilleryDetected = true;
			detectionLoc = dLoc;
			
			// Get a list of all neutral/enemy encampment squares in artillery range
			possibleArtilleryLocs = r.rc.senseEncampmentSquares(dLoc, 64, Team.NEUTRAL); //## !! add one real square to rad for splash
			trialIdx = 0; //## probably unnecessary, just to be safe for now
		}
	}
	public void clearDetector(){ //? name?
		detectionLoc = null;
		artilleryLoc = null;
		possibleArtilleryLocs = null;
		trialIdx = 0;
		artilleryDetected = false;
	}
	

	private void searchForArtillery() throws GameActionException{
		if(possibleArtilleryLocs.length == 0){ // There can't be an artillery, false alarm
			clearDetector();
			return;
		}
		while(r.rc.canSenseSquare(possibleArtilleryLocs[trialIdx])){
			Robot ar = (Robot) r.rc.senseObjectAtLocation(possibleArtilleryLocs[trialIdx]);
			if(ar != null){
				RobotInfo ri = r.rc.senseRobotInfo(ar);
				if(ri.type == RobotType.ARTILLERY){
					artilleryLoc = ri.location;
					break;
				}
			}
			trialIdx++;
			
			// Tried all of them; must have missed the artillery's death
			if(trialIdx == possibleArtilleryLocs.length){ 
				clearDetector();
				break;
			}
		}
	}
	
	private boolean artilleryAlive() throws GameActionException{
		if(r.rc.canSenseSquare(artilleryLoc)){
			Robot ar = (Robot) r.rc.senseObjectAtLocation(artilleryLoc);
			if(ar != null && r.rc.senseRobotInfo(ar).type == RobotType.ARTILLERY){
				return true;
			}
			return false;
		}
		return true;
	}
}
