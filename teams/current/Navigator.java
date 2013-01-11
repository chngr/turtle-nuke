package current;

import battlecode.common.*;


//Notes:
//Stop when at search depth (i.e. edge of localMap)
//Multiply heuristic cost by 1.01 to increase greediness? Won't break anything

//TODO: Pass alloted bytecodes?

public class Navigator {
	
	private BaseRobot r;
	

	private int searchDepth = 5; //?
	private int mapDiagRSquared = 2*searchDepth*searchDepth;
	private int mapSize = 4*searchDepth+1; // Needs to be >= 2*floor(sqrt(2)*searchDepth)+1
	private int offset = (mapSize-1)/2;
	
	//# may need to be private with getter/setter, other things might change with it
	public int mineCost = searchDepth-1; //?
	
	
	private MapLocation loc;
	
	// int instead of enum for future flexibility
	// such as combinations of types, e.g. ally and mine
	private int[][] localMap;
	
	
	Navigator(BaseRobot robot){
		this.r = robot;
	}

	public Direction moveTo(MapLocation dest){
		
		
		return null;
	}
	
	public int chessDistance(MapLocation loc1, MapLocation loc2){
		return Math.min(Math.abs(loc1.x - loc2.x), Math.abs(loc1.y - loc2.y));
	}
	private int idxDistance(int[] idxs){
		return Math.min(Math.abs(idxs[0] - offset), Math.abs(idxs[1] - offset));
	}
	
	
	
	private void updateMap(){
		loc = r.rc.getLocation(); //## robot property? otherwise nav property? WTF is the bytecode cost anyway?

		localMap = new int[mapSize][mapSize]; // automatically initialized to 0 (i.e. default value of int)
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
	
	
	//## test PriorityQueue first?
	//## return path
	
	// Efficiency note: there are priority queue implementations with better asymptotic complexity
	// (loglog(n) insert/pop); not sure how they compare in this case. There's probably something clever
	// we can do given that we can bound the number of inserts that will be performed (mapSize^2)
	private void search(MapLocation goal){
		
		boolean[][] visited = new boolean[mapSize][mapSize];
		
		LocationQueue frontier = new LocationQueue(mapSize*mapSize);
		frontier.add(); //## directionTo goal
		
		int[] cur, next;
		int cost;
		while(true){
			cur = frontier.poll();
			if(idxDistance(cur) == searchDepth){
				return 
			}
			for(Direction d : Direction.values()){
				next = {cur[0]+d.dx, cur[1]+d.dx}; //##does this work?
				if(!visited[next[0]][next[1]]){
					visited[next[0]][next[1]] = true;
					cost = curCost + chessDistance(idxToLoc(next), goal) + (localMap[next[0]][next[1]] == 1) ? mineCost : 1; //## change mine detect
					frontier.add(cost, next);
				}
			}
		}
		
	}
	
	// ##not locations, rename
	// Using fixed types for now because generics + arrays is awful
	class LocationQueue {
		
		private QueueElement[] queue;
		private int end = 0;
		
		LocationQueue(int maxSize){
			queue = new QueueElement[maxSize+1];
		}
		
		//## flip inequalities here and poll?
		public void add(int p, int[] idxs){
			queue[++end] = new QueueElement(p,idxs);
			
			int curNode = end;
			int parent;
			QueueElement tmp;
			while(curNode > 1 && queue[curNode].priority > queue[parent=curNode/2].priority){
				tmp = queue[curNode];
				queue[curNode] = queue[parent];
				queue[parent] = tmp;
				
				curNode = parent;
			}
		}
		
		// Throws nullPointerException if queue is empty
		// ## needs to return cost as well
		public int[] poll(){
			QueueElement min = queue[1];
			queue[1] = queue[end];
			
			int curNode = 1, 
				l = 2,
				r = 3,
				lp = (l > end) ? 0 : queue[l].priority,
				rp = (r > end) ? 0 : queue[r].priority;
			int child;
			QueueElement tmp;
			while(queue[curNode].priority < lp || queue[curNode].priority < rp){
				
				child = queue[l].priority > queue[r].priority ? l : r;
				
				tmp = queue[curNode];
				queue[curNode] = queue[child];
				queue[child] = tmp;
				
				curNode = child;
				l = child >> 1;
				r = l + 1;
				lp = (l > end) ? 0 : queue[l].priority;
				rp = (r > end) ? 0 : queue[r].priority;
			}
			
			return min.val;
		}
		
	}
	
	class QueueElement {
		public int priority;
		public int[] val;
		QueueElement(int p, int[] idxs){ 
			priority = p;
			val = idxs;
		}
	}
	
	
}
