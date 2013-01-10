package testplayer;

import battlecode.common.*;

public class RobotPlayer
{
	// Subsystems
	public final RobotController rc;
	public final Communicator comm;
	
	// Robot data
	public final RobotType myType;
	public final int id = -1;
	public final int spawnRound;
	
	// Robot state
	public int curRound;

	public static void run(RobotController myRC)
	{
		// Initialize subsystems
		this.rc = myRC;
		this.comm = new Communicator(this);
		
		// Initialize data
		this.id = rc.getRobot().getID();
		this.spawnRound = Clock.getRoundNum();
		this.myType = rc.getType();

		// Yay let's start doing stuff in a loop!
		while (true)
		{
			// Update current robot state
			curRound = Clock.getRoundNum();
		
			try {
				if (myType == RobotType.HQ)
					hqCode();
				else if (myType == RobotType.SOLDIER)
					soldierCode();

				// End turn
				rc.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private void hqCode()
	{
		if (rc.isActive())
		{
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir))
				rc.spawn(dir);
		}
	}
	
	private void soldierCode()
	{
		if (rc.isActive()) {
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
