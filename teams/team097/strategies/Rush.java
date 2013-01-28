package team097.strategies;

import battlecode.common.GameActionException;
import battlecode.common.Upgrade;
import team097.HQRobot;

// ## Hacky

public class Rush extends Strategy {

	public boolean researchDefusion = false;
	public boolean nukeRush = false;
	
	public Rush(HQRobot HQ) {
		super(HQ);
	}

	@Override
	public void checkStrategyChange() throws GameActionException {
		if(HQ.rc.senseEnemyNukeHalfDone()){
			nukeRush = true;
			researchDefusion = true;
		}
	}

	@Override
	public void run() throws GameActionException {
		if(HQ.rc.isActive()){
			if(researchDefusion){
				if(!HQ.rc.hasUpgrade(Upgrade.DEFUSION)){
					HQ.rc.researchUpgrade(Upgrade.DEFUSION);
				}
				else {
					researchDefusion = false;
					spawnRusher();
				}
			}
			else spawnRusher();
		}
	}
	
	private void spawnRusher() throws GameActionException{
		if(HQ.spawn()){
			HQ.sendInitializeMessage( HQ.buildRushMessage(nukeRush) );
		}
	}

	@Override
	public void begin() throws GameActionException {
		HQ.comm.putSticky(1, HQ.buildRushMessage(nukeRush));
	}

}
