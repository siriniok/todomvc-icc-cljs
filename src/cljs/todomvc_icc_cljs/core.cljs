(ns todomvc-icc-cljs.core
  (:require
    [reagent.dom :as rdom]
    [re-frame.core :as rf :refer [dispatch dispatch-sync]]
    [todomvc-icc-cljs.routes :as routes]
    [todomvc-icc-cljs.events :as events]
    [todomvc-icc-cljs.subs :as subs]
    [todomvc-icc-cljs.views :as views]
    [todomvc-icc-cljs.config :as config])
  )

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (rf/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/app] root-el)))

(defn ^:export init []
  (dispatch-sync [::events/initialize-db])
  (dev-setup)
  (mount-root))
