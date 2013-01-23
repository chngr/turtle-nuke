package current.strategies;

import current.*;
import battlecode.common.*;

public abstract class Strategy {
	
	public HQRobot HQ;
	
	public Strategy(HQRobot HQ){
		this.HQ = HQ;
	}

	public void evaluatePerformance(){
		
	}
	
	public void run() throws GameActionException {
		
	}
	
	
}
