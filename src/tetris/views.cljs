(ns tetris.views
  (:require
   [re-frame.core :as re-frame]
   [tetris.subs :as subs]
   ))

;; this is a Reagent "form 1" component
(defn- demo-button []
  [:button
   {:type "button"
    :title "To be clicked"
    :on-click #(js/console.log "He clicked me! :O")}
   "Click me :)"])

(defn main-panel []
  (let [name (re-frame/subscribe [::subs/name])]
    [:div
     [:h1
      "Hello from " @name]
     [demo-button]
     ]))
