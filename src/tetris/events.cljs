(ns tetris.events
  (:require
   [re-frame.core :as rf]
   [tetris.db :as db]
   [re-pressed.core :as rp]
   ))

(def rotation-matrices
  {:I {[[0 0] [0 0] [0 0] [0 0]]
       [[2 -1] [1 0] [0 1] [-1 2]]
       [[2 -1] [1 0] [0 1] [-1 2]]
       [[0 1] [0 1] [0 1] [0 1]]
       [[0 1] [0 1] [0 1] [0 1]]
       [[1 -1] [0 0] [-1 1] [-2 2]]
       [[1 -1] [0 0] [-1 1] [-2 2]]
       [[0 0] [0 0] [0 0] [0 0]]}
   :J {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [2 0] [0 0] [-1 1]]
       [[1 -1] [2 0] [0 0] [-1 1]]
       [[2 0] [2 2] [0 0] [-2 0]]
       [[2 0] [2 2] [0 0] [-2 0]]
       [[1 1] [0 2] [0 0] [-1 -1]]
       [[1 1] [0 2] [0 0] [-1 -1]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :L {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [-1 1] [0 2]]
       [[1 -1] [0 0] [-1 1] [0 2]]
       [[2 0] [0 0] [-2 0] [-2 2]]
       [[2 0] [0 0] [-2 0] [-2 2]]
       [[1 1] [0 0] [-1 -1] [-2 0]]
       [[1 1] [0 0] [-1 -1] [-2 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :O {
       [[0 0] [0 0] [0 0] [0 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :S {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [1 1] [0 2]]
       [[1 -1] [0 0] [1 1] [0 2]]
       [[2 0] [0 0] [0 2] [-2 2]]
       [[2 0] [0 0] [0 2] [-2 2]]
       [[1 1] [0 0] [-1 1] [-2 0]]
       [[1 1] [0 0] [-1 1] [-2 0]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :T {
       [[0 0] [0 0] [0 0] [0 0]]
       [[1 -1] [0 0] [1 1] [-1 1]]
       [[1 -1] [0 0] [1 1] [-1 1]]
       [[2 0] [0 0] [0 2] [-2 0]]
       [[2 0] [0 0] [0 2] [-2 0]]
       [[1 1] [0 0] [-1 1] [-1 -1]]
       [[1 1] [0 0] [-1 1] [-1 -1]]
       [[0 0] [0 0] [0 0] [0 0]]
       }
   :Z {
       [[0 0] [0 0] [0 0] [0 0]]
       [[2 0] [1 1] [0 0] [-1 1]]
       [[2 0] [1 1] [0 0] [-1 1]]
       [[2 2] [0 2] [0 0] [-2 0]]
       [[2 2] [0 2] [0 0] [-2 0]]
       [[0 2] [-1 1] [0 0] [-1 -1]]
       [[0 2] [-1 1] [0 0] [-1 -1]]
       [[0 0] [0 0] [0 0] [0 0]]
       }})

(defn rotate [coords rotation-matrix]
  (loop [result [(mapv + (first coords) (first rotation-matrix))]
         coords (rest coords)
         rotation-matrix (rest rotation-matrix)]
    (if (empty? coords)
      result
      (recur (conj result (mapv + (first coords) (first rotation-matrix)))
             (rest coords)
             (rest rotation-matrix)))))

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

(defn- translate
  "Translate tetromino-coords by [x y], respecting the bounds of the playfield."
  [tetromino [x y]]
  (let [translated-coords (map #(vector (+ x (first %)) (+ y (second %))) (:coords tetromino))
        rotated-coords    (rotate translated-coords (:rotation-matrix tetromino))]
    (if (or (some #(or (neg? %) (> % 9)) (map #(first %) rotated-coords))
            (some #(> % 15) (map #(second %) rotated-coords)))
      tetromino
      (merge tetromino {:coords translated-coords}))))

(rf/reg-event-db
 ::left-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino] translate [-1 0])))

(rf/reg-event-db
 ::right-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino] translate [1 0])))

(rf/reg-event-db
 ::down-arrow
 (fn [db _]
   (update-in db [:playfield :active-tetromino] translate [0 1])))

(rf/reg-event-db
 ::up-arrow
 (fn [db _]
   (let [active (get-in db [:playfield :active-tetromino])
         next (get-in rotation-matrices [(:tag active) (:rotation-matrix active)])]
     (assoc-in db [:playfield :active-tetromino :rotation-matrix] next))))

(rf/reg-event-fx
 ::initialize
 (fn [_ [_ _]]
   {:fx [[:dispatch [::rp/add-keyboard-event-listener "keydown"]]
         [:dispatch [::rp/set-keydown-rules keydown-rules]]]
    :db db/default-db}))

(defn- contiguous?
  "A set of coords `c1`` is contiguous with another set `c2` if
  on the next tick of the clock the sets would overlap."
  [c1 c2]
  (>=
      (count
       (clojure.set/intersection
        ;; have to manually translate here, to avoid bounds-checking
        (set (map #(vector (first %) (inc (second %))) c1))
        (set (keys c2))))
      1))

(defn- merge-colour-with-coords [coords colour]
  (apply hash-map (interleave coords (repeat colour))))

(defn- update-playfield [playfield]
  (let [active-tetromino (:active-tetromino playfield)
        {:keys [coords colour tag rotation-matrix]} active-tetromino
        rotated-coords (rotate coords rotation-matrix)
        base-coords (:base-coords playfield)]
    (if (contiguous? rotated-coords base-coords)
      (-> playfield
          ;; wipe active tetromino piece
          (dissoc :active-tetromino)
          ;; update base coords to include tetromino piece
          (assoc :base-coords (merge base-coords (merge-colour-with-coords rotated-coords colour)))
          ;; set up next tetromino to fall from the sky
          (assoc :active-tetromino (rand-nth db/tetrominos)))
      (update-in playfield [:active-tetromino] translate [0 1]))))

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

