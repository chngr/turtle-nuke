package turtlenuke;

import battlecode.common.*;

// FortifyBehavior notes:
// - We will probably eventually want to add some way of
//   moving through our own fortification barriers
// - Some way of communicating breach?
// - Minesweep neutrals first? Minesweep behavior?

// CURRENT ISSUE: Getting stuck in build forever if near the map edge
// Would get stuck in support, but always defend for now anyway

public class SoldierRobot extends BaseRobot {
	
  
  private enum FortState { BUILDING, SEEKING_CAMP, DEFENDING, SUPPORT };
  private FortState fortState;
  
  // Could have a density variable, i.e. spacing on the barrier
  
	
  SoldierRobot(RobotController rc){
    super(rc);
    MapLocation HQ = rc.senseHQLocation();
    buildFort(HQ, 2, HQ.directionTo(rc.senseEnemyHQLocation()), 4);
  }

  public void run() throws GameActionException{
    fortify();
    //jam();
    rc.yield();
  }
  
  //@@ Will be FortifyBehavior.run()
  private void fortify() throws GameActionException {
	  rc.setIndicatorString(0, fortState.toString()); //DEBUG
	  // set home location when at fortification?
	  if(rc.isActive()){  
		  switch(fortState) {
		  case BUILDING:
			  build();
			  break;
		  case SEEKING_CAMP:
			  seekCamp();
			  break;
		  case DEFENDING:
			  defend();
		  case SUPPORT:
			  support();
			  break;
		  }
	  }
  }
  
  // ------ SET FORTIFICATION GOAL ------
  
  private void buildFort(MapLocation center, int radius, Direction startEdge, int numDirs){
	  buildQueue = new WallSegment[numDirs];
	  
	  Direction d = startEdge;
	  MapLocation corner;
	  for(int i=0; i < numDirs; i++){
		  corner = fortCorner(center, d, radius);
		  buildQueue[i] = new WallSegment(corner, d, radius);
		  d = d.rotateRight().rotateRight();
	  }
	  initBuild();
  }
  //Returns the corner of the fortification corresponding to a direction (counterclockwise)
  private MapLocation fortCorner(MapLocation center, Direction d, int radius){
	   return center.add(d.rotateLeft(), radius);
  }
  
  
  private void buildWall(MapLocation leftEdge, Direction dir, int length){
	  buildQueue = new WallSegment[1];
	  buildQueue[0] = new WallSegment(leftEdge, dir, length);
	  initBuild();
  }
  
  private void initBuild(){
	  resetProgress();
	  curWall = buildQueue[0];
	  fortState = FortState.BUILDING;
  }
  
  private void resetProgress(){
	  //## all important state vars
  }
  
  
  // ------ FORTIFICATION LOGIC ------
  
  private MapLocation buildTarget;
  private void build() throws GameActionException{
	  if(rc.getLocation().equals(buildTarget)){
		  rc.layMine();
		  buildTarget = getBuildSpot();
	  }	  
	  else {
		  buildTarget = getBuildSpot(); // improvement: don't do this every time
		  if(buildTarget != null){
			  nav.tunnelTo(buildTarget);
		  } else {
			  fortState = FortState.SEEKING_CAMP;
			  queueIdx = 0; // Reset these, as seekCamp uses them; I guess?
			  curWall = buildQueue[0];
			  seekCamp();
	      }
	  }
  }
  
  private MapLocation campTarget;
  private void seekCamp() throws GameActionException{
	  if(rc.getLocation().equals(campTarget)){
		  fortState = FortState.DEFENDING;
		  defend();
	  }	  
	  else {
		  campTarget = getCamp(); // definitely silly to do this every time
		  if(campTarget != null){
			  nav.tunnelTo(campTarget);
		  } else {
			  // All camps are (hopefully) defended; enter support mode
			  // and replace fallen defenders (rebuild walls?)
//			  fortState = FortState.SUPPORT;
//			  support();
			  fortState = FortState.DEFENDING;
			  defend(); // ##have no support mode, defending for now
	      }
	  }
  }
  
  private Direction dirToCamp;
  private Robot defuser;
  private void defend() throws GameActionException{
	  if(defuser != null) {	 // Engaged
		  if(!rc.canSenseObject(defuser) || rc.senseRobotInfo(defuser).roundsUntilAttackIdle == 0){ //## correct? also, what if the robot is dead?
			  defuser = null;
			  if(rc.canMove(dirToCamp)){
				  rc.move(dirToCamp);
				  dirToCamp = null;
			  } //## else, what? shouldn't happen normally, mine push might fit well
			    // doing nothing will leave it camping outside the mines
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
  
  private void support(){
	  //## TODO
  }
  
  
  private WallSegment[] buildQueue; //## at top
  private WallSegment curWall;
  private int queueIdx;
  
  private static class WallSegment {
	  
	  // mineDeltas[wall direction][in|out][x|y]
	  private static int[][][] mineDeltas = {
		  {{2,1}, {2,-1}}, // North
		  {{-1,2}, {1,2}}, // East
		  {{-2,-1}, {-2,1}}, // South
		  {{1,-2}, {-1,-2}}  // West
	  };
	  
	  public MapLocation start;
	  public MapLocation curMine;
	  public MapLocation curCamp;
	  public Direction wallRight; // Parallel to the wall, your right if you're inside it
	  public int dirOrd;
	  public int length;
	  
	  private int mineIdx = 0;
	  private int campIdx = 0;
	  
	  WallSegment(MapLocation s, Direction d, int l){
		  curMine = start = s;
		  wallRight = d.rotateRight().rotateRight();
		  curCamp = s.add(wallRight.rotateRight()); //Interior edge
		  dirOrd = dirIdx(d);
		  length = l;
	  }
	  
	  public boolean nextMine(){
		  if(mineIdx < length) {
			  int[] deltas = mineDeltas[dirOrd][mineIdx++ % 2]; // Could generalize to % appropriate deltas.length
			  curMine = curMine.add(deltas[0],deltas[1]);
			  return true;
		  }
		  return false;
	  }
	  
	  public boolean nextCamp(){
		  if(campIdx < 2*length - 1){ //##?
			  campIdx++;
			  curMine = curMine.add(wallRight);
			  return true;
		  }
		  return false;
	  }
	  
	  private int dirIdx(Direction d){
		  return d.ordinal() / 2; //## probably wrong
	  }
  }
  
  private MapLocation getBuildSpot() throws GameActionException{
	  while(!needsMine(curWall.curMine) || occupied(curWall.curMine)){
		  if(!nextWallLoc()) return null; // Updates curWall/curMine; returns null when done building
	  }
	  return curWall.curMine;
  }
  private boolean needsMine(MapLocation loc){
	  return (rc.senseMineLocations(loc, 1, myTeam).length != 5);
  }
  private boolean occupied(MapLocation loc) throws GameActionException{
	  return rc.canSenseSquare(loc) && (rc.senseObjectAtLocation(loc) != null);
  }
  private boolean nextWallLoc(){
	  if(curWall.nextMine()){
		  return true;
	  }
	  if(queueIdx < buildQueue.length-1){
		  curWall = buildQueue[++queueIdx];
		  return true;
	  }
	  return false;
  }
 
  // Basically a duplicate of build spot logic; not sure how to combine
  private MapLocation getCamp() throws GameActionException{
	  while(occupied(curWall.curCamp)){
		  if(!nextCampLoc()) return null; // Updates curWall/curLoc; returns null when done building
	  }
	  return curWall.curCamp;
  }
  private boolean nextCampLoc(){
	  if(curWall.nextCamp()){
		  return true;
	  }
	  if(queueIdx < buildQueue.length-1){ //## using same vars as build; I guess?
		  curWall = buildQueue[++queueIdx];
		  return true;
	  }
	  return false;
  }
  
}
