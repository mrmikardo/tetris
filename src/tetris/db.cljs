(ns tetris.db)

;; a tetromino is denoted by its starting coords, just outside of the visible playfield
(defonce tetrominos [
                     {:coords [[4 -1] [5 -1] [6 -1] [7 -1]] :colour "#6dedef" :tag :I}
                     {:coords [[5 -1] [5 -2] [6 -1] [7 -1]] :colour "#0016e4" :tag :J}
                     {:coords [[5 -1] [6 -1] [7 -1] [7 -2]] :colour "#e4a338" :tag :L}
                     {:coords [[5 -1] [5 -2] [6 -1] [6 -2]] :colour "#f1f04f" :tag :O}
                     {:coords [[5 -1] [6 -1] [6 -2] [7 -2]] :colour "#6eec47" :tag :S}
                     {:coords [[5 -1] [6 -1] [6 -2] [7 -1]] :colour "#9226ea" :tag :T}
                     {:coords [[5 -2] [6 -2] [6 -1] [7 -1]] :colour "#dd2f21" :tag :Z}
                     ])

(def default-db
  {:timer-interval-id nil
   :elapsed-game-time 0
   :playfield {
               :active-tetromino-colour (:colour (tetrominos 6))
               :active-tetromino-base-coords (:coords (tetrominos 6))
               :active-tetromino-rotation-matrix-id 0
               :active-tetromino-tag (:tag (tetrominos 6))
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
