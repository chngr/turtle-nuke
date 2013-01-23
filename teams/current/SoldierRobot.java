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
		if (rc.isActive()) {
			readAllMessages(); // in BaseRobot
			
			currentBehavior.checkBehaviorChange();
			currentBehavior.run();
			//## use remaining bytecodes if we don't need to conserve power
			rc.yield();
		}
	}
	
	// Return the length of the message
	@Override
	protected int processMessage(char[] data, int startIdx){
		switch(data[startIdx]){
		case 0: // Subscribe
			subscribe(data[startIdx+1]);
			
		case 1: // Fortify: default (headquarters, radius 2, all directions)
			setBehavior(fortifyBehavior);
			fortifyBehavior.buildFort(HQ, 2, HQ.directionTo(rc.senseEnemyHQLocation()), 4);
			break;
			
		default:
			System.out.println("Unrecognised message"); //DEBUG
		}
		return 3; // Minimum message length
	}
	
	
	public void setBehavior(Behavior b){
		prevBehavior = currentBehavior;
		currentBehavior = b;
	}
}
