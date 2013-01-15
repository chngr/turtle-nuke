package team097;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {

  private MapLocation HQ;
  private MapLocation eHQ;
  
  private MapLocation target;
  
  private int waiting = 0;
  private static final int WAIT_TIMEOUT = 10;
  private static final int WAVE_SIZE = 200;
  
  private boolean captor = false;
  private static final int CAPTOR_PROB = 5; // id % prob = 0
  private static final int SUP_GEN_THRESHOLD = 10; //##arbitrary, untested

	
  SoldierRobot(RobotController rc) throws GameActionException{
    super(rc);
    HQ = rc.senseHQLocation();
	eHQ = rc.senseEnemyHQLocation(); 
	target = new MapLocation((3*HQ.x+eHQ.x)/4, (3*HQ.y+eHQ.y)/4);
	
//	if(rc.getTeamPower() > rc.senseCaptureCost() && (id % CAPTOR_PROB == 0)){ //##experimental; add probability based on dist to eHQ?
//		MapLocation[] hostileEnc = rc.senseEncampmentSquares(rc.getLocation(), HQ.distanceSquaredTo(eHQ)/4, Team.NEUTRAL);
//		if(hostileEnc.length > 0){
//			captor = true;
//			MapLocation[] closerEnc = rc.senseEncampmentSquares(rc.getLocation(), 32, Team.NEUTRAL); //##rad=?
//			if(closerEnc.length > 0) target = closerEnc[id % closerEnc.length];
//			else target = hostileEnc[id % hostileEnc.length];
//		}
//	}
			
  }

  public void run() throws GameActionException{ 
	// Early scouts clear a path through the mines
	if(curRound % WAVE_SIZE == 0 || curRound == 30) target = eHQ; //currently allowing wave trigger to distract captors; good?
	
    if (rc.isActive()) {
    	
//      int numNearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 24, this.enemyTeam).length;
//      if(numNearbyEnemies > 0){
//        this.combat.fight();
//      }
//      else {
        if(captor){
        	if(rc.getLocation().equals(target)){
        		double pow = rc.getTeamPower(),
        				cc = rc.senseCaptureCost();
        		if(pow > cc){
        			rc.captureEncampment(pow > cc + SUP_GEN_THRESHOLD ? RobotType.SUPPLIER : RobotType.GENERATOR);
        		}
        	}
        	else {
        		tunnelTo(target);
        	}
        } else {
        	tunnelTo(target);
        	if(WAVE_SIZE - (curRound % WAVE_SIZE) > 25 
        			&& rc.senseMine(rc.getLocation()) == null
        			&& waiting >= WAIT_TIMEOUT
        			&& rc.senseNearbyGameObjects(Robot.class, 24, this.enemyTeam).length == 0){
        		rc.layMine();
        	}
        }
      //}
    }
    
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
