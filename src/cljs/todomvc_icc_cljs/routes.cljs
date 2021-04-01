(ns todomvc-icc-cljs.routes
  (:require-macros [secretary.core :refer [defroute]])
  (:require [re-frame.core :as rf :refer [dispatch dispatch-sync]]
            [secretary.core :as secretary]
            [goog.events :as gevents]
            [goog.history.EventType :as EventType])
  (:import
    [goog History]
    [goog.history EventType]))

(defroute "/" [] (dispatch [:set-showing :all]))
(defroute "/:filter" [filter] (dispatch [:set-showing (keyword filter)]))

(defonce history
  (doto (History.)
    (gevents/listen EventType.NAVIGATE
                   (fn [^js/goog.History.Event event] (secretary/dispatch! (.-token event))))
    (.setEnabled true)))
