package swarmwave;

import battlecode.common.*;

public class SoldierRobot extends BaseRobot {

  private MapLocation target;
  MapLocation HQ;
  MapLocation eHQ; 
  private static final int WAVE_SIZE = 200;
	
  SoldierRobot(RobotController rc){
    super(rc);
    HQ = rc.senseHQLocation();
	eHQ = rc.senseEnemyHQLocation(); 
	target = new MapLocation((3*HQ.x+eHQ.x)/4, (3*HQ.y+eHQ.y)/4);
	combat.setClumpingFactor(3);
	combat.setHostilityFactor(10);
  }

  public void run() throws GameActionException{
	if(curRound % WAVE_SIZE == 0) target = eHQ;
	
    if (rc.isActive()) {
    	
//      int numNearbyEnemies = rc.senseNearbyGameObjects(Robot.class, 24, this.enemyTeam).length;
//      if(numNearbyEnemies > 0){
//        this.combat.fight();
//      }
//      else {
        if(false){ //##Explorer; capture encampments
        	
        } else {
        	tunnelTo(target);
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
	  }
}
  private void moveOrDefuse(Direction dir) throws GameActionException{
	  MapLocation loc = rc.getLocation().add(dir);
	  Team mine = rc.senseMine(loc);
	  if (mine == rc.getTeam() || mine == null){
		  rc.move(dir);
	  } else {
		  rc.defuseMine(loc);
	  }
  }
}
