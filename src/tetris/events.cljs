(ns tetris.events
  (:require
   [re-frame.core :as rf]
   [re-pressed.core :as rp]
   [tetris.db :as db]
   [tetris.geometry :as geom]
   ))

(def keydown-rules
  {:event-keys [
                [
                 [::left-arrow]
                 [{:keyCode 37}]  ;; LEFT
                 ]
                [
                 [::right-arrow]
                 [{:keyCode 39}]  ;; RIGHT
                 ]
                [
                 [::down-arrow]
                 [{:keyCode 40}]  ;; DOWN
                 ]
                [
                 [::up-arrow]
                 [{:keyCode 38}]  ;; UP
                 ]
                ]})



(rf/reg-event-db
 ::left-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino] geom/translate [-1 0])))

(rf/reg-event-db
 ::right-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino] geom/translate [1 0])))

(rf/reg-event-db
 ::down-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino] geom/translate [0 1])))

(rf/reg-event-db
 ::up-arrow
 (fn [db _]
   (let [active (get-in db [:playfield :active-tetromino])
         next (get-in geom/rotation-matrices [(:tag active) (:rotation-matrix active)])]
     (assoc-in db [:playfield :active-tetromino :rotation-matrix] next))))

(rf/reg-event-fx
 ::initialize
 (fn [_ [_ _]]
   {:fx [[:dispatch [::rp/add-keyboard-event-listener "keydown"]]
         [:dispatch [::rp/set-keydown-rules keydown-rules]]]
    :db db/default-db}))

(defn- merge-colour-with-coords [coords colour]
  (apply hash-map (interleave coords (repeat colour))))

(defn- update-playfield [playfield]
  (let [active-tetromino (:active-tetromino playfield)
        {:keys [coords colour tag rotation-matrix]} active-tetromino
        rotated-coords (geom/rotate coords rotation-matrix)
        base-coords (:base-coords playfield)]
    (if (geom/contiguous? rotated-coords base-coords)
      (-> playfield
          ;; wipe active tetromino piece
          (dissoc :active-tetromino)
          ;; update base coords to include tetromino piece
          (assoc :base-coords (merge base-coords (merge-colour-with-coords rotated-coords colour)))
          ;; set up next tetromino to fall from the sky
          (assoc :active-tetromino (rand-nth db/tetrominos)))
      (update-in playfield [:active-tetromino] geom/translate [0 1]))))

(rf/reg-event-db
 ::start-game-timer
 (fn [db [_ new-time-value]]
   (-> db
       ;; update the clock
       (assoc :elapsed-game-time new-time-value)
       ;; update the playfield
       (assoc-in [:playfield] (update-playfield (get-in db [:playfield]))))))

;; set the ID of the DOM interval timer
;; we'll need this later to clear the state (stop the game)
(rf/reg-event-db
 ::set-timer-interval-id
 (fn [db [_ interval-id]]
   (assoc db :timer-interval-id interval-id)))

