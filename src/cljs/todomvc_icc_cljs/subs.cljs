(ns todomvc-icc-cljs.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

;; -------------------------------------------------------------------------------------
;; Lightweight Extractors Layer
;;
;; Runs on every db change

(defn extract-showing
  [db _]
  (:showing db))

(defn extract-sorted-todos
  [db _]
  (:todos db))

(reg-sub :showing extract-showing)
(reg-sub :sorted-todos extract-sorted-todos)

;; -------------------------------------------------------------------------------------
;; Materialized Views Layer

(reg-sub
  :todos
  :<- [:sorted-todos]
  (fn [sorted-todos query-v _]
    (vals sorted-todos)))

(reg-sub
  :todos/by-id
  :<-[:todos]
  (fn [todos [_ id]]
    (first (filter #(= (:id %) id) todos))))

(reg-sub
  :todos/visible
  :<- [:todos]
  :<- [:showing]
  (fn [[todos showing] _]
    (let [filter-fn (case showing
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
