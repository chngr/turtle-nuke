package current.strategies;

import current.HQRobot;
import battlecode.common.*;

//TODO: Initial path clearing scouts (travel mode flee?)
//TODO: Combat channel: call unengaged allies when we fight. Swarm behavior?
//TODO: Defend HQ; in general, sense local danger (for this, prioritize spawn)
//TODO: Idle mining
//TODO: Encampment improvements

public class Swarm extends Strategy {

	public int swarmThreshold = 20;
	
	private int initialCapturePeriod;
	
	private MapLocation rallyPoint; //## may not want this in a more sophisticated version
	
	private int rushDelay = 30; // Min time between charging
	private int coolDown = 0;
	
	public Swarm(HQRobot HQ) {
		super(HQ);
	}

	@Override
	public void checkStrategyChange() throws GameActionException {
		if(HQ.rc.senseEnemyNukeHalfDone()){
			HQ.rush.nukeRush = true;
			HQ.rush.researchDefusion = true;
			HQ.setStrategy(HQ.rush);
		}
	}

	@Override
	public void begin() throws GameActionException {
		initialCapturePeriod = HQ.effectiveHQDist/2; //?
		rallyPoint = HQ.curLoc.add(HQ.curLoc.directionTo(HQ.eHQ), 6); // A sixth of the way towards the enemy; ok?
	}

	@Override
	public void run() throws GameActionException {

		if(HQ.rc.isActive()){
			if(HQ.curRound < initialCapturePeriod){ //## should extract these tests
				if(HQ.spawn()) HQ.sendInitializeMessage( HQ.buildCaptureMessage(chooseEncampment()) );
				else research();
			} else {
				if(!HQ.lowPower() && HQ.spawn()) HQ.sendInitializeMessage( HQ.buildSwarmMessage(rallyPoint) );
				else research();
			}
		}
		
		// Check whether to charge
		if( (HQ.lowPower() 
		     || HQ.rc.senseNearbyGameObjects(Robot.class, rallyPoint, 34, HQ.myTeam).length > swarmThreshold)
				&& coolDown == 0){
			
			HQ.comm.putSticky(1, HQ.buildRushMessage(false));
			coolDown = rushDelay;
		}
		if(coolDown > 0) coolDown--;
		//## else plan stuff, at least initially: improve map evaluation, determine next encampment locations...
	}
	
	private void research() throws GameActionException{
		if(!HQ.rc.hasUpgrade(Upgrade.DEFUSION)) HQ.rc.researchUpgrade(Upgrade.DEFUSION);
		else if(!HQ.rc.hasUpgrade(Upgrade.FUSION)) HQ.rc.researchUpgrade(Upgrade.FUSION);
		else HQ.rc.researchUpgrade(Upgrade.NUKE);
	}

//	//## this encampment logic is bad, we should improve it
//	private int encampmentCount = 0; // Currently not adjusted for lost encampments
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
	
	// ## choose better; particularly artillery
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
			RobotType.SUPPLIER
	};
	private int buildIdx = 0;
	private int chooseEncampment(){
		return buildOrder[buildIdx++].ordinal(); //## if we add encampments later, need a different mechanism for it
	}
}
