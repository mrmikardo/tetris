(ns tetris.subs
  (:require
   [re-frame.core :as rf]))

(rf/reg-sub
 ::elapsed-game-time
 (fn [db]
   (:elapsed-game-time db)))

(rf/reg-sub
 ::playfield
 (fn [db]
   (:playfield db)))
