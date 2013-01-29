package current;

import current.strategies.*;
import battlecode.common.*;

//TODO: Strategy selection, if we can get any others satisfactory; is immediate rush good on short maps?

public class HQRobot extends BaseRobot {

  public int spawnTimeLeft;
  public int curSpawnDelay = 10; // ## change when upgrade suppliers; can we just sense this?
  
  // Record power so we can debounce power dependent choices
  // by strategies (there are temporary fluctuations from encampment capture)
  private double prevPower = 200;
  private double power = 200;
  
  public Strategy currentStrategy;
  
  // Strategies
  public TurtleNuke turtleNuke;
  public Rush rush;
  public Swarm swarm;
  
  
  // Map variables
  public int effectiveHQDist = 0; // Estimate of how many turns to travel between HQs
  
  // Encampment variables
  MapLocation[] encampmentLocs;
  public int[] encampmentCounts; // Number of encampments of each type, indexed by ordinal
  public int builtEncampments = 0;
  
  // Add encampments to queuedEncampments as we request them,
  // so we don't try to build them again. Remove them when they're
  // done, and eventually timeout if they're not built.
  // ## might want to store queued types, but more complicated/expensive;
  // ## strategies can probably tell what their next planned types are?
  public int queuedEncampments = 0;
  private int encampmentTimeout = 150;
  private int encampmentTimer = 0;
  

  HQRobot(RobotController rc) throws GameActionException{
    super(rc);
    
    turtleNuke = new TurtleNuke(this);
    rush = new Rush(this);
    swarm = new Swarm(this);
    
    encampmentCounts = new int[7];
    
    evaluateMap();
    currentStrategy = pickStrategy();
    currentStrategy.begin();
  }

  public void run() throws GameActionException {
	  super.run();
	  updatePower();
	  updateEncampments();
	  currentStrategy.checkStrategyChange();
	  currentStrategy.run();
  }
  
  
  private void evaluateMap(){
	  //## better estimate would be nice, but seems to work fairly well in practice
	  MapLocation loc = HQ;
	  
	  while(util.getDistanceBetween(loc, eHQ) > 5){
		  effectiveHQDist += rc.senseNonAlliedMineLocations(loc, 8).length + 5;
		  loc = loc.add(loc.directionTo(eHQ), 5);
	  }
	  effectiveHQDist = Math.min(effectiveHQDist, 200); //It is guaranteed that it's possible to reach the enemy HQ in 200 rounds
  }
  
  
  private Strategy pickStrategy(){
	  //##
	  return swarm;
  }
  
  
  // Generalize to allow choosing preferred direction
  public boolean spawn() throws GameActionException{
      Direction dir = curLoc.directionTo(eHQ);

      // Need to do this first so the termination check works
      if(rc.canMove(dir) && !util.senseHostileMine(curLoc.add(dir))){
    	  spawnInDir(dir);
	      return true;
      }
      // Spawn as close to the desired direction as possible
	  Direction dirLeft = dir;
	  Direction dirRight = dir;	
	  do {
		dirLeft = dirLeft.rotateLeft();
	    if (rc.canMove(dirLeft) && !util.senseHostileMine(curLoc.add(dirLeft))) {
	      spawnInDir(dirLeft);
	      return true;
	    }
	    
	    dirRight = dirRight.rotateRight(); 
	    if (rc.canMove(dirRight)  && !util.senseHostileMine(curLoc.add(dirRight))) {
	      spawnInDir(dirRight);
	      return true;
	    }
	        
	  } while (dirRight != dirLeft) ;
	  
	  return false;
  }

  private void spawnInDir(Direction dir) throws GameActionException{
    rc.spawn(dir);
    spawnTimeLeft = curSpawnDelay;
  }

  
  
  //##
  @Override
  protected int processMessage(char[] data, int startIdx) throws GameActionException {
	  switch(data[startIdx]){
		case 0:
			// HACK: this seems to get called ~40 times/round (when we have no data?), so break the message read
			// loop when it happens. Doesn't appear to mess with proper messages.
			return data.length;
			
		// Artillery detected; we need to tell new swarmers
		// @@ centerLoc.x | centerLoc.y
		case 6:
			detector.detectedArtilleryAt(new MapLocation(data[startIdx+1],data[startIdx+2]));
			break;

			
		default:
			System.out.println("Unrecognised message"); //DEBUG
		}
		return 3; // Minimum message length
  }
  
  private static final int FORTIFY_MSG = 2;
  public char[] buildFortifyMessage(){
	  return new char[] {FORTIFY_MSG,0,0};
  }
  
  private static final int RUSH_MSG = 3;
  public char[] buildRushMessage(boolean nukeRush){
	  return new char[] {RUSH_MSG, (char) (nukeRush ? 1 : 0), 0};
  }
  
  private static final int SWARM_MSG = 4;
  public char[] buildSwarmMessage(MapLocation target){
	  return new char[] {SWARM_MSG, (char) target.x, (char) target.y};
  }
  
  private static final int CAPTURE_MSG = 5;
  public char[] buildCaptureMessage(int encTypeOrdinal){
	  queuedEncampments++;
	  encampmentTimer = encampmentTimeout;
	  return new char[] {CAPTURE_MSG,(char) encTypeOrdinal,0};
  }
  
  public void sendInitializeMessage(char[] data) throws GameActionException{
	  //System.out.println("Sending:"+Integer.toString((int)data[0])); //DEBUG
	  comm.putSticky(Communicator.INIT_SPACE, data);
  }
  
  
  public void setStrategy(Strategy s) throws GameActionException{
	  currentStrategy = s;
	  s.begin();
  }
  
  
  private void updateEncampments() throws GameActionException{
	  if(curRound % 10 == 0){ //##
		  encampmentLocs = rc.senseAlliedEncampmentSquares();
		  if(encampmentLocs.length > builtEncampments){
			  queuedEncampments -= encampmentLocs.length - builtEncampments;
			  encampmentTimer = encampmentTimeout;
		  }
		  builtEncampments = encampmentLocs.length;
		  encampmentCounts = new int[7];
		  RobotInfo ri;
		  for(MapLocation eloc : encampmentLocs){
			  ri = rc.senseRobotInfo((Robot) rc.senseObjectAtLocation(eloc));
			  encampmentCounts[ri.type.ordinal()]++;
		  }
	  }
	  if(encampmentTimer > 0) encampmentTimer--;
	  if(encampmentTimer == 0) queuedEncampments = 0; // Clear queued encampments
	  
  }
  
  // Power checking
  private void updatePower(){
	  prevPower = power;
	  power = rc.getTeamPower();
  }
  
  public boolean outOfPower(){
	  // HQ goes first, so this checks for less than 2/(decay rate) = 2.5 power at the end of the previous turn
	  double threshold = powerCapacity() + 2;
	  // If we were out of power, check that our net regen rate is less than 2
	  return prevPower < threshold && power - prevPower < 2;
  }
  // Was 16 at end of the previous turn
  public boolean lowPower(){
	  double threshold = powerCapacity() + 12;
	  return prevPower < threshold && power - prevPower < 3;
  }
  
  public double powerCapacity(){
	  return 40 + 10*encampmentCounts[RobotType.GENERATOR.ordinal()];
  }
}
