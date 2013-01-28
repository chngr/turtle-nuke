package current;

/*
A collection of useful algorithms!
- int genRandFromClosedInterval(min, max)
- int getDistanceBetween(MapLocation1, MapLocation2)
- swap(List, Idx1, Idx2)
- getNearestNRobots( ... )

*/

import battlecode.common.*;

public class Utilities {
	
	private BaseRobot r;
	
	Utilities(BaseRobot robot){
		this.r = robot;
	}
	
	public Direction[] movableDirs = {
			  Direction.NORTH_WEST,
              Direction.NORTH,
              Direction.NORTH_EAST,
              Direction.WEST,
              Direction.EAST,
              Direction.SOUTH_WEST,
              Direction.SOUTH,
              Direction.SOUTH_EAST
    };
	
	// ## Math.random() is not random in battlecode - all robots will be the same
	// ## If we need randomness, we should write our own, or use e.g. 2012 team's
	public int genRandFromClosedInterval(int min, int max){
		return (int)(min + Math.random() * (max - min));
	}
	
	public int getDistanceBetween(MapLocation loc1, MapLocation loc2){
		return Math.max(Math.abs(loc1.x - loc2.x), Math.abs(loc1.y - loc2.y));
	}
	
	
	public <T> void swap(T[] lst, int idx1, int idx2)
	{
		T tmp = lst[idx1];
		lst[idx1] = lst[idx2];
		lst[idx2] = tmp;
	}
	
	public class RobotIdDistPair {
		int id;
		int dist;
	}
	
	public RobotIdDistPair[] getNearestNRobots(int N, MapLocation c, int r2, Team t) throws GameActionException{
		Robot[] robots = r.rc.senseNearbyGameObjects(Robot.class, c, r2, t);
		int totalRobotNum = robots.length;
		RobotIdDistPair[] rdPairs = new RobotIdDistPair[totalRobotNum];
		for (int i = 0; i < totalRobotNum; i++)
		{
			rdPairs[i].id = robots[i].getID();
			rdPairs[i].dist = getDistanceBetween(r.rc.senseLocationOf(robots[i]), c);
		}
		_qsNearestNRobots(rdPairs, N, 0, totalRobotNum - 1);
		return rdPairs;
	}
	
	private void _qsNearestNRobots(RobotIdDistPair[] rdPairs, int N, int idxLeft, int idxRight){
		if (idxRight < idxLeft)
			return;
		int idxPivot = genRandFromClosedInterval(idxLeft, idxRight);
		
		// Partition around idxPivot
		RobotIdDistPair pivotVal = rdPairs[idxPivot];
		swap(rdPairs, idxPivot, idxRight);
		int idxCur = idxLeft;
		for (int i = idxLeft; i < idxRight; i++){
			if (rdPairs[i].dist < pivotVal.dist)
			{
				swap(rdPairs, i, idxCur);
				idxCur++;
			}
		}
		swap(rdPairs, idxCur, idxRight);
		
		// Recurse on relevant sublist
		_qsNearestNRobots(rdPairs, N, idxLeft, idxCur - 1);
		if (idxCur < idxLeft + N)
			_qsNearestNRobots(rdPairs, N - (idxCur - idxLeft) - 1, idxCur + 1, idxRight);
	}
	
	
	public boolean senseHostileMine(MapLocation location) {
		Team mine_team = r.rc.senseMine(location);
		return !(mine_team == null || mine_team == r.myTeam);
	}
	
	
	public boolean senseDanger(){
		return r.rc.senseNearbyGameObjects(Robot.class, 14, r.enemyTeam).length > 0;
	}
	
	
	// Calculate roughly how close a location is to the line between the HQs
	public float calculateCentrality(MapLocation loc){
		int hdx = r.HQ.x - r.eHQ.x, hdy = r.HQ.y - r.eHQ.y,
			ldx = r.HQ.x - loc.x,   ldy = r.HQ.y - loc.y;
		if(hdx*ldx < 0 && hdy*ldy < 0){ // Wrong side of HQ
			return getDistanceBetween(r.HQ,loc);
		}
		return Math.abs(hdx/hdy - ldx/ldy) * getDistanceBetween(r.HQ,loc);
	}
	
	
	public boolean inMap(MapLocation loc){
		return (0 <= loc.x) && (loc.x < r.mapw) && (0 <= loc.y) && (loc.y < r.maph);
	}
	
    public boolean squareFree(MapLocation loc) throws GameActionException{
	    return r.util.inMap(loc) && (r.rc.senseObjectAtLocation(loc) == null) && !senseHostileMine(loc);
    }
	
    
	private Robot HQRobot;
	public Robot getHQ() throws GameActionException{
		if(HQRobot != null) return HQRobot;
		return (HQRobot = (Robot) r.rc.senseObjectAtLocation(r.HQ));
	}
	private int HQID;
	public int getHQID() throws GameActionException{
		if(HQID != 0) return HQID;
		return (HQID = getHQ().getID());
	}
}