package current.behaviors;

import current.*;
import battlecode.common.*;


public class CombatBehavior extends Behavior {

	public CombatBehavior(SoldierRobot r){
		super(r);
	}
	
	public void run() throws GameActionException{
		r.combat.fight();
	}
	
	public void checkBehaviorChange(){
		
	}
}
