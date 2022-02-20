(ns tetris.subs
  (:require
   [re-frame.core :as rf]
   [tetris.events :as events]))

(rf/reg-sub
 ::elapsed-game-time
 (fn [db]
   (:elapsed-game-time db)))

(rf/reg-sub
 ::playfield
 (fn [db]
   (:playfield db)))

(rf/reg-sub
 ::active-tetromino-coords
 (fn [{:keys [:playfield]}]
   (let [rotation-matrix (events/get-rotation-matrix-by-id-and-tag
                          (:active-tetromino-rotation-matrix-id playfield)
                                 (:active-tetromino-tag playfield))
         active-coords    (events/apply-rotation-matrix (:active-tetromino-base-coords playfield) rotation-matrix)]
     (println active-coords)
     active-coords)))

(rf/reg-sub
 ::timer-interval-id
 (fn [db]
   (:timer-interval-id db)))
