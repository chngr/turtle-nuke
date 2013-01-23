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

	
	SoldierRobot(RobotController rc){
		super(rc);
		combatBehavior = new CombatBehavior(this);
        captureBehavior = new CaptureBehavior(this);
        swarmBehavior = new SwarmBehavior(this);
        fortifyBehavior = new FortifyBehavior(this);
        scoutBehavior = new ScoutBehavior(this);
        currentBehavior = scoutBehavior; //default
	}


	public void run() throws GameActionException{
		if (rc.isActive()) {
			readMessages(comm.getSticky()); // in BaseRobot
			readMessages(comm.receive());
			
			currentBehavior.checkBehaviorChange();
			currentBehavior.run();
			//## use remaining bytecodes if we don't need to conserve power
			rc.yield();
		}
	}
	
	@Override
	protected void processMessage(char[] data, int startIdx){
		switch(data[startIdx] & 0xF){
		case 0: // Fortify: default (headquarters, radius 2, all directions)
			setBehavior(fortifyBehavior);
			fortifyBehavior.buildFort(HQ, 2, HQ.directionTo(rc.senseEnemyHQLocation()), 4);
			break;
			
		default:
			System.out.println("Unrecognised message"); //DEBUG
		}
	}
	
	
	public void setBehavior(Behavior b){
		prevBehavior = currentBehavior;
		currentBehavior = b;
	}
}
