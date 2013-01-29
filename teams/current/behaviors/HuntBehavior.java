package current.behaviors;

import current.SoldierRobot;
import battlecode.common.*;

//TODO: Sense if encampment square occupied early if we have vision - need to add curTarget, if(curTarget != null)...
//		- much like artillery hunting...

// Hunt down enemy encampments, hopefully without engaging the enemy's
// main force
public class HuntBehavior extends Behavior {

	private MapLocation[] backPath;
	private int pathIdx;
	
	private MapLocation[] targets;
	private int targetIdx;
	
	// Have we finished our back door path?
	private boolean backdoored;
	
	
	HuntBehavior(SoldierRobot r) {
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
		if(!backdoored){
			if(r.util.getDistanceBetween(r.curLoc, backPath[pathIdx]) < 3){
				if(pathIdx < backPath.length - 1){
					pathIdx++;
				} else {
					// Done with our waypoints, hunt
					initTargets();
					backdoored = true;
					return;
				}
			}
			r.nav.tunnelTo(backPath[pathIdx]); // ## may want to be simpleNav, or clear the way for backdoor
			
		} else {
			// ## initial version; might need better target selection (modify get n closest from util?)
			// ## getFirstTargets(), getSecondTargets() - try multiple sets before charging
			// 		- or just put the second set on the end of the target list?
			
			// ## for now just move to them, if combat doesn't engage they're empty;
			// probably won't sense them early anyway
			// ## @V Update sensing once we use vision
			if(targetIdx < targets.length){ // Check this first, could be empty
				if(r.curLoc.distanceSquaredTo(targets[targetIdx]) < 14){
						targetIdx++;
				} else {
					r.nav.tunnelTo(backPath[pathIdx]); // ## simpleNav
				}
			} else {
				// Tried all the viable encampment squares
				r.setBehavior(r.travelBehavior);
				r.travelBehavior.destination = r.eHQ;
			}
		}
	}
	
	// ## Copied from backdoor; should probably do the same thing both places for travel efficiency
	public void initPath(){
		backPath = new MapLocation[2];
		
		//## simple version first
		
		Direction eHQDir = r.HQ.directionTo(r.eHQ);
		int dist = r.util.getDistanceBetween(r.HQ, r.eHQ)*3/4; //##?
		
		backPath[0] = restrictToMap(r.HQ.add(eHQDir.rotateRight(), dist));
		backPath[1] = restrictToMap(backPath[0].add(eHQDir, dist));
	}
	private MapLocation restrictToMap(MapLocation loc){
		if(loc.x < 0) loc = new MapLocation(0, loc.y);
		else if(loc.x > r.mapw - 1) loc = new MapLocation(r.mapw - 1, loc.y);
		if(loc.y < 0) loc = new MapLocation(loc.x, 0);
		else if(loc.x > r.maph - 1) loc = new MapLocation(loc.x, r.maph - 1);
		return loc;
	}
	
	private void initTargets() throws GameActionException{
		int dist = r.util.getDistanceBetween(r.curLoc, r.eHQ);
		targets = r.rc.senseEncampmentSquares(r.curLoc.add(r.curLoc.directionTo(r.eHQ), dist/2), dist*dist/4, Team.NEUTRAL);
	}

}
