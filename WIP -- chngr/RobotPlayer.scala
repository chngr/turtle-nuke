package randomplayer

import battlecode.common._
import scala.util.control.Breaks

object RobotPlayer {
  def run(rc : RobotController) {
    while(true) {
      try {
        if(rc.getType() == RobotType.HQ)
          hqCode(rc)
        if(rc.getTyoe() == RobotType.SOLDIER)
          soldierCode(rc)
      }
    }
  }

  // Takes the RobotController for HQ and run code here
  def hqCode(hq : RobotController) {
    var roundNum : Int = Clock.getRoundNum()
    var com : Communicator = new Communicator(roundNum)
    var notNuking : Boolean = com.checkNuke(roundNum)

    // Spawns in the direction of enemy hq
    def regularSpawn() : Unit = {
      var dir : Direction = hq.getLocation.directionTo(hq.senseEnemyHQLocation())
      if(hq.canMove(dir))
        hq.spawn(dir)
    }
    def research(upgrade : Upgrade => Unit) = {
      if(!hq.hasUpgrade(upgrade))
        researchUpgrade(upgrade)
    }

    try{
      // Check nuke reserach progress every 100 rounds
      if(notNuking && roundNum % 100 == 0){
        if(hq.senseEnemyNukeHalfDone()){
          com.nukeWarning()
          notNuking = false
        }
      }
      // Check is the HQ is active --- not already in the process
      if(hq.isActive()){
        // Spawn for now
        regularSpawn()
        hq.yield()
      }
    }
    catch{
      e : Exception => {
        println("caught exception: ")
        e.printStackTrace()
      }
    }
  }

  // Takes the RobotController for SOLDIERS and run code here
  def soldierCode(rc: RobotController) {
    var roundNum : Int = Clock.getRoundNum()
    var curLoc : MapLocation = rc.getLocation()
    var team : Team = rc.getTeam()

    // Tries to move toward home as much as possible; returns BOOL to show success
    def retreat() : Boolean = {
      var homeDir : Direction = curLoc.directionTo(rc.senseHQLocation())
      moveToLocation(homeDir)
    }

    // Tries to move toward location; returns boolean indicating sucess
    def moveToLocation(loc : MapLocation => Boolean) = {
      var locDir : Direction = curLoc.directionTo(loc)
      var succeed : Boolean = true
      val dist = curDir.distanceSquaredTo(loc)
      // Try to move toward direction
      // If not possible to move in one orthogonal direction towards location, don't move
      if(rc.isActive() && dist > 0){
        if(locDir.isDiagonal()){
          for(i <- 1 to 5){
            if(rc.canMove(locDir) && noMines(locDir)){
              rc.move(locDir)
              break
            }
            else{
              if(i % 2 == 0){
                for(_ <- 1 to i)
                  locDir.rotateLeft()
              }
              else if(i == 5)
                succeed = false
              else{
                for(_ <- 1 to i)
                  locDir.rotateRight()
              }
            }
          }
        }
        else{
          for(i <- 1 to 3){
            if(rc.canMove(locDir) && noMines(locDir)){
              rc.move(locDir)
              break
            }
            else{
              if(i == 1)
                locDir.rotateRight()
              else if(i == 2){
                for(_ <- 1 to i)
                  locDir.rotateLeft()
              }
              else
                succeed = false
            }
          }
        }
      }
      succeed
    }

    def noMines(dir : Direction => Boolean) = {
      var loc = curDir.add(dir)
      var senseMine(loc) = mine
      mine == null || mine == team
    }
    try{
      if(rc.isActive()){
        // Checks if on enemy mine
        if(rc.senseMine(curLoc) == team.opponent())
          retreat()
      }
    }
    catch{
      case e: Exception => {
        println("caught exception: ")
        e.printStackTrace()
      }
    }
  }
}
