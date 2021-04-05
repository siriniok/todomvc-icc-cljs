(ns todomvc-icc-cljs.db
  (:require [cljs.reader]
            [cljs.spec.alpha :as s]
            [re-frame.core :as re-frame]))


(s/def ::id int?)
(s/def ::title string?)
(s/def ::done boolean?)
(s/def ::todo (s/keys :req-un [::id ::title ::done]))
(s/def ::todos (s/and                                       ;; should use the :kind kw to s/map-of (not supported yet)
                 (s/map-of ::id ::todo)
                 #(instance? PersistentTreeMap %)
                 ))
(s/def ::visibility-filter
  #{:all
    :active
    :done
    })
(s/def ::db (s/keys :req-un [::todos ::visibility-filter]))

(def default-db
  {:todos   (sorted-map)
   :visibility-filter :all})


(def ls-key "todos-reframe")

(defn todos->local-store
  "Puts todos into localStorage"
  [todos]
  (.setItem js/localStorage ls-key (str todos)))


(re-frame/reg-cofx
  :local-store-todos
  (fn [cofx _]
    (assoc cofx :local-store-todos
           (into (sorted-map)
                 (some->> (.getItem js/localStorage ls-key)
                          (cljs.reader/read-string)
                          )))))
