(ns tetris.db)

(def default-db
  {:timer-interval-id nil
   :elapsed-game-time 0
   :playfield {
               :active-tetromino-colour "#dd2f21"
               :active-tetromino-coords [[1 0] [2 0] [1 -1] [2 -1]]
               ;; to start with, base is the bottom row
               :base-coords {[0 16] "#000000"
                             [1 16] "#000000"
                             [2 16] "#000000"
                             [3 16] "#000000"
                             [4 16] "#000000"
                             [5 16] "#000000"
                             [6 16] "#000000"
                             [7 16] "#000000"
                             [8 16] "#000000"
                             [9 16] "#000000"}
               }})
