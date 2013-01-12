package blank;

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
            SimpleNavigator nav = new SimpleNavigator(rc);
            Combat fighter = new Combat(rc);
            Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, 24, rc.getTeam().opponent());
            if(enemies.length > 0)
              fighter.fight();
            else
              nav.moveTo(rc.senseEnemyHQLocation());
            rc.yield();
          }
      }
			 catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
