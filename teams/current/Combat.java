package current;

/**********************************
Combat.java - Combat interface

This module exports basic functionality for combat and fighting. The
main method is

    Combat.fight();

which controls immediate combat maneuvering. All calculations and
movement will be controlled purely by the Combat class through that
function.


The core philosophy is that we want to be adjacent to few enemies, 
that are themselves adjacent to our allies. To achieve this we calculate
the value and cost of attacking each enemy, then add the costs and
average the values over the enemies adjacent to a potential move.
This reflects the fact that our damage is divided among enemies,
while their damage is additive.

The value of attacking an enemy is proportional to the number of allies
adjacent to it, (reflecting the fact that here our damage sums, and we 
want to focus fire). The cost of attacking an enemy is proportional to
1/(adjacentAllies+1), reflecting the division of their damage.

**********************************/

import java.util.Arrays; //DEBUG
import battlecode.common.*;

//Notes: If there are very many enemies in range, this might timeout; we may want to add
//a check for that, or just accept our inevitable doom. In practice, happens very rarely (< 1/10 games)
//However, combat.fight() does take ~8500 bytecodes in heavy combat situations; drains our power.


//TODO: Set enemy cost 0 if not active (defusing, mining)


public class Combat{
  private BaseRobot r;

  Combat(BaseRobot robot){
    this.r = robot;
  }

  /***************************************************************************
   * Constants and Parameters for Combat.java
   **************************************************************************/

  //## scale by own energon in computation? don't wan't to disengage 
  //## (unless we have medbay, but that would be a combat behavior check)
  //## but would be ideal if the more damaged units took the less dangerous positions
  
  // These need to be mutually balanced for combat to work properly
  // If we want to engage a full health enemy in single combat, we need
  // enemyValue - 40*enemyEnergonScale > enemyCost
  public double enemyCost = 6;
  public double enemyValue = 8.1;
  public double allyFactor = 5; // Multiplied by number of allies adjacent in enemy value calculation
  public double enemyEnergonScale = 1/20; // We subtract (enemy energon) * scale from the enemy's value
  
  // Cost of moving onto a mine; if this is 0, we ignore mines
  // ## should defuse instead
  public double mineCost = 1000;
  
  // Value of attacking enemy HQ and encampments
  public double enemyHQVal = 20;
  public double enemyEncampmentVal = 10;
  
  // Discount factors for enemies two squares away
  // We only count the cost of unengaged far enemies; they are about as dangerous as adjacent ones
  public double farCostDiscount = 0.9;
  // We can't actually attack non-adjacent enemies; moving towards them is only valuable for the future
  public double farValueDiscount = 0.1; 
  
  
  /**
   * Local variables
   */
  
  // Hard-coded values for possible move directions
  private Direction[] moveDirs = {Direction.NONE,
		  						  Direction.NORTH_WEST,
                                  Direction.NORTH,
                                  Direction.NORTH_EAST,
                                  Direction.WEST,
                                  Direction.EAST,
                                  Direction.SOUTH_WEST,
                                  Direction.SOUTH,
                                  Direction.SOUTH_EAST};

  // Local map for combat
  // Stores energon of robots; + for allies, - for enemies
  private double[][] localMap; //7x7
  
  // Store the value and cost of being near enemies
  private double[][] enemyValues; //7x7
  private double[][] enemyCosts; //7x7

  
  // Store the indexes of nearby enemies in the local map, for iteration
  private int[][] enemyIdxs; //48x2; 48 is max enemies in range
  private int numEnemies;
  

  /***************************************************************************
   * Primary public method for the package; interface for the combat algorithm.
   *
   * The basic procedure for the fight() routine is as follows:
   *
   *  1. Sense environment for nearby game objects and put them in the localMap
   *  2. Compute the value and cost of being near each enemy, and store them
   *  3. Compute the net value of moving in each movable direction (including none)
   *  4. Move in the direction with the highest net value
   *
   *  enemyDanger can be set to change the cost of moving near enemies;
   *  more configurable parameters may be added later.
   ***************************************************************************/
  public void fight() throws GameActionException{
	// 0. Reset map, costs and enemies
	reset();
	
    // 1. Senses and populates local map
    updateMap();
    
    // 2. Compute enemy values and costs
    evaluateEnemies();

    // 3. Calculate the net score of adjacent squares and current square
    double[] directionScores = computeScores();

    // 4. Move to the square with highest net score (possibly the current square)
    double bestScore = directionScores[0];
    int bestDirIdx = 0;
    
    //r.rc.setIndicatorString(2, Arrays.toString(directionScores)); //DEBUG

    for(int i = 1; i < 9; i++){
      if(directionScores[i] > bestScore){
        bestScore = directionScores[i];
        bestDirIdx = i;
      }
    }

    //canMove catches map edges and bytcode overrun (happens very occasionally)
    if(bestDirIdx != 0 && r.rc.canMove(moveDirs[bestDirIdx])) // 0: Direction.None
      r.rc.move(moveDirs[bestDirIdx]);
  }

  /**************************************************************************
   * Core Functions of Combat.java
   **************************************************************************/
  private void updateMap() throws GameActionException{

	
	// ## for complete eval of diagonal, radSquared should be 18; 
	// ## only helps if ally can see for us, would need to check if squares are in bounds of array
    Robot[] nearbyRobots = r.rc.senseNearbyGameObjects(Robot.class, 14);
    for(Robot robot: nearbyRobots){
      RobotInfo ri = r.rc.senseRobotInfo(robot);
      int dx = r.curLoc.x - ri.location.x;
      int dy = r.curLoc.y - ri.location.y;

      if(ri.team == r.myTeam)
        localMap[3 - dx][3 - dy] = ri.energon;
      else{
    	  if(ri.type == RobotType.SOLDIER){
	        localMap[3 - dx][3 - dy] = -ri.energon;
	        enemyIdxs[numEnemies][0] = 3 - dx;
	        enemyIdxs[numEnemies][1] = 3 - dy;
	        numEnemies++;
    	  } else {
        	  localMap[3 - dx][3 - dy] = -1; // So we don't try to step here
        	  enemyValues[3 - dx][3 - dy] = (ri.type == RobotType.HQ) ? enemyHQVal : enemyEncampmentVal;
          }
      }
      
    }
    localMap[3][3] = 0; // Ignore ourselves
  }

  // @E If this is too slow, there is less value in evaluating far enemies;
  // we could only populate a 5x5, and change the computation of farCost
  // to something simpler
  private void evaluateEnemies(){
	  int x, y, allyCount;
	  for(int i=0; i<numEnemies; i++){
		  x = enemyIdxs[i][0];
		  y = enemyIdxs[i][1];
		  allyCount = isAlly(x-1,y-1) + isAlly(x,y-1) + isAlly(x+1,y+1) +
                  isAlly(x-1,y) + isAlly(x+1,y) + isAlly(x-1,y+1) +
                  isAlly(x,y+1) + isAlly(x+1,y+1);
		  enemyValues[x][y] = enemyValue + allyCount*allyFactor + localMap[x][y]*enemyEnergonScale; // Last term is -enemyEnergon/scale
		  enemyCosts[x][y] = enemyCost/(allyCount + 1);
	  }
	  
  }
  private int isAlly(int x, int y){
	  if(x < 0 || x > 6 || y < 0 || y > 6) return 0; // Check if out of bounds
	  return (localMap[x][y] > 0) ? 1 : 0;
  }
  
  
  // Computes cost based on a square around the tile
  private double[] computeScores(){
    double[] directionCosts = new double[9];

    directionCosts[0] = crossCost(3, 3);
    directionCosts[1] = crossCost(2, 2);
    directionCosts[2] = crossCost(3, 2);
    directionCosts[3] = crossCost(4, 2);
    directionCosts[4] = crossCost(2, 3);
    directionCosts[5] = crossCost(4, 3);
    directionCosts[6] = crossCost(2, 4);
    directionCosts[7] = crossCost(3, 4);
    directionCosts[8] = crossCost(4, 4);
    
    return directionCosts;
  }

  /***************************************************************************
  * Computes the cost of a given square. Uses the values and costs of 
  * squares within two of the given square.
  ***************************************************************************/
  private int numAdjacent;
  private double crossCost(int x, int y){
	  
	if(localMap[x][y] != 0) return -100000; // Occupied; can't move here
	else if(r.util.senseHostileMine(r.curLoc.add(x-3,y-3)) && mineCost != 0) return -mineCost;
	  
	numAdjacent = 0;
	
    // Two squares away
	double farCost = farCost(enemyCosts[x-1][y-2]) + farCost(enemyCosts[x][y-2]) + farCost(enemyCosts[x+1][y-2]) +
            farCost(enemyCosts[x-2][y-1]) + farCost(enemyCosts[x-1][y]) + farCost(enemyCosts[x-1][y+1]) +
            farCost(enemyCosts[x+2][y-1]) + farCost(enemyCosts[x+2][y]) + farCost(enemyCosts[x+2][y+1]) +
            farCost(enemyCosts[x-1][y+2]) + farCost(enemyCosts[x][y+2]) + farCost(enemyCosts[x+1][y+2]) +
            farCost(enemyCosts[x+2][y+2]) + farCost(enemyCosts[x+2][y-2]) + farCost(enemyCosts[x-2][y+2]) +
            farCost(enemyCosts[x-2][y-2]);

	double farVal = enemyValues[x-1][y-2] + enemyValues[x][y-2] + enemyValues[x+1][y-2] +
            enemyValues[x-2][y-1] + enemyValues[x-1][y] + enemyValues[x-1][y+1] +
            enemyValues[x+2][y-1] + enemyValues[x+2][y] + enemyValues[x+2][y+1] +
            enemyValues[x-1][y+2] + enemyValues[x][y+2] + enemyValues[x+1][y+2] +
            enemyValues[x+2][y+2] + enemyValues[x+2][y-2] + enemyValues[x-2][y+2] +
            enemyValues[x-2][y-2];
	
    // One square away
	double adjCost = enemyCosts[x-1][y-1] + enemyCosts[x][y-1] + enemyCosts[x+1][y+1] +
            enemyCosts[x-1][y] + enemyCosts[x+1][y] + enemyCosts[x-1][y+1] +
            enemyCosts[x][y+1] + enemyCosts[x+1][y+1];

	double adjVal = hasEnemy(enemyValues[x-1][y-1]) + hasEnemy(enemyValues[x][y-1])+ hasEnemy(enemyValues[x+1][y+1]) +
            hasEnemy(enemyValues[x-1][y]) + hasEnemy(enemyValues[x+1][y]) + hasEnemy(enemyValues[x-1][y+1]) +
            hasEnemy(enemyValues[x][y+1]) + hasEnemy(enemyValues[x+1][y+1]);
	
	//r.rc.setIndicatorString(2, (farVal * farValueDiscount) +" "+ (farCost * farCostDiscount) +" "+ adjVal/numAdjacent +" "+ adjCost); //DEBUG
    return (farVal * farValueDiscount) - (farCost * farCostDiscount) + ((numAdjacent == 0) ? 0 : adjVal/numAdjacent) - adjCost;
  }
  private double farCost(double mapVal){
	  // Ignore the cost of enemies if they are already engaged with our allies
	  return (mapVal == enemyCost) ? mapVal : 0;
  }
  private double hasEnemy(double mapVal){
	  if(mapVal != 0) numAdjacent++;
	  return mapVal;
  }
  
  
  // Reset instance variables
  private void reset(){
		localMap = new double[7][7];
		
		enemyIdxs = new int[48][2];
		numEnemies = 0;
		
		enemyValues = new double[7][7];
		enemyCosts = new double[7][7];
  }

}
