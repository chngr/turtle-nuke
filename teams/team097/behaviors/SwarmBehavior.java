package team097.behaviors;

import team097.*;
import battlecode.common.*;

// Artillery detection:
// We don't need to notify when locations are checked, when one
// robot can see it all the others will have a chance to sense
// before that robot would move away
// ## New robots won't know the team097 progress, and progress is lost with
// the original swarm; HQ sensing through them after all? then no destroyed msg !! @*

// ## Also if the lone sensor is destroyed immediately;
// probably not critical

//## when we get to the end of possible locs go back to normal swarm

//## send destroyed message where we detect destroyed (not in clearArt...)

// !! ## How does the cycling possible locations interact with combat? still need to do it, how to organise?
// call combat.fight() [combat.fightInDir(dir)] from here and switch to swarm mode when we recieve artillery detected?
// merge (i.e. copy and don't call original) combat behavior into swarm? but the stalemate is useful for rush...

public class SwarmBehavior extends Behavior {

	// Destination location
	public MapLocation target;

	
	//##shield loc?
	
	public SwarmBehavior(SoldierRobot r){
		super(r);
	}
	
	public void run() throws GameActionException{
		// Only hunt the artillery if somewhat nearby; don't want to mess up rallying
		//##? 4*artilleryRadSquared, max danger zone - will this be a problem on small maps? !! almost; splash
		if(r.detector.artilleryDetected && r.curLoc.distanceSquaredTo(r.detector.detectionLoc) < 256){
			r.nav.tunnelTo(r.detector.getTargetLocation()); //## simpleNav?
		} else {
			r.nav.tunnelTo(target);
		}
	}
	
	public void checkBehaviorChange(){
		if(r.util.senseDanger()){
			r.setBehavior(r.combatBehavior);
		}
	}
	
	private boolean subscribed; //Subscribed to the swarm space?
	public void init(){
		r.detector.turnOn();
		if(!subscribed){
			r.subscribe(Communicator.SWARM_SPACE);
			subscribed = true;
		}
	}
}
