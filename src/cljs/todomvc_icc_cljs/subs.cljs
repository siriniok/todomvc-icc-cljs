(ns todomvc-icc-cljs.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))

;; -------------------------------------------------------------------------------------
;; Lightweight Extractors Layer
;;
;; Runs on every db change

(defn extract-visibility-filter
  [db _]
  (:visibility-filter db))

(defn extract-todo
  [db _]
  (db :todo))

(defn extract-list
  [db _]
  (db :list))

(reg-sub :visibility-filter/index extract-visibility-filter)
(reg-sub :todo/index extract-todo)
(reg-sub :list/index extract-list)

;; -------------------------------------------------------------------------------------
;; Materialized Views Layer

(reg-sub
  :visibility-filter
  :<- [:visibility-filter/index]
  (fn [visibility-filter-index]
    visibility-filter-index))

(reg-sub
  :todo/all
  :<- [:todo/index]
  (fn [todo-index _]
    (todo-index :by-id)))

(reg-sub
  :list/all
  :<- [:list/index]
  (fn [list-index _]
    (list-index :by-id)))

(reg-sub
  :todo
  :<-[:todo/all]
  (fn [todo [_ id]]
    (get todo id)))

(reg-sub
  :list
  :<-[:list/all]
  (fn [list [_ id]]
    (get list id)))

(reg-sub
  :list-todo/all

  (fn [[_ id]]
    [(subscribe [:list id]) (subscribe [:todo/all])])

  (fn [[list todo] _]
    (map #(get todo %) (list :tasks))))

(reg-sub
  :filter-list-todo/all-ids

  (fn [[_ id]]
    [(subscribe [:list-todo/all id])
     (subscribe [:visibility-filter])])

  (fn [[todos visibility-filter] _]
    (let [filter-fn (case visibility-filter
                      :active (complement :completed)
                      :completed   :completed
                      :all    identity)]
      (->> todos
           (filter filter-fn)
           (map #(% :id))))))

(reg-sub
  :list/all-complete?

  (fn [[_ id]]
    [(subscribe [:list-todo/all id])])

  (fn [[todos] _]
    (every? :completed todos)))

(reg-sub
  :list/completed-count

  (fn [[_ id]]
    [(subscribe [:list-todo/all id])])

  (fn [[todos] _]
    (count (filter :completed todos))))

(reg-sub
  :list/counts

  (fn [[_ id]]
    [(subscribe [:list-todo/all id])
     (subscribe [:list/completed-count id])])

  (fn [[todos completed] _]
    [(- (count todos) completed) completed]))
