package current.behaviors;

import current.SoldierRobot;
import battlecode.common.*;

// ## This could just be travel with waypoints

public class BackdoorBehavior extends Behavior {

	private MapLocation[] backPath;
	private int pathIdx;
	
	BackdoorBehavior(SoldierRobot r) {
		super(r);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void checkBehaviorChange() {
		if(r.util.senseDanger()){
			r.setBehavior(r.combatBehavior);
		}
	}

	@Override
	public void run() throws GameActionException {
		if(r.util.getDistanceBetween(r.curLoc, backPath[pathIdx]) < 3){
			if(pathIdx < backPath.length - 1){
				pathIdx++;
			} else {
				// Done with our waypoints, attack the enemy HQ
				r.setBehavior(r.travelBehavior);
				r.travelBehavior.destination = r.eHQ;
				return;
			}
		}
		r.nav.tunnelTo(backPath[pathIdx]); // ## this will want simpleNav; possibly hunt too, but it could clear the backpath
	}
	
	public void initPath(){
		backPath = new MapLocation[2];
		
		//## simple version first
		
		Direction eHQDir = r.HQ.directionTo(r.eHQ);
		int dist = r.util.getDistanceBetween(r.HQ, r.eHQ)*3/4; //##?
		
		backPath[0] = restrictToMap(r.HQ.add(eHQDir.rotateRight(), dist));
		backPath[1] = restrictToMap(backPath[0].add(eHQDir, dist));
		
//		if(eHQDir.isDiagonal()){
//			
//		} else {
//			
//		}
		
//		int dx = r.eHQ.x - r.HQ.x;
//		int dy = r.eHQ.y - r.HQ.y;
//		int pathD1;
//		if(Math.abs(dx) < Math.abs(dy)){
//			pathD1 = eHQ.x + (dx > 0)
//			backPath[0] = new MapLoc
//		}
	}
	private MapLocation restrictToMap(MapLocation loc){
		if(loc.x < 0) loc = new MapLocation(0, loc.y);
		else if(loc.x > r.mapw - 1) loc = new MapLocation(r.mapw - 1, loc.y);
		if(loc.y < 0) loc = new MapLocation(loc.x, 0);
		else if(loc.x > r.maph - 1) loc = new MapLocation(loc.x, r.maph - 1);
		return loc;
	}

}
