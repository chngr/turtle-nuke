package javatestplayer;

import battlecode.common.*;

/** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously.
 */
public class RobotPlayer {
	public static void run(RobotController rc) {
		while (true) {
			try {
				if (rc.getType() == RobotType.HQ) {
          hqCode(rc);
				} else if(rc.getType() == RobotType.ARTILLERY){
		  artilleryCode(rc);		
				} else if (rc.getType() == RobotType.SOLDIER) {
          Combat fighter = new Combat(rc);

          // Work if the soldier is active
          if(rc.isActive()){
            if(rc.senseNearbyGameObjects(Robot.class, 14, rc.getTeam().opponent()).length > 0)
              fighter.fight();
            else{
              MapLocation goal = rc.senseEnemyHQLocation();

              // Find encampments!
              MapLocation[] nearbyEncampments = rc.senseEncampmentSquares(rc.getLocation(), 32, Team.NEUTRAL);
              if(nearbyEncampments.length != 0){
                goal = nearbyEncampments[nearbyEncampments.length - 1];
                if(rc.senseEncampmentSquare(rc.getLocation())){
                  if(rc.senseCaptureCost() <= rc.getTeamPower()){
                    RobotType build;
                    // ## testing with artillery
                    build = RobotType.ARTILLERY;
//                    if(Math.random() > 0.5)
//                      build = RobotType.SUPPLIER;
//                    else
//                      build = RobotType.GENERATOR;
                    rc.captureEncampment(build);
                  }
                }
              }
              // Move in a random direction
              if(Math.random() < 0.1 && rc.senseMine(rc.getLocation()) == null){
                rc.layMine();
              }
              else if(Math.random() < 0.9){
                if(goal == null){
                  goal = rc.senseEnemyHQLocation();
                }
                Direction dir;
                // Move towards enemy hq
                double randVal = Math.random();
                if(randVal > 0.4){
                  dir = rc.getLocation().directionTo(goal);
                }
                // Else randomly choose direction
                else if(randVal > 0.1){
                  dir = Direction.values()[(int)(Math.random() * 8)];
                }
                else{
                  Robot[] nearbyAllies = rc.senseNearbyGameObjects(Robot.class, rc.getLocation(), 32, rc.getTeam());
                  if(nearbyAllies.length > 0){
                    dir = rc.getLocation().directionTo(rc.senseRobotInfo(nearbyAllies[0]).location);
                  }
                  else{
                    dir = rc.getLocation().directionTo(rc.senseHQLocation());
                  }
                }
                // Check if the thing is a mine
                if(rc.senseMine(rc.getLocation().add(dir)) != null && rc.senseMine(rc.getLocation().add(dir)) != rc.getTeam()){
                  if(Math.random() > 0.1)
                    rc.defuseMine(rc.getLocation().add(dir));
                  else
                    rc.yield();
                }
                else if(rc.canMove(dir)){
                  rc.move(dir);
                }
                else{
                  goal = null;
                }
              }
            }
          }
          rc.yield();
        }
      } catch (Exception e) {
        //e.printStackTrace();
      }
    }
  }
  private static void hqCode(RobotController rc) throws GameActionException{
    if (rc.isActive()) {
      // Spawn a soldier
      Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
      if (rc.canMove(dir) && Clock.getRoundNum() % 2 != 0)
        rc.spawn(dir);
      // Tries to spawn, otherwise shall research instead
      else{
        Upgrade[] upgrades = {Upgrade.PICKAXE, Upgrade.FUSION, Upgrade.DEFUSION, Upgrade.VISION, Upgrade.NUKE};
        for(int i = 0; i < 5; i++){
          if(rc.hasUpgrade(upgrades[i]))
            continue;
          else{
            rc.researchUpgrade(upgrades[i]);
            break;
          }
        }
      }
    }
  }
  private static void artilleryCode(RobotController rc) throws GameActionException{
	  if(rc.isActive()){
			// Choose target
			// ## should be like combat, build local map and search for good location
			Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 64, rc.getTeam().opponent());
			if(enemies.length > 0) rc.attackSquare(rc.senseLocationOf(enemies[0]));
		}
  }
}
