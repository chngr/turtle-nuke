package team097.behaviors;

import team097.*;
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
