(ns todomvc-icc-cljs.views
  (:require
    [reagent.core :as r]
    [re-frame.core :as re-frame :refer [subscribe dispatch]]
    [clojure.string :as str]
    ))

(def <subs (comp deref subscribe))
(def >emit dispatch)

(defn todo-input [{:keys [title on-save on-stop]}]
  (let [val  (r/atom title)
        stop #(do (reset! val "")
                  (when on-stop (on-stop)))
        save #(let [v (-> @val str str/trim)]
                (on-save v)
                (stop))]
    (fn [props]
      [:input (merge (dissoc props :on-save :on-stop :title)
                     {:type        "text"
                      :value       @val
                      :auto-focus  true
                      :on-blur     save
                      :on-change   #(reset! val (-> % .-target .-value))
                      :on-key-down #(case (.-which %)
                                      13 (save)
                                      27 (stop)
                                      nil)})])))


(defn todo-input-new [props]
  [todo-input (merge (dissoc props :list-id)
                     {:class "new-todo"
                      :placeholder "What needs to be done?"
                      :on-save #(when (seq %)
                                  (>emit [:todo/add %])
                                  (>emit [:todo->list/add-to (props :list-id)]))})])


(defn todo-input-edit [props]
  (let [id (props :id)]
    [todo-input (merge props
                       {:class "edit"
                        :on-save #(if (seq %)
                                    (>emit [:todo/edit id %])
                                    (>emit [:todo/delete id]))})]))


(defn toggle-todo-checkbox [id]
  (let [{ :keys [completed] } (<subs [:todo id])]
    [:input.toggle
     {:type "checkbox"
      :checked completed
      :on-change #(>emit [:todo/toggle-completed id])}]))


(defn delete-todo-button [id]
  [:button.destroy
   {:on-click #(>emit [:todo/delete id])}])


(defn todo-item []
  (let [editing (r/atom false)]
    (fn [id left-component right-component]
      (let [{:keys [id completed title]} (<subs [:todo id])]
        [:li {:class (str (when completed "completed ")
                          (when @editing "editing"))}
         [:div.view
          (when left-component
            [left-component id])
          [:label
           {:on-double-click #(reset! editing true)}
           title]
          (when right-component
            [right-component id])]
         (when @editing
           [todo-input-edit
            {:id id
             :title title
             :on-stop #(reset! editing false)}])]))))


(defn main-section [id]
  (let [visible-todos (<subs [:list/visible-todos id])
        all-complete? (<subs [:list/all-complete? id])]
    [:section.main
     [:input#toggle-all.toggle-all
      {:type "checkbox"
       :id "toggle-all"
       :checked all-complete?
       :on-change #(>emit [:list->todo/toggle-all-completed id])}]
     [:label
      {:for "toggle-all"}
      "Mark all as complete"]
     [:ul.todo-list
      (for [todo-id visible-todos]
        ^{:key todo-id} [todo-item
                         todo-id
                         toggle-todo-checkbox
                         delete-todo-button])]]))


(defn footer-controls [id]
  (let [[active completed] (<subs [:list/counts id])
        visibility-filter       (<subs [:visibility-filter])
        a-fn          (fn [filter-kw txt]
                        [:a {:class (when (= filter-kw visibility-filter) "selected")
                             :href (str "#/" (name filter-kw))} txt])]
    [:footer.footer
     [:span.todo-count
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul.filters
      [:li (a-fn :all    "All")]
      [:li (a-fn :active "Active")]
      [:li (a-fn :completed   "Completed")]]
     (when (pos? completed)
       [:button.clear-completed {:on-click #(>emit [:list->todo/clear-completed id])}
        "Clear completed"])]))


(defn header [id]
  (let [{:keys [title]} (<subs [:list id])]
    [:header.header
     [:h1 title]
     [todo-input-new
      { :placeholder "What needs to be done?" :list-id id} ]]))


(defn todo-list [id]
  [:<>
   [:section.todoapp
    [header id]
    (when (seq (<subs [:list id]))
      [main-section id])
    [footer-controls id]]
   [:footer.info
    [:p "Double-click to edit a todo"]]])


(defn app []
  [:main.app [todo-list 0]])
