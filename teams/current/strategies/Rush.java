package current.strategies;

import battlecode.common.GameActionException;
import battlecode.common.Upgrade;
import current.HQRobot;

public class Rush extends Strategy {

	public boolean researchDefusion = false;
	
	public Rush(HQRobot HQ) {
		super(HQ);
	}

	@Override
	public void checkStrategyChange() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void run() throws GameActionException {
		if(HQ.rc.isActive()){
			if(researchDefusion){
				if(!HQ.rc.hasUpgrade(Upgrade.DEFUSION)){
					HQ.rc.researchUpgrade(Upgrade.DEFUSION);
				}
				else researchDefusion = false;
			}
			else if(HQ.spawn()){
				HQ.sendInitializeMessage( HQ.buildRushMessage() );
			}
		}
	}

	@Override
	public void begin() throws GameActionException {
		HQ.comm.putSticky(1, HQ.buildRushMessage()); //##! not working somehow
	}

}
