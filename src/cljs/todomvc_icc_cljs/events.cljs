(ns todomvc-icc-cljs.events
  (:require
    [todomvc-icc-cljs.db :refer [default-db todo->local-store list->local-store db->local-store]]
    [re-frame.core :refer [reg-event-db reg-event-fx inject-cofx path after add-post-event-callback dispatch]]
    [day8.re-frame.tracing :refer-macros [fn-traced]]
    [cljs.spec.alpha :as s]))


;; -- Helpers -----------------------------------------------------------------

(defn get-last-id [domain]
  (last (keys (domain :by-id))))


(defn allocate-next-id
  "Returns the next domain entity id.
  Assumes domain entities are sorted.
  Returns one more than the current largest id."
  [domain]
  ((fnil inc -1) (get-last-id domain)))


;; -- Interceptors --------------------------------------------------------------

(defn check-and-throw
  "Throws an exception if `db` doesn't match the Spec `a-spec`."
  [a-spec db]
  (when-not (s/valid? a-spec db)
    (throw (ex-info (str "spec check failed: " (s/explain-str a-spec db)) {}))))

(def check-spec-interceptor (after (partial check-and-throw :todomvc-icc-cljs.db/db)))

(def todo->local-store-interceptor (after todo->local-store))
(def list->local-store-interceptor (after list->local-store))
(def db->local-store-interceptor (after db->local-store))

(def todo-interceptors [check-spec-interceptor
                        (path :todo)
                        todo->local-store-interceptor])

(def list-interceptors [check-spec-interceptor
                        (path :list)
                        list->local-store-interceptor])

(def db-interceptors [check-spec-interceptor
                      db->local-store-interceptor])

;; -- Event Handlers ----------------------------------------------------------

(reg-event-fx
  ::initialize-db
  [(inject-cofx :local-store-todo)
   (inject-cofx :local-store-list)
   check-spec-interceptor]
  (fn-traced [{:keys [db local-store-todo local-store-list]} _]
             {:db (assoc default-db :todo local-store-todo :list local-store-list)}))


(reg-event-db
  :todo/add
  todo-interceptors
  (fn [todo [_ text]]
    (let [id (allocate-next-id todo)]
      (-> todo
          (assoc-in [:by-id id] {:id id :title text :completed false})
          (update-in [:by-order] conj id)))))


(reg-event-db
  :todo/toggle-completed
  todo-interceptors
  (fn [todo [_ id]]
    (update-in todo [:by-id id :completed] not)))


(reg-event-db
  :todo/edit
  todo-interceptors
  (fn-traced  [todo [_ id title]]
             (assoc-in todo [:by-id id :title] title)))


(reg-event-db
  :todo/delete
  todo-interceptors
  (fn [todo [_ id]]
    (-> todo
        (update :by-id dissoc id)
        (update :by-order
                (partial filterv (complement #{id}))))))


(reg-event-db
  :list/add-to
  list-interceptors
  (fn [list [_ id todo-id]]
    (update-in list [:by-id id :tasks] conj todo-id)))


(reg-event-db
  :list/delete-from
  list-interceptors
  (fn [list [_ id todo-id]]
    (update-in list [:by-id id :tasks]
               (partial filterv (complement #{todo-id})))))


(reg-event-fx
  :todo->list/add-to
  db-interceptors
  (fn [{:keys [db]} [_ id]]
    (let [todo-id (get-last-id (:todo db))]
      { :fx [[:dispatch [:list/add-to id todo-id]]] })))


(reg-event-fx
  :todo->list/delete-from
  db-interceptors
  (fn [{:keys [db]} [_ todo-id]]
    (let [events
          (mapv (fn [%] [:dispatch [:list/delete-from % todo-id]])
                (-> db :list :by-order))]
      {:fx events})))


(add-post-event-callback
  :dependent-events-dispatcher
  (fn [[name & args]]
    (when (= name :todo/delete)
      (dispatch (into [:todo->list/delete-from] args)))))


(reg-event-fx
  :list->todo/clear-completed
  db-interceptors
  (fn [{:keys [db]} [_ id]]
    (let [completed-ids (->> (get-in db [:list :by-id id :tasks ])
                             (map #(get-in db [:todo :by-id %]))
                             (filter :completed)
                             (map :id))
          events (mapv (fn [%] [:dispatch [:todo/delete %]]) completed-ids)]
      {:fx events})))


(reg-event-fx
  :list->todo/toggle-all-completed
  db-interceptors
  (fn [{:keys [db]} [_ id]]
    (let [not-completed-ids (->> (get-in db [:list :by-id id :tasks ])
                             (map #(get-in db [:todo :by-id %]))
                             (filter (complement :completed))
                             (map :id))
          events (mapv (fn [%] [:dispatch [:todo/toggle-completed %]])
                       not-completed-ids)]
      {:fx events})))


(reg-event-db
  :visibility-filter/apply
  [check-spec-interceptor (path :visibility-filter)]
  (fn [old-visibility-filter [_ new-visibility-filter]]
    new-visibility-filter))
