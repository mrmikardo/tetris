(ns tetris.events
  (:require
   [re-frame.core :as re-frame]
   [tetris.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(defn- update-active-tetronomion
  "Move the currently falling tetronomion one coord closer to the base."
  [coords]
  (map #(vector (first %) (+ 1 (second %))) coords))

(defn- contiguous-with-base?
  "A tetronomion is contiguous with the base if it shares at least a single
  edge with one of the edges of the base (i.e. 2 coords)."
  [tetronomion-coords base-coords]
  (>=
      (count
       (clojure.set/intersection
        (set tetronomion-coords)
        (set base-coords)))
      2))

(defn- update-playfield [playfield]
  (let [{:keys [active-tetronomion-coords base-coords]} playfield]
    (if (contiguous-with-base? active-tetronomion-coords base-coords)
      (-> playfield
        ;; wipe active tetronomion piece
        (dissoc :active-tetronomion-coords)
        ;; update base coords to include tetronomion piece
        (assoc :base-coords (concat base-coords active-tetronomion-coords))
        ;; TODO set up next tetronomion to fall from the sky...
        )
      (assoc playfield :active-tetronomion-coords (update-active-tetronomion active-tetronomion-coords)))))

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
