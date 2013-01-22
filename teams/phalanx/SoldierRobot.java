package phalanx;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {
  
  private int waiting = 0;
  private static final int WAIT_TIMEOUT = 10;
  private static final int WAVE_SIZE = 200;
  
  
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
  }

  public void run() throws GameActionException{ 
	// Early scouts clear a path through the mines
	if(curRound % WAVE_SIZE == 0 && phlxState == PhalanxState.IDLE) phlxState = PhalanxState.ADVANCING; 
	
    if (rc.isActive()) {
    	switch (phlxState) {
	    	case SEEKING:
	    		seek();
	    		break;
	    	case FORMING:
	    		form();
	    		break;
	    	case ADVANCING:
	    		advance();
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
	  if(rc.canMove(tryDir)) moveOrDefuse(tryDir);
	  else if(rc.canMove(mySide)) moveOrDefuse(mySide);
  }
  private void advance() throws GameActionException{
	  int[][] map = new int[5][5]; //1:ally 2:enemy
	  Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class, 8);
	  RobotInfo rInfo;
	  int[] idxs;
	  for(Robot r : nearbyRobots){
		  rInfo = rc.senseRobotInfo(r);
		  idxs = locToIndex(rInfo.location);
		  map[idxs[0]][idxs[1]] = (rInfo.team == myTeam) ? 1 : 2;
	  }
	  
	  MapLocation myLoc = rc.getLocation();
	  switch (phlxFront) {
		  case NORTH:
			  break;
			  
		  case EAST:
			  break;
			  
		  case SOUTH:
			  break;
			  
		  case WEST:
			  break;
			  
		  default: System.out.println("Invalid dir");
		  
	  }
  }
  
  
  private int[] locToIndex(MapLocation loc){
	  MapLocation myLoc = rc.getLocation();
	  int[] index = new int[2];
	  index[0] = loc.x - myLoc.x + 2;
	  index[1] = loc.y - myLoc.y + 2;
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
  private void moveOrDefuse(Direction dir) throws GameActionException{
	  waiting = 0;
	  MapLocation loc = rc.getLocation().add(dir);
	  Team mine = rc.senseMine(loc);
	  if (mine == rc.getTeam() || mine == null){
		  rc.move(dir);
	  } else {
		  rc.defuseMine(loc);
	  }
  }
  
}
