(ns tetris.subs
  (:require
   [re-frame.core :as rf]
   [tetris.events :as events]))

(rf/reg-sub
 ::elapsed-game-time
 (fn [db]
   (:elapsed-game-time db)))

(rf/reg-sub
 ::active-tetromino
 (fn [db]
   (get-in db [:playfield :active-tetromino])))

(rf/reg-sub
 ::base-coords
 (fn [db]
   (get-in db [:playfield :base-coords])))

(rf/reg-sub
 ::timer-interval-id
 (fn [db]
   (:timer-interval-id db)))
