(ns tetris.views
  (:require
   [re-frame.core :as rf]
   [reagent.core :as r]
   [tetris.subs :as subs]
   [tetris.events :as events]))

(defn- dispatch-time-update []
  (let [elapsed-time @(rf/subscribe [::subs/elapsed-game-time])]
    (rf/dispatch [::events/start-game-timer (inc elapsed-time)])))

(defn- start-game-timer! []
  (let [interval-id (js/setInterval dispatch-time-update 1000)]
    (rf/dispatch [::events/set-timer-interval-id interval-id])))

(defn- stop-game-timer! []
  (let [interval-id @(rf/subscribe [::subs/timer-interval-id])]
    (js/clearInterval interval-id)))

(defonce do-timer (start-game-timer!))

(defn- game-timer []
  (let [elapsed-time @(rf/subscribe [::subs/elapsed-game-time])]
    [:h1
     elapsed-time]))

(defn- timer-start-button []
  [:button {:class ["bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"]
            :on-click start-game-timer!}
   "Start timer"])

(defn- timer-stop-button []
  [:button {:class ["bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded"]
            :on-click stop-game-timer!}
   "Stop timer"])

;; TODO move this to utils
(defn- coord-in?
  "Is coord in coords?"
  [coord coords]
  (> (count (clojure.set/intersection (set [coord]) (set coords))) 0))

(defn playfield []
  (let [rows-count 16
        cols-count 10
        cell-size  35
        {:keys [:active-tetromino-colour :base-coords]} @(rf/subscribe [::subs/playfield])
        active-tetromino-coords @(rf/subscribe [::subs/active-tetromino-coords])]
    [:div {:class ["relative"]}
     [:table
      {:class ["table-fixed border-2"]
       :style {:width (* cell-size cols-count)}}
      [:tbody
       (for [i (range rows-count)]
         ^{:key i}
         [:tr
          {:style {:height cell-size}}
          (for [j (range cols-count)]
            ^{:key j}
            [:td
             {:style {:background-color (cond
                                          (coord-in? [j i] active-tetromino-coords) active-tetromino-colour
                                          (coord-in? [j i] (keys base-coords)) (get base-coords [j i])
                                          :else "#ffffff")}
              :class ["p-0 border border-purple-400 text-center select-none"]}])])]]]))

(defn main-panel []
  [:div
   {:class "container"}
   [:div
    {:class ["row" "flex" "justify-center" "mt-20"]}
    [timer-start-button]
    [timer-stop-button]
    [:div
     {:class ["flex" "flex-col" "items-center"]}
     [:h1
      {:class ["mb-2"]}
      "Tetris"]
     [game-timer]
     [playfield]]]
   ])
