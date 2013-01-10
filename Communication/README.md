The Communication module shall provide an easy interface for sending and
receiving messages on the global message board.

    // Provides easy communication
    class Communication

      -- Each message shall be a 32-bit packet stored in 4 arrays
      --   0    1    2    3
      -- [ | ][   ][   ][   ]
      --
      -- Around half of 0 is a CHECKSUM
      -- Others shall be message

      -- Hopping around a 2500-entry, precomputed array of PRN

      fetchValue :: [Int] -> [Int]
      public globalTransmit
      public globalReceive
      public idTransmit
      public idReceive
