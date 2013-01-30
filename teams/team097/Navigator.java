package team097;

/*************************************
Navigator.java - Navigation interfaces
Encapsulates all navigation-related functions.
DOES NOT INCLUDE INTELLIGENT GOAL SETTING.

In our internal representation of the map/costs:
[object id (if relevant)]  [square attributes]
       16 bits             16 bits
e.g. 0x0002000A = object id 2 with content of type 10
current square attributes:
0x00000001: hostile mine
*************************************/

//Notes:
//Multiply heuristic cost by 1.01 to increase greediness? Won't break anything
import battlecode.common.*;

// SEARCH:
// *** I think there might be unmerged bugfixes and improvements in test
//TODO: !!! Do the next search (centered at the current goal location) while moving !!!
// - Should start with a shallow search or delegate to another movement method
//   while first search is in progress

//TODO:@1 other robots
//TODO: map edges

//TODO: don't copy the whole path for new nodes, just include a reference to the parent's path
//TODO: error handling
//TODO: bytecodes
//TODO: getter/setter for searchDepth, searchFreq
//TODO:@5 danger and impatience state variables; will need to replan path when they change


//Infinite loop debugging:
// 	private int debugCounter = 0; //DEBUG
//	r.rc.setIndicatorString(0,Clock.getRoundNum()+";"+r.id +": "+ debugCounter++); //DEBUG


public class Navigator {
	
	private static final Direction[] DIRECTIONS = {
		Direction.NORTH,
		Direction.NORTH_EAST,
		Direction.EAST,
		Direction.SOUTH_EAST,
		Direction.SOUTH,
		Direction.SOUTH_WEST,
		Direction.WEST,
		Direction.NORTH_WEST
	}; //?
		
	private BaseRobot r;
	
	
	// ------------   SEARCH   ------------
	// Truncated pseudo A* search to find the best path to the goal
	// Efficient movement, but expensive computationally

	private int searchDepth = 5; //?
	private int mapDiagRSquared = 2*searchDepth*searchDepth;
	private int mapSize = 4*searchDepth+1; // Needs to be >= 2*floor(sqrt(2)*searchDepth)+1
	private int offset = (mapSize-1)/2;
	private int maxPathLength = 6; //? //#
	
	private int searchFreq = 3; // Needs to be <= searchDepth
	
	//# may need to be private with getter/setter, other things might change with it
	public int mineCost = 100; //?
	
	
	private MapLocation loc;
	
	private int[][] localMap;
	
	
	Navigator(BaseRobot robot){
		this.r = robot;
		this.localMap = new int[mapSize][mapSize]; // automatically initialized to 0
	}
	
	private Direction[] path;
	private int pathIdx;
	private int searchTimer = 0;
	private MapLocation currentDest;

	public Direction moveTo(MapLocation dest) throws GameActionException{
		if(r.rc.isActive()){
			if(!dest.equals(currentDest) || (searchTimer %= searchFreq) == 0){
				updateMap();
				
				currentDest = dest;
				pathIdx = 0;
				
				path = search(dest);
			}
			
			if(path[pathIdx] == null) {
				return Direction.OMNI; // at destination
			}
			else if(!takeStep(path[pathIdx])){	//## handle failed move. re-search?
				return null; // move failed
			} else {
				return path[pathIdx];
			}
		}
		return Direction.NONE; //?
	}
	
	
	// Returns success of movement
	public boolean takeStep(Direction dir) throws GameActionException {

		if(r.rc.canMove(dir)){
			MapLocation dest = r.rc.getLocation().add(dir);
			//#check diffuseMines var first
			Team mine = r.rc.senseMine(dest);
			if(mine == Team.NEUTRAL || mine == r.rc.getTeam().opponent()){
				r.rc.defuseMine(r.rc.getLocation().add(dir));
			}
			else{
				r.rc.move(dir);
				pathIdx++;
				searchTimer++;
			}
			return true;
		}
		return false;
	}
	
	//## switch to utility version?
	public int chessDistance(MapLocation loc1, MapLocation loc2){
		return Math.max(Math.abs(loc1.x - loc2.x), Math.abs(loc1.y - loc2.y)); //Efficiency: ifs are probably faster
	}
	private int idxDistance(int[] idxs){
		return Math.max(Math.abs(idxs[0] - offset), Math.abs(idxs[1] - offset));
	}
	
	
	
	private void updateMap(){
		loc = r.rc.getLocation();

		localMap = new int[mapSize][mapSize]; // automatically initialized to 0
		MapLocation[] nearbyMines = r.rc.senseNonAlliedMineLocations(loc, mapDiagRSquared);
		
		for(MapLocation mine : nearbyMines){
			int[] idx = locToIndex(mine);
			localMap[idx[0]][idx[1]] = 1;
		}
	}
	
	private int[] locToIndex(MapLocation loc){
		int[] index = new int[2];
		index[0] = loc.x - this.loc.x + offset;
		index[1] = loc.y - this.loc.y + offset;
		return index;
	}
	
	private MapLocation idxToLoc(int[] idx){
		return new MapLocation(this.loc.x + idx[0] - offset, this.loc.y + idx[1] - offset);
	}
	
	
	// Efficiency note: there are priority queue implementations with better asymptotic complexity
	// (loglog(n) insert/pop); not sure how they compare in this case. There's probably something clever
	// we can do given that we can bound the number of inserts that will be performed (mapSize^2)
	private Direction[] search(MapLocation goal){ // returns path
		
		boolean[][] visited = new boolean[mapSize][mapSize];
		
		LocationQueue frontier = new LocationQueue(mapSize*mapSize);
		frontier.add(new QueueElement());
		visited[offset][offset] = true;
		
		QueueElement cur;
		int[] newLoc;
		int edgeCost, heuristic;
		while(true){

			cur = frontier.poll();
			if(idxDistance(cur.idx) == searchDepth || cur.pathLength == maxPathLength){ // is this the best way to limit pathlength?
				return cur.path;
			}
			for(Direction d : DIRECTIONS){
				newLoc = new int[]{cur.idx[0]+d.dx, cur.idx[1]+d.dy}; //##does this work?
				if(!visited[newLoc[0]][newLoc[1]]){
					System.out.println(idxToLoc(newLoc).toString());
					heuristic = chessDistance(idxToLoc(newLoc), goal);
					if(heuristic == 0) {
						Direction[] goalPath = cur.path;
						goalPath[cur.pathLength+1] = d;
						return goalPath;
					}
					edgeCost = ((localMap[newLoc[0]][newLoc[1]] == 1) ? mineCost : 1); //@1 Future improvements here
					frontier.add(new QueueElement(cur, d, edgeCost, heuristic));
					visited[newLoc[0]][newLoc[1]] = true;
				}
			}
		}
		
	}
	
	class QueueElement {
		public int pathLength;
		public Direction[] path;
		public int cost;
		public int priority;
		public int[] idx;

		// Initial QueueElement
		QueueElement() {
			priority = 0;
			cost = 0;
			path = new Direction[maxPathLength+1]; // Guaranteed null at end
			pathLength = 0;
			idx = new int[] {offset,offset};
		}
		
		QueueElement(QueueElement base, Direction dir, int edgeCost, int heuristic){
			pathLength = base.pathLength;
			path = new Direction[maxPathLength];
			System.arraycopy(base.path, 0, path, 0, pathLength);
			path[pathLength++] = dir;
			
			idx = new int[] { base.idx[0] + dir.dx, base.idx[1] + dir.dy};
			cost = base.cost + edgeCost;
			priority = cost + heuristic;
		}

	}
	// ##not locations, rename
	// Using fixed types for now because generics + arrays is awful
	class LocationQueue {
		
		private static final int INF = 10000;
		
		private QueueElement[] queue;
		private int end = 0;
		
		LocationQueue(int maxSize){
			queue = new QueueElement[maxSize+1];
		}
		
		public void add(QueueElement qe){
			queue[++end] = qe;
			
			int curNode = end;
			int parent;
			QueueElement tmp;
			while(curNode > 1 && queue[curNode].priority < queue[parent=curNode/2].priority){
				tmp = queue[curNode];
				queue[curNode] = queue[parent];
				queue[parent] = tmp;
				
				curNode = parent;
			}
		}
		
		// Throws nullPointerException if queue is empty
		public QueueElement poll(){
			QueueElement min = queue[1];
			queue[1] = queue[end];
			queue[end--] = null;
			
			int curNode = 1, 
				l = 2,
				r = 3,
				lp = (l > end) ? INF : queue[l].priority,
				rp = (r > end) ? INF : queue[r].priority;
			int child;
			QueueElement tmp;
			while(queue[curNode].priority > lp || queue[curNode].priority > rp){
				
				child = lp < rp ? l : r;
				
				tmp = queue[curNode];
				queue[curNode] = queue[child];
				queue[child] = tmp;
				
				curNode = child;
				l = child << 1;
				r = l + 1;
				lp = (l > end) ? INF : queue[l].priority;
				rp = (r > end) ? INF : queue[r].priority;
			}
			
			return min;
		}
		
	}
	
	

	// ------------   TUNNEL   ------------
	// Move directly to the target, defusing mines
	// Useful if the path is going to be traveled often, or we don't want mines there
	
    public void tunnelTo(MapLocation loc) throws   GameActionException{
    	if(r.util.getDistanceBetween(r.curLoc, loc) > 3){
	    	looseTunnel(loc); // ## TEST
    	} else {
	    	Direction dir = r.curLoc.directionTo(loc);
	    	
	    	//## !! other movements want this !!
	    	if(r.util.senseHostileMine(r.curLoc)){ // Stepped on undetected enemy mine
	    		moveNear(dir); // Make sure we move, not defuse
	    		return;
	    	}
			if(!loc.equals(r.rc.getLocation())){ //apparently necessary; why doesn't canMove catch this?
			    if(r.rc.canMove(dir)) moveOrDefuse(dir);
			    else if(r.rc.canMove(dir.rotateRight()))  moveOrDefuse(dir.rotateRight());
			    else if(r.rc.canMove(dir.rotateLeft()))  moveOrDefuse(dir.rotateLeft());
			    // ##?
			    else if(r.rc.canMove(dir.rotateRight().rotateRight()))  moveOrDefuse(dir.rotateRight().rotateRight());
			    else if(r.rc.canMove(dir.rotateLeft().rotateLeft()))  moveOrDefuse(dir.rotateLeft().rotateLeft());
	
			}
    	}
	}
    
    public void looseTunnel(MapLocation loc) throws GameActionException{
    	Direction dir = r.curLoc.directionTo(loc);
		if(!loc.equals(r.curLoc)){ //apparently necessary; why doesn't canMove catch this?
	    	if(r.util.senseHostileMine(r.curLoc)){ // Stepped on undetected enemy mine
	    		moveNear(dir); // Make sure we move, not defuse
	    		return;
	    	}
	    	if(r.rc.canMove(dir) && !r.util.senseHostileMine(r.curLoc.add(dir))) r.rc.move(dir);
		    else if(r.rc.canMove(dir.rotateRight()) && !r.util.senseHostileMine(r.curLoc.add(dir.rotateRight())))  r.rc.move(dir.rotateRight());
		    else if(r.rc.canMove(dir.rotateLeft()) && !r.util.senseHostileMine(r.curLoc.add(dir.rotateLeft())))  r.rc.move(dir.rotateLeft()); 
		    
		    else if(r.rc.canMove(dir)) moveOrDefuse(dir);
		    else if(r.rc.canMove(dir.rotateRight()))  moveOrDefuse(dir.rotateRight());
		    else if(r.rc.canMove(dir.rotateLeft()))  moveOrDefuse(dir.rotateLeft());
		    // ##?
		    else if(r.rc.canMove(dir.rotateRight().rotateRight()))  moveOrDefuse(dir.rotateRight().rotateRight());
		    else if(r.rc.canMove(dir.rotateLeft().rotateLeft()))  moveOrDefuse(dir.rotateLeft().rotateLeft());
		}
    }
    
	
	public boolean moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation loc = r.rc.getLocation().add(dir);
		Team mine = r.rc.senseMine(loc);
		if (mine == r.rc.getTeam() || mine == null){
		   r.rc.move(dir);
		   return true;
		}
		r.rc.defuseMine(loc);
		return false;
	}
	
	// Move as close to the dir as possible, but always move if possible
	public boolean moveNear(Direction dir) throws GameActionException{

	      // Need to do this first so the termination check works
	      if(r.rc.canMove(dir) && !r.util.senseHostileMine(r.curLoc.add(dir))){
	    	  r.rc.move(dir);
		      return true;
	      }
	      // Move as close to the desired direction as possible
		  Direction dirLeft = dir;
		  Direction dirRight = dir;	
		  do {
			dirLeft = dirLeft.rotateLeft();
		    if (r.rc.canMove(dirLeft) && !r.util.senseHostileMine(r.curLoc.add(dirLeft))) {
		      r.rc.move(dirLeft);
		      return true;
		    }
		    
		    dirRight = dirRight.rotateRight(); 
		    if (r.rc.canMove(dirRight)  && !r.util.senseHostileMine(r.curLoc.add(dirRight))) {
		      r.rc.move(dirRight);
		      return true;
		    }
		        
		  } while (dirRight != dirLeft) ;
		  
		  return false;
	}
	
}
