package team097.behaviors;

import team097.*;
import battlecode.common.*;

//TODO: @1 Handle mines without defusion (even just request research)

public class CombatBehavior extends Behavior {

	// Turns before breaking stalemate, if we outnumber them locally;
	// If there are hostile mines, start defusing them, otherwise
	// we can just decrease the cost of far enemies
	public int stalemateTimeout = 7; //?
	
	public int allyDelta = 1; // How many more allies we need to have before breaking a stalemate
		
	private MapLocation prevLoc; // ## should this be in Base/SoldierRobot?
	private int turnsImmobile = 0;
	
	private boolean nukeRush;
	private boolean artilleryHunting;
	
	// Enemies and allies with 14 squares;
	// reused for efficiency, but might want to
	// have some radii different - be careful doing this
	private Robot[] enemies14; // Initialized in check behavior change
	private Robot[] allies14; // Initialized only if stalemate, in outnumber check
	
	
	public CombatBehavior(SoldierRobot r){
		super(r);
	}
	
	public void setDefaultParams(){
		stalemateTimeout = 7;
		allyDelta = 1;
		r.combat.enemyHQVal = 20;
		artilleryHunting = false;
	}
	// ## @Values
	public void setNukeRush(){
		stalemateTimeout = 3;
		allyDelta = -2;
		r.combat.enemyHQVal = 80;
		nukeRush = true;
	}
	// ## !! @Values
	public void setArtilleryHunt(){
		stalemateTimeout = 4;
		allyDelta = 0;
		artilleryHunting = true;
	}
	
	public void checkBehaviorChange(){
		enemies14 = r.rc.senseNearbyGameObjects(Robot.class, 14, r.enemyTeam);
		if(enemies14.length == 0) r.setBehavior(r.prevBehavior);
	}
	
	
	public void run() throws GameActionException{
		//## see swarmbehavior
		if(r.detector.artilleryDetected && r.curLoc.distanceSquaredTo(r.detector.detectionLoc) < 256){
			if(!artilleryHunting) setArtilleryHunt();
			fight(r.curLoc.directionTo(r.detector.getTargetLocation()));
		} else {
			if(artilleryHunting) setDefaultParams();
			fight(Direction.OMNI);
		}
	}
	
	private void fight(Direction d) throws GameActionException{
		if(r.curLoc.equals(prevLoc)){
			turnsImmobile++;
		} else {
			turnsImmobile = 0;
		}
		
		if(turnsImmobile >= stalemateTimeout){
			allies14 = r.rc.senseNearbyGameObjects(Robot.class, 14, r.myTeam);
			if(allies14.length - enemies14.length >= allyDelta){ // ## This is biased towards finding allies; problem?
				if(r.rc.senseNonAlliedMineLocations(r.curLoc, 14).length > 0){
					breakMines();
				} else {
					r.combat.enemyCost = 3;
					r.combat.farCostDiscount = 0.1; // ## should have proper way of doing this ##val?
					r.combat.fightInDir(d);
					r.combat.enemyCost = 6;
					r.combat.farCostDiscount = 0.9; // ## default
				}
			}
			turnsImmobile = 0;
		} else {
			r.combat.fightInDir(d);
		}
		
		prevLoc = r.curLoc;
	}
	
	
	public void breakMines() throws GameActionException{
		if(!allyDefusing()){
			if(r.rc.hasUpgrade(Upgrade.DEFUSION)){
				if(!engagingEnemy() || artilleryHunting){ //## or nukeRush?
					leapFrog();
				}
			} else {
				// ## @1
			}
		}
	}

	
	// First attempt at leap frog defusion with the defusion upgrade.
	// Find a hostile mine towards an enemy, and defuse it.
	// Should only be called if there *are* mines to defuse, otherwise
	// potentially expensive.
	// WILL THROW EXCEPTION IF WE DON'T HAVE DEFUSION
	// Not sure if this should be here, other behaviors might want it
	// or something similar.
	private void leapFrog() throws GameActionException{
		RobotInfo ri;
		MapLocation[] mines;
		for(Robot e : enemies14){
			ri = r.rc.senseRobotInfo(e);
			mines = r.rc.senseNonAlliedMineLocations( r.curLoc.add(r.curLoc.directionTo(ri.location)), 2);
			if(mines.length > 0){
				r.rc.defuseMine(mines[0]);
				return;
			}
		}
	}
	
	// Whether there is a enemy within immediate attack range
	// i.e. a 5x5 square
	private boolean engagingEnemy(){
		return r.engagedEnemies.length > 0;
	}
	
	private boolean allyDefusing() throws GameActionException{
		RobotInfo ri;
		for(Robot a : allies14){
			ri = r.rc.senseRobotInfo(a);
			if(ri.roundsUntilAttackIdle  != 0) return true;
		}
		return false;
	}
}
