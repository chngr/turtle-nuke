package phalanx;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {
  
  private int waiting = 0;
  private static final int WAIT_TIMEOUT = 10;
  private static final int WAVE_SIZE = 50;
  
  private MapLocation eHQ;
  private int rushDist = 10;
  
  // Phalanx vars
  private MapLocation phlxCenter;
  private Direction phlxFront;
  private Direction mySide;
  private boolean isLeft;
//  private Direction phlxRight;
//  private Direction phlxLeft;
  
  private enum PhalanxState { SEEKING, FORMING, IDLE, ADVANCING };
  private PhalanxState phlxState;

	
  SoldierRobot(RobotController rc) throws GameActionException{
    super(rc);
    initPhalanx();
    eHQ = rc.senseEnemyHQLocation(); 
  }

  public void run() throws GameActionException{ 
	// Early scouts clear a path through the mines
	if(curRound % WAVE_SIZE == 0 && phlxState == PhalanxState.IDLE) phlxState = PhalanxState.ADVANCING; 
	
    if (rc.isActive()) {
    	if(util.getDistanceBetween(rc.getLocation(), eHQ) < rushDist){
    		tunnelTo(eHQ);
    	} else {
	    	switch (phlxState) {
		    	case SEEKING:
		    		seek();
		    		break;
		    	case FORMING:
		    		form();
		    		break;
		    	case ADVANCING:
		    		advance();
			default:
				break;
	    	}
    	}
    }
    
  }
  
  
  //##width?
  private void initPhalanx(){ // only supports cardinal directions
	  MapLocation HQ = rc.senseHQLocation();
	  MapLocation eHQ = rc.senseEnemyHQLocation();
	  
	  int dx = HQ.x - eHQ.x,
		  dy = HQ.y - eHQ.y;
	  if(Math.abs(dx) < Math.abs(dy)){
		  phlxCenter = new MapLocation(eHQ.x, (3*HQ.y + eHQ.y)/4);
		  phlxFront = dy > 0 ? Direction.NORTH : Direction.SOUTH;
	  } else {
		  phlxCenter = new MapLocation((3*HQ.x + eHQ.x)/4, eHQ.y);
		  phlxFront = dx > 0 ? Direction.WEST : Direction.EAST;
	  }
	  
	// Would need more general balancing for right/left
	  if(id % 7 % 2 == 0){ //##sufficiently random?
		  mySide = phlxFront.rotateRight().rotateRight();
		  isLeft = false;
	  } else {
		  mySide = phlxFront.rotateLeft().rotateLeft();
		  isLeft = true;
	  }
	  
	  phlxState = PhalanxState.SEEKING;
  }
  
  
  // Completely stupid with respect to collisions; would need to fix
  private void seek() throws GameActionException{
	  if(util.getDistanceBetween(rc.getLocation(), phlxCenter) > 1) {
	  	tunnelTo(phlxCenter);
	  } else {
		phlxState = PhalanxState.FORMING;
		form();
	  }
  }

  private void form() throws GameActionException{
	  Direction tryDir = isLeft ? mySide.rotateRight() : mySide.rotateLeft();
	  if(rc.canMove(tryDir)){
		  System.out.println("at dest");
		  if(moveOrDefuse(tryDir)) phlxState = PhalanxState.IDLE; // In the phalanx line
	  }
	  else if(rc.canMove(mySide)) moveOrDefuse(mySide);
  }
  
  //## In util?
  private int[][] dirIdxs = { // From north, clockwise
		  {1,0},{2,0},{2,1},{2,2},{1,2},{0,2},{0,1},{0,0}
  };
  
  // Currently most naive possible advance - ignores enemies,
  // just focuses on advancing as a line
  boolean[][] map;
  private void advance() throws GameActionException{
	  if(rc.canMove(phlxFront)){
		  map = localMap(); // Adjacent allies
		  
		  int fIdx = phlxFront.ordinal();//##?
		  if(  (isAlly(fIdx-1) || (!isAlly(fIdx-2) && !isAlly(fIdx-3)))
			&& (isAlly(fIdx+2) || (!isAlly(fIdx+1) && !isAlly(fIdx+3))) ){
			moveOrDefuse(phlxFront);  
		  }
  	  }
  }
  private boolean isAlly(int dirOrd) {
	  int[] idx = dirIdxs[ ((dirOrd < 0) ? dirOrd+8 : dirOrd) % 8 ]; // Wrapping
	  return map[idx[0]][idx[1]];
  }
  
  private boolean[][] localMap() throws GameActionException{
	  boolean[][] map = new boolean[3][3]; //1:ally 2:enemy
	  Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 2, myTeam);
	  RobotInfo rInfo;
	  int[] idxs;
	  for(Robot r : nearbyRobots){
		  rInfo = rc.senseRobotInfo(r);
		  idxs = locToIndex(rInfo.location);
		  map[idxs[0]][idxs[1]] = true;
	  }
	  
	  return map;
  }
  private int[] locToIndex(MapLocation loc){
	  MapLocation myLoc = rc.getLocation();
	  int[] index = new int[2];
	  index[0] = loc.x - myLoc.x + 1;
	  index[1] = loc.y - myLoc.y + 1;
	  return index;
  }
  
  private void tunnelTo(MapLocation loc) throws GameActionException{
	  if(!loc.equals(rc.getLocation())){ //apparently necessary; why doesn't canMove catch this?
		  Direction dir = rc.getLocation().directionTo(loc);
		  if(rc.canMove(dir)) moveOrDefuse(dir);
		  else if(rc.canMove(dir.rotateRight()))  moveOrDefuse(dir.rotateRight());
		  else if(rc.canMove(dir.rotateLeft()))  moveOrDefuse(dir.rotateLeft());
		  else waiting++;
	  }
	  else waiting = WAIT_TIMEOUT;
  }
  // Returns whether we moved (
  private boolean moveOrDefuse(Direction dir) throws GameActionException{
	  waiting = 0;
	  MapLocation loc = rc.getLocation().add(dir);
	  Team mine = rc.senseMine(loc);
	  if (mine == rc.getTeam() || mine == null){
		  rc.move(dir);
		  return true;
	  }
	  rc.defuseMine(loc);
	  return false;
  }
  
}
