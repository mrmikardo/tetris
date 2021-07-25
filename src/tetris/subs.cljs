(ns tetris.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::elapsed-game-time
 (fn [db]
   (:elapsed-game-time db)))
