(ns tetris.events
  (:require
   [re-frame.core :as rf]
   [tetris.db :as db]
   [re-pressed.core :as rp]
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
                ]})

(defn- translate-tetromino
  "Translate tetromino-coords by [x y], respecting the bounds of the playfield."
  [tetromino-coords [x y]]
  (let [translated-coords (map #(vector (+ x (first %)) (+ y (second %))) tetromino-coords)]
    (if (some #(or (neg? %) (> % 9)) (flatten translated-coords))
      tetromino-coords
      translated-coords)))

(rf/reg-event-db
 ::left-arrow
 (fn [db [_ _ _]]
   (let [{:keys [playfield]} db
         {:keys [active-tetromino-coords]} playfield]
     (assoc-in db [:playfield :active-tetromino-coords] (translate-tetromino active-tetromino-coords [-1 0])))))

(rf/reg-event-db
 ::right-arrow
 (fn [db [_ _ _]]
   (let [{:keys [playfield]} db
         {:keys [active-tetromino-coords]} playfield]
     (assoc-in db [:playfield :active-tetromino-coords] (translate-tetromino active-tetromino-coords [1 0])))))

(rf/reg-event-db
 ::down-arrow
 (fn [db [_ _ _]]
   (let [{:keys [playfield]} db
         {:keys [active-tetromino-coords]} playfield]
     (assoc-in db [:playfield :active-tetromino-coords] (translate-tetromino active-tetromino-coords [0 1])))))

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
        (set (translate-tetromino tetromino-coords [0 1]))
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
      (assoc playfield :active-tetromino-coords (translate-tetromino active-tetromino-coords [0 1])))))

;; handle clock events
(rf/reg-event-db
 ::game-timer
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

(rf/reg-event-fx
 ::key-down
 (fn [cofx [_ e]]
   (println "KEY PRESSED")))

(rf/reg-event-fx
 ::table-clicked
 (fn [cofx [_ e]]
   (println "TABLE CLICKED")))
