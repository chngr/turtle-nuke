package current.behaviors;

import current.*;
import battlecode.common.*;


public abstract class Behavior {
	
	protected SoldierRobot r;
	
	Behavior(SoldierRobot r){
		this.r = r;
	}
	
	public void run() throws GameActionException{
		
	}
	public void checkBehaviorChange(){
		
	}

}
