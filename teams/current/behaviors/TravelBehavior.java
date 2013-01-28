package current.behaviors;

import battlecode.common.*;
import current.SoldierRobot;

// ## destination behavior; default to prevbehavior? Ask HQ?
// ## waypoints

public class TravelBehavior extends Behavior {

	public enum EnemyResponse { ENGAGE, AVOID, IGNORE }
	public EnemyResponse enemyResponse;
	
	public enum MovementMode { SEARCH, TUNNEL, BUG }
	public MovementMode movementMode;
	
	// If within rushDistance, ignore enemies
	//@R public int rushDistance;
	
	public MapLocation destination;
	
	
	public TravelBehavior(SoldierRobot r) {
		super(r);
		reset();
	}

	public void checkBehaviorChange() {
		if(enemyResponse != EnemyResponse.IGNORE){ //@R && r.util.getDistanceBetween(r.curLoc, destination) > rushDistance
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
		switch(movementMode){	
		case SEARCH:
			r.nav.moveTo(destination);
			break;
		case TUNNEL:
			r.nav.tunnelTo(destination);
			break;
		case BUG:
			r.nav.tunnelTo(destination); //## haven't added bug to main navigator yet
		}
	}

	public void reset(){
		enemyResponse = EnemyResponse.ENGAGE;
		movementMode = MovementMode.TUNNEL; //## not the best default once others work
		//@R rushDistance = 0;
	}
	
}
