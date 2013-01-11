package current;

/*************************************
Navigator.java - Navigation interfaces
Encapsulates all navigation-related functions.
DOES NOT INCLUDE INTELLIGENT GOAL SETTING.

In our internal representation of the map/costs:
[object id (if relevant)]  [cost]
       16 bits             16 bits
e.g. 0x0002000A = object id 2 with cost 10
*************************************/

import battlecode.common.*;

public class Navigator
{
	private BaseRobot r;
	
	// Costs (modify through e.g. [NavigatorObj].mineCost)
	public int mineCost;
	public int enemyCost;
//	public int normalCost = 0;

	// Internal state
	private int[][] map;
	
	Navigator(BaseRobot robot)
	{
		this.r = robot;
		this.mineCost = 10;
		this.enemyCost = 6;
		
		this.map = new int[r.rc.maph][r.rc.mapw];	// default initialization: grid of 0's
	}
	
}