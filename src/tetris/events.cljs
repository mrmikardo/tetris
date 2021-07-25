(ns tetris.events
  (:require
   [re-frame.core :as re-frame]
   [tetris.db :as db]
   ))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::game-timer
 (fn [db [_ new-time-value]]
   (assoc db :elapsed-game-time new-time-value)))

(re-frame/reg-event-db
 ::set-timer-interval-id
 (fn [db [_ interval-id]]
   (assoc db :timer-interval-id interval-id)))
