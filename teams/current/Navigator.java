package current;

/*************************************
Navigator.java - Navigation interfaces
Encapsulates all navigation-related functions.
DOES NOT INCLUDE INTELLIGENT GOAL SETTING.
*************************************/

import battlecode.common.*;

public class Navigator
{
	private BaseRobot r;
	
	// Costs (modify through e.g. [NavigatorObj].mineCost)
	public int mineCost;
	public int enemyCost;
//	public int normalCost = 0;
	
	Navigator(BaseRobot robot)
	{
		this.r = robot;
		mineCost = 10;
		enemyCost = 6;
	}
	
	
}