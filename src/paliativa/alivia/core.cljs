(ns ^:figwheel-hooks paliativa.alivia.core
  (:require [goog.dom :as gdom]
            [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]
            [paliativa.alivia.rules :as rules]
            [clara.rules
             :refer [insert fire-rules query]]))

(enable-console-print!)

(defonce app-state
  (atom {:page 0
         :session rules/session}))

(defn next-page [selected]
  (let [{:keys [page session]} @app-state
        session (if selected
                  (-> session
                      (insert selected)
                      fire-rules)
                  session)
        treatments (query session rules/check-treatment)]
    (swap! app-state
           assoc
           :page    (inc page)
           :session session
           :treatments treatments)))

(defn return-home []
  (swap! app-state
         #(-> %
              (assoc :page 0))))

(defn select-option [mapper v]
  (fn [_]
    (swap! app-state
           assoc :select (mapper v))))

(defn header []
  [:header
   [:h1 {:class-name "row"}
    [:img {:src "img/saline.png"
           :width "35em"}]
    "Alivia"
    [:img {:src "img/pills.png"
           :width "35em"}]]])

(defn conclusion [treatments done?]
  (cond
    (seq treatments)
    [:div
     (for [{{:keys [medicine description diagnostic]} :?treatment} treatments]
       [:details {:key diagnostic}
        [:summary diagnostic]
        [:div
         [:p description]
         [:p "Combinar:"
          (for [{:keys [options]} medicine]
            [:ol "Opciones"
             (for [option options]
               [:li option])])]]])]

    done?
    [:div {:class-name "column"}
     "No se pudo encontrar un solucion adecuada por favor acuda al médico."
     [:button {:on-click return-home}
      "Re-Iniciar"]]))

(defn main []
  (let [{:keys [page treatments]} @app-state
        {:keys [question options insertion]} (when (< page (count rules/questions))
                                               (nth rules/questions page))]
    [:div {:class-name "container"}
     [header]
     [conclusion treatments (nil? question)]
     (when question
       [:div {:class-name "column"}
        [:h2 question]
        (for [[k v] (partition 2 options)]
          [:button {:class "option"
                    :key k
                    :on-click #(next-page (insertion v))}
           k])
        [:button
         {:on-click #(next-page nil)}
         "Siguiente"]])]))

(defn mount [el]
  (rdom/render [main] el))

(defn get-app-element []
  (gdom/getElement "app"))

(defn mount-app-element []
  (when-let [el (get-app-element)]
    (mount el)))

(mount-app-element)

(defn ^:after-load on-reload []
  (mount-app-element))
