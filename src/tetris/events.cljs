(ns tetris.events
  (:require
   [re-frame.core :as re-frame]
   [tetris.db :as db]
   ))

;; a tetromino is denoted by its starting coords, just outside of the visible playfield
(defonce tetrominos [
                     {:coords [[4 -1] [5 -1] [6 -1] [7 -1]] :colour "#6dedef"} ;; I
                     {:coords [[5 -1] [5 -2] [6 -1] [7 -1]] :colour "#0016e4"} ;; J
                     {:coords [[5 -1] [6 -1] [7 -1] [7 -2]] :colour "#e4a338"} ;; L
                     {:coords [[5 -1] [5 -2] [6 -1] [6 -2]] :colour "#f1f04f"} ;; O
                     {:coords [[5 -1] [6 -1] [6 -2] [7 -2]] :colour "#6eec47"} ;; S
                     {:coords [[5 -1] [6 -1] [6 -2] [7 -1]] :colour "#9226ea"} ;; T
                     {:coords [[5 -2] [6 -2] [6 -1] [7 -1]] :colour "#dd2f21"} ;; Z
                     ])

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(defn- update-tetromino-position
  "Move the currently falling tetromino one coord closer to the base."
  [coords]
  (map #(vector (first %) (+ 1 (second %))) coords))

(defn- contiguous-with-base?
  "A tetromino is contiguous with the base if on the next tick of the clock
  its coords would overlap with the base coords."
  [tetromino-coords base-coords]
  (>=
      (count
       (clojure.set/intersection
        (set (update-tetromino-position tetromino-coords))
        (set base-coords)))
      1))

(defn- merge-colour-with-coords [coords colour]
  (apply hash-map (interleave coords (repeat colour))))

(defn- update-playfield [playfield]
  (let [{:keys [active-tetromino-coords active-tetromino-colour base-coords]} playfield]
    (if (contiguous-with-base? active-tetromino-coords (keys base-coords))
      (let [next-tetromino (rand-nth tetrominos)]
        (-> playfield
            ;; wipe active tetromino piece
            (dissoc :active-tetromino-coords)
            ;; update base coords to include tetromino piece
            (assoc :base-coords (merge base-coords (merge-colour-with-coords active-tetromino-coords active-tetromino-colour)))
            ;; set up next tetromino to fall from the sky
            (assoc :active-tetromino-coords (:coords next-tetromino))
            (assoc :active-tetromino-colour (:colour next-tetromino))))
      (assoc playfield :active-tetromino-coords (update-tetromino-position active-tetromino-coords)))))

;; handle clock events
(re-frame/reg-event-db
 ::game-timer
 (fn [db [_ new-time-value]]
   (-> db
       ;; update the clock
       (assoc :elapsed-game-time new-time-value)
       ;; update the playfield
       (assoc-in [:playfield] (update-playfield (get-in db [:playfield]))))))

;; set the ID of the DOM interval timer
;; we'll need this later to clear the state (stop the game)
(re-frame/reg-event-db
 ::set-timer-interval-id
 (fn [db [_ interval-id]]
   (assoc db :timer-interval-id interval-id)))
