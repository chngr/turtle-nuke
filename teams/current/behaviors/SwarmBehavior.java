package current.behaviors;

import current.*;
import battlecode.common.*;


public class SwarmBehavior extends Behavior {

	public MapLocation target;
	
	public SwarmBehavior(SoldierRobot r){
		super(r);
	}
	
	public void run() throws GameActionException{
		r.nav.tunnelTo(target);
	}
	
	public void checkBehaviorChange(){
		
	}
}
