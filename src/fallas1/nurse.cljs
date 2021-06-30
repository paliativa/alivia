(ns ^:figwheel-hooks fallas1.nurse
  (:require [goog.dom :as gdom]
            [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]
            [fallas1.rules :as rules]
            [clara.rules
             :refer [insert fire-rules query]]))

(enable-console-print!)

(defonce app-state
  (atom {:page 0
         :session rules/session}))

(defn step-rules [session select]
  (-> session
      (insert select)
      fire-rules))

(defn next-page [_]
  (let [{:keys [select page session]} @app-state]

    (if select
      (let [session (-> session
                        (insert select)
                        fire-rules)
            treatment (query session rules/check-treatment)]
        (swap! app-state
               assoc
               :select nil
               :error-message nil
               :page    (inc page)
               :session session
               :treatment treatment))
      (swap! app-state
             assoc :error-message  "Debe seleccionar una opcion"))))

(defn return-home []
  (swap! app-state
         #(-> %
              (assoc :page 0))))

(defn select-option [mapper v]
  (fn [_]
    (swap! app-state assoc :select (mapper v))))

(defn main []
  (let [{:keys [page error-message treatment]} @app-state
        {:keys [question options insertion]} (when (< page (count rules/questions))
                                               (nth rules/questions page))]
    [:div {:class-name "column container"}
     [:h1 "Diagnosticar"]
     (when question
       [:div {:class-name "column"}
        [:h2 question]
        (for [[k v] (partition 2 options)]
          [:a {:class "option"
               :key k
               :on-click (select-option insertion v)}
           [:input {:name "answer"
                    :type "radio"
                    :on-change (select-option insertion v)}]
           [:span {:class "option-text"} k]])
        [:button
         {:on-click next-page}
         "Siguiente"]])
     (when (and (not question)
                (not treatment))
       [:div {:class-name "column"}
        "No se pudo encontrar un solucion adecuada por favor acuda al médico."
        [:button {:on-click return-home}
         "Re-Iniciar"]])
     (when (seq treatment)
       [:div
        (for [{{:keys [medicine description diagnostic]} :?treatment} treatment]
          [:div {:key diagnostic}
           [:h3 diagnostic]
           [:p description]
           "Combinar:"
           (for [{:keys [options]} medicine]
             [:ol "Opciones"
              (for [option options]
                [:li option])])])])
     (when error-message
       [:div {:class-name "error"}
        error-message])]))

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
