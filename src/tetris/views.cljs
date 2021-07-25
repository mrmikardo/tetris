(ns tetris.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [tetris.subs :as subs]
   [tetris.events :as events]))

(defn- dispatch-time-update []
  (let [elapsed-time @(rf/subscribe [::subs/elapsed-game-time])]
    (rf/dispatch [::events/game-timer (inc elapsed-time)])))

(defn- start-game-timer! []
  (let [interval-id (js/setInterval dispatch-time-update 1000)]
    (rf/dispatch [::events/set-timer-interval-id interval-id])))

(defonce do-timer (start-game-timer!))

(defn- game-timer []
  (let [elapsed-time @(rf/subscribe [::subs/elapsed-game-time])]
    [:h1
     elapsed-time]))

(defn playfield []
  (let [rows-count 16
        cols-count 10
        cell-size  35]
    [:div {:class ["relative"]}
     [:table
      {:class ["table-fixed border-2 border-purple-700"]
       :style {:width (* cell-size cols-count)}}
      [:tbody
       (for [i (range rows-count)]
         ^{:key i}
         [:tr
          {:style {:height cell-size}}
          (for [j (range cols-count)]
            ^{:key j}
            [:td
             {:class ["p-0 border border-purple-400 text-center select-none"]}])])]]]))

(defn main-panel []
  [:div
   {:class "container"}
   [:div
    {:class ["row" "flex" "justify-center" "mt-20"]}
    [:div
     {:class ["flex" "flex-col" "items-center"]}
     [:h1
      {:class ["mb-2"]}
      "Tetris"]
     [game-timer]
     [playfield]]]
   ])
