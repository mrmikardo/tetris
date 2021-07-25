(ns tetris.views
  (:require
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [tetris.subs :as subs]
   ))

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
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     {:class "container"}
     [:div
      {:class ["row" "flex" "justify-center" "mt-20"]}
      [:div
       {:class ["flex" "flex-col" "items-center"]}
       [:h1
        {:class ["mb-2"]}
        "Tetris"]
       [playfield]]]
     ]))
