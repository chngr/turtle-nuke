package team097.strategies;

import team097.*;
import battlecode.common.*;

public abstract class Strategy {
	
	public HQRobot HQ;
	
	public Strategy(HQRobot HQ){
		this.HQ = HQ;
	}

	public abstract void checkStrategyChange() throws GameActionException;
	public abstract void begin() throws GameActionException;
	public abstract void run() throws GameActionException ;
	
}
