The Navigation module shall provide an easy interface to move to a specific
location in the map. The module shall handle any route finding necessary to
get to a location.

    class Navigation
      -------------------
      -- object variables
      -------------------

      -- soft constraints
      normalSquareCost :: Int = 0
      enemySquareCost :: Int = 6
      mineSquareCost :: Int = 10
      stepOnMines :: Bool = False

      -- path caching
      pathCache :: [Locations]

      -------------------
      -- public functions
      -------------------

      -- Pass in destination, returns a direction
      -- Moves its current move AND returns the direction of next move
      -- May also defuse if mine is in path planned by algorithm
      moveTo :: Destination -> Direction

      -- Setter functions, with normal square normalized to 0
      setMineCost :: Int -> ()
      setAvoidEnemy :: Int -> ()
      setDefuseCost :: Int -> ()
      setStepMine :: Bool -> ()

      -- Perhaps for other structure
      setPath :: [Locations] -> ()
