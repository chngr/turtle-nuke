package current;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {

	
	SoldierRobot(RobotController rc){
		super(rc);
	}

	public void run() throws GameActionException
	{
		if (rc.isActive()) {
//			char[] rcvMsg = comm.receive(comm.IDtoCurFreq(-1));
//			rc.setIndicatorString(0, Integer.toString(rcvMsg.length));
//			if (rcvMsg[0] == 'a')
//				rc.suicide();
		
			if (Math.random()<0.005) {
				// Lay a mine
				if(rc.senseMine(rc.getLocation())==null)
					rc.layMine();
			} else {
				// Choose a random direction, and move that way if possible
				Direction dir = Direction.values()[(int)(Math.random()*8)];
				if(rc.canMove(dir)) {
					rc.move(dir);
					//rc.setIndicatorString(0, "Last direction moved: "+dir.toString());
				}
			}
		}

		if (Math.random()<0.01 && rc.getTeamPower()>5) {
			// Write the number 5 to a position on the message board corresponding to the robot's ID
			rc.broadcast(rc.getRobot().getID()%GameConstants.BROADCAST_MAX_CHANNELS, 5);
		}
	}
}
