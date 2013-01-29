package current;

import current.behaviors.*;
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

	
	SoldierRobot(RobotController rc) throws GameActionException{
		super(rc);
		combatBehavior = new CombatBehavior(this);
        captureBehavior = new CaptureBehavior(this);
        swarmBehavior = new SwarmBehavior(this);
        fortifyBehavior = new FortifyBehavior(this);
        scoutBehavior = new ScoutBehavior(this);
        travelBehavior = new TravelBehavior(this);
        currentBehavior = scoutBehavior; //default
        readMessages(comm.getSticky(2)); // Initialization message
	}


	public void run() throws GameActionException{
		super.run();
		if (rc.isActive()) {
			readAllMessages(); // in BaseRobot
			
			currentBehavior.checkBehaviorChange();
			currentBehavior.run();
			//## use remaining bytecodes if we don't need to conserve power
		}
	}
	
	// Return the length of the message
	@Override
	protected int processMessage(char[] data, int startIdx){
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
			setBehavior(travelBehavior);
			travelBehavior.reset();
			travelBehavior.destination = eHQ;
			if(data[startIdx+1] == 1) combatBehavior.setNukeRush();
			//travelBehavior.rushDistance = 2; //## is this helpful?
			break;
			
		case 4: // Swarm @@ target.x | target.y
			setBehavior(travelBehavior);
			travelBehavior.reset();
			travelBehavior.destination = new MapLocation(data[startIdx+1],data[startIdx+2]);
			break;
			
		case 5: // Capture @@ encampmentOrdinal | 0
			// Better would be to allow forced encampmentType change
			// Do we have access to channel of each message? would be best to overwrite it once accepted
			if (currentBehavior != captureBehavior) { 
				setBehavior(captureBehavior);
				captureBehavior.encampmentType = RobotType.values()[data[startIdx+1]];
				//System.out.println(captureBehavior.encampmentType+" received capture goal");
			}
			break;
			
			
		default:
			System.out.println("Unrecognised message: " + (int)data[startIdx]); //DEBUG
		}
		return 3; // Minimum message length
	}
	
	
	public void setBehavior(Behavior b){
		rc.setIndicatorString(1, b.getClass().getName()); //DEBUG
		prevBehavior = currentBehavior;
		currentBehavior = b;
	}
}
