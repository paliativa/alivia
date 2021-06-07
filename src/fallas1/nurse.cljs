(ns ^:figwheel-hooks fallas1.nurse
  (:require [goog.dom :as gdom]
            [reagent.core :as reagent :refer [atom]]
            [reagent.dom :as rdom]
            [fallas1.rules :as rules]
            [clara.rules
             :refer [insert fire-rules]
             :refer-macros [defsession defrule]]))

(enable-console-print!)

(defonce app-state
  (atom {:page 0
         :session rules/session}))

(def questions
  [{:question "¿Siempre es el mismo lugar?"
    :options   ["si, siempre es el mismo lugar." :yes
                "no, el dolor se irradia hacia otras partes" :no]
    :insertion rules/->Zone}
   {:question "¿Que intensidad siente de dolor?"
    :options (into [] (mapcat #(vector (str %) %) (range 1 10)))
    :insertion rules/->Intensity}
   {:question "¿Que caracteristica tiene el dolor?"
    :options ["Opresivo. Dolor en una región. Se siente como que está apretado o hinchado." :opresive
              "Punzante. Dolor muy localizado, se puede indicar con un dedo." :stinging
              "Quemante. " :burning
              "Eléctrico. Se puede sentir el recorrido del dolor." :electric
              "Indefinido. El paciente no sabe explicar, o es difícil explicar porque es mezcla de varios por ejemplo" :undefined]
    :insertion rules/->Characteristic}
   {:question "¿Cual es la duración?"
    :options ["Agudo. Corresponde al tiempo menor que las últimas 2 semanas." :acute
              "Subagudo. De 2 semanas a 3 meses." :subacute
              "Crónico. Más de 3 meses." :chronic]
    :insertion rules/->Duration}
   {:question "¿Siempre duele igual o a veces duele más o menos? ."
    :options ["constante" :constant
              "intermitente" :intermittent
              "reacciona a un estímulo" :triggered]
    :insertion rules/->Constancy}
   {:question "¿Que estimulo dispara el dolor?"
    :options ["Posicional. ¿La persona puede acostarse o no, por ejemplo?" :position
              "Movilización." :mobilization
              "Al realizar fuerza." :strength
              "Al aumentar la actividad física (correr, caminar, etc.)" :activity
              "Expectoración (al toser)" :expectoration
              "Reflejo nauseoso / vómito." :reflex
              "Iluminación." :ligthing
              "Sonidos fuertes" :loudness
              "Infusión de cierta medicación endovenosa." :intrafusion-infusion]
    :insertion rules/->Stimulus}])

(defn next-page [_]
  (let [{:keys [select page session]} @app-state]
    (if select
      (swap! app-state
             assoc
             :select nil
             :error-message nil
             :page (inc page)
             :session (-> session
                          (insert select)
                          fire-rules))
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
  (let [{:keys [page error-message]} @app-state
        {:keys [question options insertion]} (when (< page (count questions))
                                               (nth questions page))]
    [:div {:class-name "container"}
     (if question
       [:div {:class-name "column"}
        question
        (for [[k v] (partition 2 options)]
          [:span {:key k}
           [:input {:name "answer"
                    :type "radio"
                    :on-change (select-option insertion v)}]
           k])
        [:button
         {:on-click next-page}
         "Siguiente"]]
       [:div {:class-name "column"}
        "No se pudo encontrar un solucion adecuada por favor acuda al médico."
        [:button {:on-click return-home}
         "Re-Iniciar"]])
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
