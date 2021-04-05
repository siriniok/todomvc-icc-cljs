(ns todomvc-icc-cljs.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

;; -------------------------------------------------------------------------------------
;; Lightweight Extractors Layer
;;
;; Runs on every db change

(defn extract-visibility-filter
  [db _]
  (:visibility-filter db))

(defn extract-todos
  [db _]
  (:todos db))

(reg-sub :visibility-filter/index extract-visibility-filter)
(reg-sub :todos/index extract-todos)

;; -------------------------------------------------------------------------------------
;; Materialized Views Layer
;;
(reg-sub
  :visibility-filter
  :<- [:visibility-filter/index]
  (fn [visibility-filter-index]
    visibility-filter-index))

(reg-sub
  :todos
  :<- [:todos/index]
  (fn [todos-index query-v _]
    (vals todos-index)))

(reg-sub
  :todos/by-id
  :<-[:todos]
  (fn [todos [_ id]]
    (first (filter #(= (:id %) id) todos))))

(reg-sub
  :todos/visible
  :<- [:todos]
  :<- [:visibility-filter]
  (fn [[todos visibility-filter] _]
    (let [filter-fn (case visibility-filter
                      :active (complement :done)
                      :done   :done
                      :all    identity)]
      (filter filter-fn todos))))

(reg-sub
  :todos/all-complete?
  :<- [:todos]
  (fn [todos _]
    (every? :done todos)))

(reg-sub
  :todos/completed-count
  :<- [:todos]
  (fn [todos _]
    (count (filter :done todos))))

(reg-sub
  :todos/footer-counts
  :<- [:todos]
  :<- [:todos/completed-count]
  (fn [[todos completed] _]
    [(- (count todos) completed) completed]))
