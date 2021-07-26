(ns tetris.db)

(def default-db
  {:timer-interval-id nil
   :elapsed-game-time 0
   :playfield {
               :active-tetromino-colour "#dd2f21"
               :active-tetromino-coords [[1 0] [2 0] [1 -1] [2 -1]]
               ;; to start with, base is the bottom row
               :base-coords [[0 15]
                             [1 15]
                             [2 15]
                             [3 15]
                             [4 15]
                             [5 15]
                             [6 15]
                             [7 15]
                             [8 15]
                             [9 15]]
               }})
