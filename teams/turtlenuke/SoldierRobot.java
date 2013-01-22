package turtlenuke;

import battlecode.common.*;

// FortifyBehavior notes:
// - We will probably eventually want to add some way of
//   moving through our own fortification barriers
// - Some way of communicating breach?
// - Minesweep neutrals first? Minesweep behavior?

public class SoldierRobot extends BaseRobot {
	
  //@@ Will belong to FortifyBehavior
  private MapLocation fortCenter;
  private Direction[] fortDirs = null; // ##? null: all directions; only N/E/S/W valid, no repeats
  //##? replace with boolean[] = {N?,E?,S?,W?}
  //##? instead just starting pos and wallnum/length?
  private int fortRadius = 2; // (square) radius of mine wall; must be even //##add 1 otherwise?
  // Could have a density variable, i.e. spacing on the barrier

  
  private Direction dirToCamp;
  private Robot defuser;
  
	
  SoldierRobot(RobotController rc){
    super(rc);
    fortCenter = rc.senseHQLocation();
  }

  public void run() throws GameActionException{
    
    fortify();
    //jam();

  }
  
  //@@ Will be FortifyBehavior.run()
  //## might want parameterized versions with 
  private void fortify() throws GameActionException{
	  //## check whether moving to seeking, building, or defending[engaged,camping]
	  // Should also eventually have reserve mode, for waiting to replace lost defender
	  
	  // set home location when at fortification?
	  
	  //## incomplete, need to account for fortifyDirections
	  if(util.getDistanceBetween(rc.getLocation(), fortCenter) != fortRadius){ 
		  // move to fortification barrier
	  }
	  else {
		  
	  }
	  
	  //## building
	  Direction currentFortifyDir; //## determine this
	  
	  
	  
	  //## defending
	  if(defuser != null) {	 // Engaged
		  if(rc.senseRobotInfo(defuser).roundsUntilAttackIdle == 0){ //## correct? also, what if the robot is dead?
			  defuser = null;
			  if(rc.canMove(dirToCamp)){
				  rc.move(dirToCamp);
				  dirToCamp = null;
			  } //## else, what? shouldn't happen normally, mine push might fit well
		  }
	  }
	  else { 				 // Camping
		  RobotInfo enInfo;
		  Direction defuserDir;
		  for(Robot enemy : rc.senseNearbyGameObjects(Robot.class, 8, enemyTeam)){ //5x5 grid
			  enInfo = rc.senseRobotInfo(enemy);
			  if(enInfo.roundsUntilAttackIdle != 0){ //## does this work?
				  defuserDir = rc.getLocation().directionTo(enInfo.location); 
				  if(rc.canMove(defuserDir)){ //##not good enough; pick direction better
					  defuser = enemy;
					  rc.move(defuserDir);
					  dirToCamp = defuserDir.opposite();
					  break;
				  }
			  }
		  }
	  }
  }
  
  // Could generalize to any construction with sectionDeltas and lists of sections
  
  // Returns the corner of the fortification corresponding to a direction (counterclockwise)
  private MapLocation fortCorner(Direction d){
	  return fortCenter.add(d.rotateLeft(), fortRadius);
  }

  
//  private MapLocation firstRequiredMine; //## first fort corner
//  private MapLocation requiredMineLoc; //## init with above?
//  private MapLocation nextRequiredMine(){ //##null if done placing
//	  do {
//		  if(rc.senseMineLocations(requiredMineLoc, 1, myTeam).length != 5){ // Check if fully mined
//			  return requiredMineLoc;
//		  }
//		  //## check for change of wall
//		  if(mineLocIdx % fortRadius == 0); //## ; may not be 0
//		  delta = mineDeltas[curWall][mineLocIdx % 2];
//		  requiredMineLoc = requiredMineLoc.add(delta[0], delta[1]);
//		  mineLocIdx++;
//	  } while(!requiredMineLoc.equals(firstRequiredMine)); //##?
//	  return null;
//  }
  
  private void defend(){
	  
  }
  
  private void build(){
	  
  }
  
  
  
  
  //## alt method
  private WallSegment[] buildQueue; //## at top
  
  //## need to initialize curWall when setting buildQueue
  private MapLocation getBuildSpot(){
	  while(!needsMine(curWall.curLoc)){
		  nextWallLoc();
	  }
	  return curWall.curLoc;
  }

  
  private int queueIdx;
  private WallSegment curWall;
  
  //## need to also call when arrived at a blocked wallloc
  private boolean nextWallLoc(){
	  if(curWall.nextLoc()){
		  return true;
	  }
	  if(queueIdx < buildQueue.length){
		  curWall = buildQueue[++queueIdx];
		  return true;
	  }
	  return false;
  }

  
  // For now, just check whether it needs mines, and let
  // the robot detect if there is another robot mining it
  // when it gets there, since it needs to do this anyway
  private boolean needsMine(MapLocation loc){
	  return (rc.senseMineLocations(loc, 1, myTeam).length != 5);
  }
  //##replace w/ fort
//  private class WallSegment{
//	  //## camp locs
//	  public MapLocation start;
//	  public Direction dir;
//	  public int length;
//	  WallSegment(MapLocation s, Direction d, int l){
//		  start = s; dir = d; length = l;
//	  }
//  }
  
  private static class WallSegment {
	  
	  // mineDeltas[wall direction][in|out][x|y]
	  private static int[][][] mineDeltas = {
		  {{2,1}, {2,-1}}, // North
		  {{-1,2}, {1,2}}, // East
		  {{-2,-1}, {-2,1}}, // South
		  {{1,-2}, {-1,-2}}  // West
	  };
	  
	  //## camp locs
	  public MapLocation start; //## no longer correct; start at end of previous
	  public MapLocation curLoc;
	  public int dirOrd;
	  public int length;
	  
	  private int curIdx = 0;
	  
	  WallSegment(MapLocation s, int dOrd, int l){
		  curLoc = start = s; dirOrd = dOrd; length = l;
	  }
	  
	  public boolean nextLoc(){
		  if(curIdx < length) { //## off by one?
			  int[] deltas = mineDeltas[dirOrd][curIdx++ % 2]; // Could generalize to % appropriate deltas.length
			  curLoc = curLoc.add(deltas[0],deltas[1]);
			  return true;
		  }
		  return false;
	  }
  }
  
}
