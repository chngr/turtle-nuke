package current.behaviors;

import current.*;
import battlecode.common.*;


public abstract class Behavior {
	
	protected SoldierRobot r;
	
	Behavior(SoldierRobot r){
		this.r = r;
	}
	
	public abstract void checkBehaviorChange();
	public abstract void run() throws GameActionException;
	
}
