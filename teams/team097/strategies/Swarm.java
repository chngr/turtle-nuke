package team097.strategies;

import team097.Communicator;
import team097.HQRobot;
import battlecode.common.*;

//TODO: When shields, set as rally point and send [shield notification message?]

//##TODO: switch to artillery detector
//TODO: Persistent artillery search: send the current index
//##TODO: Use the spawnSwarm method

//TODO: Initial path clearing scout(s) (travel mode flee?)
//TODO: Combat channel: call unengaged allies when we fight. Swarm behavior?
//TODO: Defend HQ; in general, sense local danger (for this, prioritize spawn)
//TODO: Idle mining
//TODO: Encampment improvements

public class Swarm extends Strategy {

	public int swarmThreshold = 20;// ##? map size dependant?
	
	private int initialCapturePeriod;
	
	private MapLocation rallyPoint;
	private int rallyCount; // Number of our robots near the rally point
	
	private int rushDelay = 30; // Min time between charging
	private int coolDown = 0;
	
	public Upgrade prioritizedResearch;
	
	// Ad hoc assignment of other behaviors
	public boolean captureNext;
	public boolean huntNext;
	
	private boolean capt1;
	private boolean capt2;
	private boolean hunt;
	private boolean huntLeft = true; // Whether to hunt on the left or right
	
	private boolean enemyNuke;
	private int roundsSinceNukeDetected = 0;
	
	public Swarm(HQRobot HQ) {
		super(HQ);
	}

	// ## No longer actually a strategy change
	public void checkStrategyChange() throws GameActionException {
		if(enemyNuke){
			roundsSinceNukeDetected++;
			swarmThreshold = Math.max(20 - roundsSinceNukeDetected/10,0); //##?
		} else if(HQ.rc.senseEnemyNukeHalfDone()){
			enemyNuke = true;
			rushDelay = 5;
			prioritizedResearch = Upgrade.DEFUSION;
			HQ.comm.putSticky(Communicator.SWARM_SPACE, HQ.buildRushMessage(true));
			// ## make a couple of backdoorers
		}
		
		rallyCount = HQ.rc.senseNearbyGameObjects(Robot.class, rallyPoint, 34, HQ.myTeam).length;
		if(capt1 && rallyCount > 4){
			captureNext = true;
			capt1 = false;
		}
		else if(hunt && rallyCount > 8){
			huntNext = true;
			hunt = false;
			huntLeft = !huntLeft;
		}
		else if(capt2 && rallyCount > 14){
			captureNext = true;
			capt2 = false;
		}
	}

	@Override
	public void begin() throws GameActionException {
		initialCapturePeriod = HQ.effectiveHQDist/2; //?
		rallyPoint = HQ.curLoc.add(HQ.curLoc.directionTo(HQ.eHQ), 6); // A sixth of the way towards the enemy; ok?
	}

	@Override
	public void run() throws GameActionException {

		if(HQ.detector.artilleryDetected && !HQ.rc.hasUpgrade(Upgrade.DEFUSION)){
			prioritizedResearch = Upgrade.DEFUSION;
		}
		
		
		if(HQ.rc.isActive()){
			if(HQ.curRound < initialCapturePeriod){ //## should extract these tests
				if(HQ.spawn()) HQ.sendInitializeMessage( HQ.buildCaptureMessage(chooseEncampment()) );
				else research();
			} else if(prioritizedResearch != null) {
				if(!HQ.rc.hasUpgrade(prioritizedResearch)){
					HQ.rc.researchUpgrade(prioritizedResearch);
				} else {
					prioritizedResearch = null;
					spawnSwarm();
				}
			} else if(captureNext){
				if(HQ.spawn()){
					HQ.sendInitializeMessage( HQ.buildCaptureMessage(chooseEncampment()) );
					captureNext = false;
				}
				else research();
			} else if(huntNext){
				if(HQ.spawn()){
					HQ.sendInitializeMessage( HQ.buildHuntMessage(huntLeft) );
					huntNext = false;
				}
				else research();
			}
			else {
				if(HQ.lowPower()) research();
				else spawnSwarm();
			}
		}
		
		
		// Check whether to charge
		if( (HQ.lowPower() 
		     || rallyCount > swarmThreshold)
				&& coolDown == 0){
			if(enemyNuke){
				HQ.comm.putSticky(Communicator.SWARM_SPACE, HQ.buildRushMessage(true));
			} else {
				HQ.comm.putSticky(Communicator.SWARM_SPACE, HQ.buildSwarmMessage(HQ.eHQ));
			}
			coolDown = rushDelay;
			if(!enemyNuke){
				capt1 = true; // Reset the captures for this wave
				capt2 = true;
			}
			hunt = true;
		}
		if(coolDown > 0) coolDown--;
		//## else plan stuff, at least initially: improve map evaluation, determine next encampment locations...
	}
	
	//## vision for artillery; better system in general
	private void research() throws GameActionException{
		if(!HQ.rc.hasUpgrade(Upgrade.DEFUSION)) HQ.rc.researchUpgrade(Upgrade.DEFUSION);
		else if(!HQ.rc.hasUpgrade(Upgrade.FUSION)) HQ.rc.researchUpgrade(Upgrade.FUSION);
		else if(!HQ.rc.hasUpgrade(Upgrade.VISION)) HQ.rc.researchUpgrade(Upgrade.VISION);
		else HQ.rc.researchUpgrade(Upgrade.NUKE);
	}

//	//## this encampment logic is bad, we should improve it
//	private int encampmentCount = 0; // currently not adjusted for lost encampments
//	private int encampmentTimer = 0;
//	private int encampmentPeriod;
//	private boolean wantEncampment(){
//		if()
//			//## arbitrary param
//			if(encampmentCount < HQ.effectiveHQDist/20){ // Initial encampments
//				encampmentCount++;
//				return true;
//			} else if(encampmentTimer >= encampmentPeriod + encampmentCount){ // Periodically get encampments
//				encampmentTimer = 0;
//				encampmentCount++;
//				return true;
//			} else{
//				encampmentTimer++;
//				return false;
//			}
//	}
	
	// ## choose better
	private RobotType[] buildOrder = {
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.GENERATOR,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.GENERATOR,
			RobotType.GENERATOR,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.GENERATOR,
			RobotType.GENERATOR,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.GENERATOR,
			RobotType.GENERATOR,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER,
			RobotType.SUPPLIER
	};
	private int buildIdx = 0;
	private int chooseEncampment(){
		if(buildIdx < buildOrder.length){
			return buildOrder[buildIdx++].ordinal() + 1; // 0 means the robot chooses
		} else {
			return 0;
		}
	}
	
	
	private void spawnSwarm() throws GameActionException{
		if(HQ.spawn()){
			HQ.sendInitializeMessage( HQ.buildSwarmMessage(rallyPoint) );
			if(HQ.detector.artilleryDetected){
				//## !! @* send the index for persistent search - will also work if we've found it
				//HQ.sendInitializeMessage( HQ.buildArtilleryDetectedMessage(HQ.detector.detectionLoc) );
				// BUG: for some reason we can't do multiple initializations; this instead for now
				// ## Sending this seems to hurt performance actually
				//HQ.comm.putSticky(Communicator.SWARM_SPACE, HQ.buildArtilleryDetectedMessage(HQ.detector.detectionLoc));
			}
		} else { // We're blocked; shouldn't happen
			HQ.comm.putSticky(Communicator.SWARM_SPACE, HQ.buildSwarmMessage(HQ.eHQ)); // Send forth the swarm; might unblock us
			research();
		}
	}
	
}
