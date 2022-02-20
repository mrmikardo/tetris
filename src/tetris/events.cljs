(ns tetris.events
  (:require
   [re-frame.core :as rf]
   [tetris.db :as db]
   [re-pressed.core :as rp]
   ))


;; the different ways each tetromino can be rotated
;; a tetromino's position is represented by a vector of coords (see `tetrominos` above)
;; + a rotation matrix represented by the coords below. The first set of coords in each
;; vector this will always be a set of 0s, as it represents the 'canonical' un-rotated
;; variant of a tetromino. The remaining vectors represent the block rotated 90 degrees
;; in a given direction.
(def tag-to-rotations
  {:I [
       [[0 0] [0 0] [0 0] [0 0]]
       [[2 -1] [1 0] [0 1] [-1 2]]
       [[0 1] [0 1] [0 1] [0 1]]
       [[1 -1] [0 0] [-1 1] [-2 2]]
       ]
   :J [
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [2 0] [0 0] [-1 1]]
       [[2 0] [2 2] [0 0] [-2 0]]
       [[1 1] [0 2] [0 0] [-1 -1]]
       ]
   :L [
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [-1 1] [0 2]]
       [[2 0] [0 0] [-2 0] [-2 2]]
       [[1 1] [0 0] [-1 -1] [-2 0]]
       ]
   :O [
       [[0 0] [0 0] [0 0] [0 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       ]
   :S [
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [1 1] [0 2]]
       [[2 0] [0 0] [0 2] [-2 2]]
       [[1 1] [0 0] [-1 1] [-2 0]]
       ]
   :T [
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [1 1] [-1 1]]
       [[2 0] [0 0] [0 2] [-2 0]]
       [[1 1] [0 0] [-1 1] [-1 -1]]
       ]
   :Z [
       [[0 0] [0 0] [0 0] [0 0]]
       [[2 0] [1 1] [0 0] [-1 1]]
       [[2 2] [0 2] [0 0] [-2 0]]
       [[0 2] [-1 1] [0 0] [-1 -1]]
       ]})

(defn get-rotation-matrix-by-id-and-tag [id tag]
  (get-in tag-to-rotations [tag id]))

(defn apply-rotation-matrix [base-coords rotation-matrix]
  (loop [result          [(mapv + (first base-coords) (first rotation-matrix))]
         base-coords     (rest base-coords)
         rotation-matrix (rest rotation-matrix)]
    (if (empty? base-coords)
      result
      (recur (conj result (mapv + (first base-coords) (first rotation-matrix)))
             (rest base-coords)
             (rest rotation-matrix)))))

(def default-keydown-rules
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

(defn- translate-tetromino
  "Translate tetromino-coords by [x y], respecting the bounds of the playfield."
  [tetromino-coords [x y] rotation-matrix]
  (let [translated-coords (map #(vector (+ x (first %)) (+ y (second %))) tetromino-coords)
        rotated-coords    (apply-rotation-matrix translated-coords (get-rotation-matrix-by-id-and-tag :I rotation-matrix))]
    (if (or (some #(or (neg? %) (> % 9)) (map #(first %) rotated-coords))
            (some #(> % 15) (map #(second %) rotated-coords)))
      tetromino-coords
      translated-coords)))

(rf/reg-event-db
 ::left-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino-base-coords] translate-tetromino [-1 0] (get-in db [:playfield :active-tetromino-rotation-matrix-id]))))

(rf/reg-event-db
 ::right-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino-base-coords] translate-tetromino [1 0] (get-in db [:playfield :active-tetromino-rotation-matrix-id]))))

(rf/reg-event-db
 ::down-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino-base-coords] translate-tetromino [0 1] (get-in db [:playfield :active-tetromino-rotation-matrix-id]))))

(rf/reg-event-db
 ::up-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino-rotation-matrix-id] #(mod (inc %) 4))))

(rf/reg-event-fx
 ::initialize
 (fn [_ [_ _]]
   {:fx [[:dispatch [::rp/add-keyboard-event-listener "keydown"]]
         [:dispatch [::rp/set-keydown-rules default-keydown-rules]]]
    :db db/default-db}))

(defn- contiguous-with-base?
  "A tetromino is contiguous with the base if on the next tick of the clock
  its coords would overlap with the base coords."
  [tetromino-coords base-coords]
  (>=
      (count
       (clojure.set/intersection
        ;; have to manually translate here, to avoid bounds-checking
        (set (map #(vector (first %) (+ 1 (second %))) tetromino-coords))
        (set base-coords)))
      1))

(defn- merge-colour-with-coords [coords colour]
  (apply hash-map (interleave coords (repeat colour))))

(defn- update-playfield [playfield]
  (let [{:keys [active-tetromino-coords active-tetromino-colour base-coords]} playfield]
    (if (contiguous-with-base? active-tetromino-coords (keys base-coords))
      (let [next-tetromino (rand-nth db/tetrominos)]
        (-> playfield
            ;; wipe active tetromino piece
            (dissoc :active-tetromino-coords)
            ;; update base coords to include tetromino piece
            (assoc :base-coords (merge base-coords (merge-colour-with-coords active-tetromino-coords active-tetromino-colour)))
            ;; set up next tetromino to fall from the sky
            ;; TODO pull this out to helper
            (assoc :active-tetromino-base-coords (:coords next-tetromino))
            (assoc :active-tetromino-colour (:colour next-tetromino))
            (assoc :active-tetromino-tag (:tag next-tetromino))
            (assoc :active-tetromino-rotation-matrix-id 0)))
      (update-in playfield [:active-tetromino-base-coords] translate-tetromino [0 1] (:active-tetromino-rotation-matrix-id playfield)))))

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

