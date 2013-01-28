package team097;

import battlecode.common.*;

public class PassiveEncampment extends BaseRobot {
	
	PassiveEncampment(RobotController rc) throws GameActionException{
		super(rc);
//		sendAliveMessage();
	}

	// ## still need to test this
	// Oops, this whole thing is stupid, we can just use (and likely process and cache) senseAlliedEncampments() :P
	// ## remove keepalive stuff
//	private boolean doomed = false;
	public void run() throws GameActionException
	{
//		// Not a perfect calculation, might want to divide by allies
//		// Also won't help if an artillery kills us, would need keepalive
//		if(rc.getEnergon() <= 6*rc.senseNearbyGameObjects(Robot.class, 8, enemyTeam).length){
//			doomed = true;
//			sendDoomedMessage();
//		} else if(doomed){ // We unexpectedly survived
//			doomed = false;
//			sendAliveMessage();
//		}
		rc.yield();
	}
	
//	private static final int ALIVE_MSG = 14;
//	private void sendAliveMessage() throws GameActionException{
//		comm.sendToId(util.getHQID(), new char[]{ALIVE_MSG, (char) myType.ordinal(), 0} );
//	}
//	private static final int DOOMED_MSG = 15;
//	private void sendDoomedMessage() throws GameActionException{
//		comm.sendToId(util.getHQID(), new char[]{DOOMED_MSG, (char) myType.ordinal(), 0} );
//	}

	@Override
	protected int processMessage(char[] data, int startIdx) { return 0; }
}
