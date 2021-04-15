(ns todomvc-icc-cljs.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))

;; Sample data for inferring the spec
;;
;; (require '[spec-provider.provider :as sp])
;;
;; (sp/pprint-specs (sp/infer-specs
;;   [{
;;     :by-id {
;;      0 { :id 0 :title "Task 1" :completed false }
;;      1 { :id 1 :title "Task 2" :completed false }}
;;     :by-order [0 1]
;;     }]
;;   :db/todo) 'db 's)
;;
;; (sp/pprint-specs (sp/infer-specs
;;   [{
;;     :by-id {
;;      0 { :id 0 :title "List 1" :tasks [0 1] }
;;      1 { :id 1 :title "List 2" :tasks [0 1] }}
;;     :by-order [0 1]
;;     }]
;;   :db/list) 'db 's)

(s/def ::id integer?)
(s/def ::title string?)
;; ids in any arbitrary order
(s/def ::by-order (s/coll-of integer? :kind vector? :distinct true))

(s/def ::completed boolean?)
(s/def ::todo-item (s/keys :req-un [::id ::completed ::title]))
(s/def :todo/by-id (s/and sorted? (s/map-of ::id ::todo-item)))

(s/def ::tasks (s/coll-of integer? :kind vector? :distinct true))
(s/def ::list-item (s/keys :req-un [::id ::tasks ::title]))
(s/def :list/by-id (s/and sorted? (s/map-of ::id ::list-item)))

(s/def ::todo (s/keys :req-un [:todo/by-id ::by-order]))
(s/def ::list (s/keys :req-un [:list/by-id ::by-order]))
(s/def ::visibility-filter
  #{:all
    :active
    :completed
    })

(s/def ::db (s/keys :req-un [::todo ::list ::visibility-filter]))

(def default-db
  {:todo {:by-id (sorted-map) :by-order []}
   :list {:by-id (sorted-map 0 {:id 0 :title "todos" :tasks []})
          :by-order [0]}
   :visibility-filter :all})


;; Local Storage Sync

(def ls-todo-key "todo-reframe")
(def ls-list-key "list-reframe")

(defn todo->local-store
  "Puts todo into localStorage"
  [todo]
  (.setItem js/localStorage ls-todo-key (str todo)))

(defn list->local-store
  "Puts list into localStorage"
  [list]
  (.setItem js/localStorage ls-list-key (str list)))

(defn db->local-store [db]
  (todo->local-store (db :todo))
  (list->local-store (db :list)))

(defn load-domain [cofx cofx-key domain ls-key]
  (let [stored-value (some->> (.getItem js/localStorage ls-key)
                              (cljs.reader/read-string))]
    (assoc cofx cofx-key
           {:by-id (into (-> default-db domain :by-id) (get stored-value :by-id))
            :by-order (if (seq (get stored-value :by-order))
                        (get stored-value :by-order)
                        (-> default-db domain :by-order)) })))

(defn load-todo [cofx _]
  (load-domain cofx :local-store-todo :todo ls-todo-key))

(defn load-list [cofx _]
  (load-domain cofx :local-store-list :list ls-list-key))

(re-frame/reg-cofx :local-store-todo load-todo)
(re-frame/reg-cofx :local-store-list load-list)
