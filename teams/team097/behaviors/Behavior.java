package team097.behaviors;

import team097.*;
import battlecode.common.*;


public abstract class Behavior {
	
	protected SoldierRobot r;
	
	Behavior(SoldierRobot r){
		this.r = r;
	}
	
	public abstract void checkBehaviorChange();
	public abstract void run() throws GameActionException;
	
}
