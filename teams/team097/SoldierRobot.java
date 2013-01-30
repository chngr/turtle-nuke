package team097;

import team097.behaviors.*;
import battlecode.common.*;


public class SoldierRobot extends BaseRobot {
	
	public Behavior currentBehavior;
	public Behavior prevBehavior;
	
	public CombatBehavior combatBehavior;
	public CaptureBehavior captureBehavior;
	public SwarmBehavior swarmBehavior; // This should maybe be a slow creep, not a blind rush; or that could be a different behavior
	public FortifyBehavior fortifyBehavior;
	public ScoutBehavior scoutBehavior;
	public TravelBehavior travelBehavior;
	public HuntBehavior huntBehavior;
	
	// Artillery detection
	// If we take damage unexpectedly, there must be an enemy artillery
	public double curEnergon = 40;
	public double prevEnergon = 40;
	public double curShields = 0;
	public double prevShields = 0;
	public Robot[] engagedEnemies = new Robot[0]; // Empty array
	
	SoldierRobot(RobotController rc) throws GameActionException{
		super(rc);
		combatBehavior = new CombatBehavior(this);
        captureBehavior = new CaptureBehavior(this);
        swarmBehavior = new SwarmBehavior(this);
        fortifyBehavior = new FortifyBehavior(this);
        scoutBehavior = new ScoutBehavior(this);
        travelBehavior = new TravelBehavior(this);
        huntBehavior = new HuntBehavior(this);
        currentBehavior = scoutBehavior; //default
        readMessages(comm.getSticky(2)); // Initialization message
	}


	public void run() throws GameActionException{
		super.run();
		
		// Artillery detection
		if(detector.isOn() && !detector.artilleryDetected && detectArtilleryAttack()){
			comm.putSticky(Communicator.ARTILLERY_DETECTION_SPACE, buildArtilleryDetectedMessage(curLoc));
		}
		prevEnergon = curEnergon;
		curEnergon = rc.getEnergon();
		prevShields = curShields;
		curShields = rc.getShields();
		engagedEnemies = rc.senseNearbyGameObjects(Robot.class, 8, enemyTeam);
		
		if (rc.isActive()) {			
			currentBehavior.checkBehaviorChange();
			currentBehavior.run();
		}
		//## use remaining bytecodes if we don't need to conserve power
	}
	
	// Return the length of the message
	@Override
	protected int processMessage(char[] data, int startIdx) throws GameActionException{
		//System.out.println("Recieved message: header "+(int)data[startIdx]); //DEBUG
		switch(data[startIdx]){
		case 0:
			// HACK: this seems to get called ~40 times/round (when we have no data?), so break the message read
			// loop when it happens. Doesn't appear to mess with proper messages.
			return data.length; 
					
		case 1:  // Subscribe @@ channelnum | 0
			subscribe(data[startIdx+1]);
			break;
			
		case 2: // Fortify: default (headquarters, radius 4, two directions)
			setBehavior(fortifyBehavior);
			fortifyBehavior.buildFort(HQ, 2, HQ.directionTo(rc.senseEnemyHQLocation()).rotateLeft().rotateLeft(), 3);
			break;
			
		case 3: // Rush @@ nukeRush? | 0
			setBehavior(swarmBehavior);
			swarmBehavior.init();
			swarmBehavior.target = eHQ;
			if(data[startIdx+1] == 1) combatBehavior.setNukeRush();
			//travelBehavior.rushDistance = 2; //## is this helpful?
			break;
			
		case 4: // Swarm @@ target.x | target.y
			setBehavior(swarmBehavior);
			swarmBehavior.init();
			swarmBehavior.target = new MapLocation(data[startIdx+1],data[startIdx+2]);
			break;
			
		case 5: // Capture @@ encampmentOrdinal | 0
			setBehavior(captureBehavior);
			if(data[startIdx+1] != 0)
				captureBehavior.encampmentType = RobotType.values()[data[startIdx+1] - 1];
			break;
			
		case 6: // Artillery detected @@ detectionLoc.x | detectionLoc.y
			detector.detectedArtilleryAt(new MapLocation(data[startIdx+1],data[startIdx+2]));
			break;
			
		case 7: // Hunt enemy encampments @@ left? | 0
			setBehavior(huntBehavior);
			if(data[startIdx+1] == 1){ //
				huntBehavior.left = true;	
			}
			huntBehavior.initPath();

			break;

			
		default:
			System.out.println("Unrecognised message: " + (int)data[startIdx]); //DEBUG*
		}
		return 3; // Minimum message length
	}
	
	public void setBehavior(Behavior b){
		//rc.setIndicatorString(1, b.getClass().getName()); //DEBUG
		prevBehavior = currentBehavior;
		currentBehavior = b;
	}
	
	
	public boolean detectArtilleryAttack(){
		double eDelta = prevEnergon - curEnergon;
		double sDelta = prevShields - curShields - 1; // Base decay rate
		
		// ## could be improved
		if(eDelta + sDelta >= 15){ // Artillery splash damage
			boolean onMine = util.senseHostileMine(curLoc);
			if(eDelta > 6*engagedEnemies.length + (onMine ? 10 : 0)
				|| sDelta > (onMine ? 9 : 0)){ // 90% mine absorption
				return true;
			}
		}
		return false;
	}
	
}
