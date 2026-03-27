(ns conao3.casana.output
  (:require
   [cheshire.core :as json]
   [clojure.string :as str])
  (:import
   (com.fasterxml.jackson.core.util
    DefaultIndenter
    DefaultPrettyPrinter)))

(set! *warn-on-reflection* true)


(defn- cell-str
  [v]
  (cond
    (map? v) (or (:name v) (str v))
    (nil? v) "-"
    :else (str v)))


(defn- col-widths
  [headers rows]
  (map (fn [h]
         (reduce (fn [w row]
                   (max w (count (cell-str (get row h)))))
                 (count (name h))
                 rows))
       headers))


(defn- format-row
  [values widths]
  (->> (map (fn [v w] (format (str "%-" w "s") v)) values widths)
       (str/join "  ")))


(defn- rows-seq
  [data]
  (if (sequential? data) data [data]))


(defn print-table
  [headers data]
  (let [rows (rows-seq data)
        widths (col-widths headers rows)]
    (println (format-row (map #(str/upper-case (name %)) headers) widths))
    (doseq [row rows]
      (println (format-row (map #(cell-str (get row %)) headers) widths)))))


(defn print-text
  [headers data]
  (doseq [row (rows-seq data)]
    (println (str/join "\t" (map #(cell-str (get row %)) headers)))))


(def ^:private pretty-printer
  (let [nl DefaultIndenter/SYSTEM_LINEFEED_INSTANCE
        pp (.withoutSpacesInObjectEntries (DefaultPrettyPrinter.))]
    (doto pp
      (.indentArraysWith nl)
      (.indentObjectsWith nl))))


(defn display
  [fmt headers data]
  (case fmt
    :json (println (json/generate-string data {:pretty pretty-printer}))
    :text (print-text headers data)
    (print-table headers data)))
