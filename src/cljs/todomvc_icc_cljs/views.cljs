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


(defn todo-item
  []
  (let [editing (r/atom false)]
    (fn [{:keys [id done title]}]
      [:li {:class (str (when done "completed ")
                        (when @editing "editing"))}
        [:div.view
          [:input.toggle
            {:type "checkbox"
             :checked done
             :on-change #(>emit [:toggle-done id])}]
          [:label
            {:on-double-click #(reset! editing true)}
            title]
          [:button.destroy
            {:on-click #(>emit [:delete-todo id])}]]
        (when @editing
          [todo-input
            {:class "edit"
             :title title
             :on-save #(if (seq %)
                          (>emit [:save id %])
                          (>emit [:delete-todo id]))
             :on-stop #(reset! editing false)}])])))


(defn task-list
  []
  (let [visible-todos (<subs [:todos/visible])
        all-complete? (<subs [:todos/all-complete?])]
      [:section.main
        [:input#toggle-all.toggle-all
          {:type "checkbox"
           :id "toggle-all"
           :checked all-complete?
           :on-change #(>emit [:complete-all-toggle])}]
        [:label
          {:for "toggle-all"}
          "Mark all as complete"]
        [:ul.todo-list
          (for [todo  visible-todos]
            ^{:key (:id todo)} [todo-item todo])]]))


(defn footer-controls
  []
  (let [[active done] (<subs [:todos/footer-counts])
        showing       (<subs [:showing])
        a-fn          (fn [filter-kw txt]
                        [:a {:class (when (= filter-kw showing) "selected")
                             :href (str "#/" (name filter-kw))} txt])]
    [:footer.footer
     [:span.todo-count
      [:strong active] " " (case active 1 "item" "items") " left"]
     [:ul.filters
      [:li (a-fn :all    "All")]
      [:li (a-fn :active "Active")]
      [:li (a-fn :done   "Completed")]]
     (when (pos? done)
       [:button.clear-completed {:on-click #(>emit [:clear-completed])}
        "Clear completed"])]))


(defn task-entry
  []
  [:header.header
    [:h1 "todos"]
    [todo-input
      {:class "new-todo"
       :placeholder "What needs to be done?"
       :on-save #(when (seq %)
                    (>emit [:add-todo %]))}]])


(defn app
  []
  [:<>
   [:section.todoapp
    [task-entry]
    (when (seq (<subs [:todos]))
      [task-list])
    [footer-controls]]
   [:footer.info
    [:p "Double-click to edit a todo"]]])
