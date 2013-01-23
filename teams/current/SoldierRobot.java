package current;

import current.behaviors.*;
import battlecode.common.*;

public class SoldierRobot extends BaseRobot {
	
	private Behavior currentBehavior;
	private Behavior prevBehavior;
	
	private CombatBehavior combatBehavior;
	private CaptureBehavior captureBehavior;
	private SwarmBehavior swarmBehavior; // This should maybe be a slow creep, not a blind rush; or that could be a different behavior
	private FortifyBehavior fortifyBehavior;
	private ScoutBehavior scoutBehavior;

	
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
			//## read messages, changing state if instructed to
			currentBehavior.checkBehaviorChange();
			currentBehavior.run();
			//## use remaining bytecodes if we don't need to conserve power
			rc.yield();
		}
	}
}
