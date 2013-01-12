package blank;

import battlecode.common.*;

public class SimpleNavigator {

	private RobotController r;

	private int closestAttainedDist;
	private int closestAttainedDistRoundNum;
	private int mineCost = -99999999;
	private MapLocation curLoc;
	private Direction curHeadingDir;

	SimpleNavigator(RobotController robot){
		this.r = robot;
		closestAttainedDist = 99999999;
	}

	public int getDistanceBetween(MapLocation loc1, MapLocation loc2){
		return Math.max(Math.abs(loc1.x - loc2.x), Math.abs(loc1.y - loc2.y));
	}
	public boolean moveTo(MapLocation goal) throws GameActionException {
		curLoc = r.getLocation();
		if (curLoc.equals(goal))
		{
			closestAttainedDist = 99999999;
			return false;
		}

		Direction optimalDir = curLoc.directionTo(goal);
    return forceStep(optimalDir);
	}

	public boolean takeSafeRelevantStep(Direction optimalDir, Direction avoidDir) throws GameActionException
	{
		Direction dir1 = optimalDir;
		Direction dir2 = optimalDir.rotateRight();

		for (int i = 0; i < 3; i++) {
			if (dir1 != avoidDir){
				if (takeSafeStep(dir1) == 0)
				{
					curHeadingDir = dir1;
					return true;
				}
			}
			if (dir2 != avoidDir){
				if (takeSafeStep(dir2) == 0)
				{
					curHeadingDir = dir2;
					return true;
				}
			}
			dir1 = dir1.rotateLeft();
			dir2 = dir2.rotateRight();
		}
		return false;
	}

	public boolean forceStep(Direction dir) throws GameActionException {
		int safeStepRes = takeSafeStep(dir);
		if (safeStepRes == 0)
			return true;
		else if (safeStepRes == 1){
			if (forceStepOntoMine(dir, true))
				return true;
		}
		return false;
	}

	public int takeSafeStep(Direction dir) throws GameActionException {
		MapLocation dest = curLoc.add(dir);
		Team mine = r.senseMine(dest);
		if (r.canMove(dir) && (mine == r.getTeam() || mine == null))
		{
			r.move(dir);
			return 0;
		}
		else if (mine == r.getTeam().opponent() || mine == Team.NEUTRAL)
		{
			return 1;	// there is a mine (status code 0x01)
		}
		else
		{
			return 2;	// there is an obstacle (enemy/friendly robot)
		}
	}

	public boolean forceStepOntoMine(Direction dir, boolean doDefuse) throws GameActionException {
		if (r.canMove(dir)){
			if (doDefuse)
				r.defuseMine(curLoc.add(dir));
			r.move(dir);
			return true;
		} else
			return false;
	}
}
