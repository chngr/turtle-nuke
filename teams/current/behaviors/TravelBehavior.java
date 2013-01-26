package current.behaviors;

import battlecode.common.*;
import current.SoldierRobot;

// ## rush distance?
// ## destination behavior; default to prevbehavior
// ## waypoints

public class TravelBehavior extends Behavior {

	public enum EnemyResponse { ENGAGE, AVOID, IGNORE }
	public EnemyResponse enemyResponse;
	
	public enum MovementMode { SEARCH, TUNNEL, BUG } //## search depth?
	
	
	public MapLocation destination;
	
	
	public TravelBehavior(SoldierRobot r) {
		super(r);
	}

	public void checkBehaviorChange() {
		if(enemyResponse != EnemyResponse.IGNORE){
			if(r.util.senseDanger()){
				if(enemyResponse == EnemyResponse.ENGAGE){
					r.setBehavior(r.combatBehavior);
				} else if(enemyResponse == EnemyResponse.AVOID){
					// ##
				}
			}
		}
	}
	
	public void run() throws GameActionException {
		r.nav.tunnelTo(destination); //##
	}

	
	
}
