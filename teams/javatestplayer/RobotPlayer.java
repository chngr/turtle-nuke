package testjavaplayer;

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
					if (rc.isActive()) {
						// Spawn a soldier
						Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
						if (rc.canMove(dir))
							rc.spawn(dir);
            // Tries to spawn, otherwise shall research instead
            else{
              Upgrade[] upgrades = {Upgrade.DEFUSION, Upgrade.FUSION, Upgrade.PICKAXE, Upgrade.VISION, Upgrade.NUKE};
              for(int i = 0; i < 5; i++){
                if(rc.hasUpgrade(upgrades[i]))
                  continue;
                else
                  rc.researchUpgrade(upgrades[i]);
              }
            }
					}
				} else if (rc.getType() == RobotType.SOLDIER) {
          Combat fighter = new Combat(rc);

          // Work if the soldier is active
          if(rc.isActive()){
            MapLocation goal = null;

            // Find encampments!
            MapLocation[] nearbyEncampments = rc.senseEncampmentSquares(rc.getLocation(), 32, Team.NEUTRAL);
            if(nearbyEncampments.length != 0){
              if(goal == null)
                goal = nearbyEncampments[0];
              if(rc.senseEncampmentSquare(rc.getLocation())){
                if(rc.senseCaptureCost() <= rc.getTeamPower()){
                  RobotType build;
                  if(Math.random() > 0.5)
                    build = RobotType.SUPPLIER;
                  else
                    build = RobotType.GENERATOR;
                  rc.captureEncampment(build);
                }
                else
                  goal = null;
              }
            }
            MapLocation home = rc.senseHQLocation();
            int distHome = rc.getLocation().distanceSquaredTo(home);
            int numAllies = rc.senseNearbyGameObjects(Robot.class, 32, rc.getTeam()).length;
            // Move in a random direction
            if(Math.random() < 0.8){
              if(goal == null){
                goal = rc.senseEnemyHQLocation();
              }
              Direction dir;
              // Move towards enemy hq
              double randVal = Math.random();
              if(randVal > 0.7){
                dir = rc.getLocation().directionTo(goal);
              }
              // Else randomly choose direction
              else if(randVal > 0.4){
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
              if(rc.senseMine(rc.getLocation().add(dir)) != null){
                if(Math.random() > 0.4)
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
            if(rc.senseNearbyGameObjects(Robot.class, 32, rc.getTeam().opponent()).length > 0)
              fighter.fight();
            rc.yield();
          }
				}

				// End turn
				rc.yield();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
