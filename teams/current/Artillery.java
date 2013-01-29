package current;

import battlecode.common.*;

//TODO: Less stupid target selection; remember to especially avoid damaging our encampments/HQ

public class Artillery extends BaseRobot {

	Artillery(RobotController rc){
		super(rc);
	}
	
	private boolean doomed = false;
	public void run() throws GameActionException
	{
		if(rc.isActive()){
			// Choose target
			// ## should be like combat, build local map and search for good location
			Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 64, enemyTeam);
			if(enemies.length > 0) rc.attackSquare(rc.senseLocationOf(enemies[0]));
		}
		
	
//		// Decide if we're about to die
//		// Not a perfect calculation, might want to divide by allies
//		// Also won't help if an artillery kills us, would need keepalive
//		if(rc.getEnergon() <= 6*rc.senseNearbyGameObjects(Robot.class, 8, enemyTeam).length){
//			doomed = true;
//			sendDoomedMessage();
//		} else if(doomed){ // We unexpectedly survived
//			doomed = false;
//			sendAliveMessage();
//		}
		
		//rc.suicide();
	}

//
//	private static final int ALIVE_MSG = 14;
//	private void sendAliveMessage() throws GameActionException{
//		comm.sendToId(util.getHQID(), new char[]{ALIVE_MSG, (char) myType.ordinal(), 0} );
//	}
//	private static final int DOOMED_MSG = 15;
//	private void sendDoomedMessage() throws GameActionException{
//		comm.sendToId(util.getHQID(), new char[]{DOOMED_MSG, (char) myType.ordinal(), 0} );
//	}
//	
	@Override
	protected int processMessage(char[] data, int startIdx) { return 0; }
}
