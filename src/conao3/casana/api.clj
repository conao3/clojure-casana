(ns conao3.casana.api
  (:require
   [cheshire.core :as json]
   [clj-http.client :as http]))


(def ^:private base-url "https://app.asana.com/api/1.0")


(defn- auth-opts
  [token]
  {:headers {"Authorization" (str "Bearer " token)}
   :as :json})


(defn get!
  [config path]
  (-> (http/get (str base-url path) (auth-opts (:access-token config)))
      :body
      :data))


(defn post!
  [config path body]
  (-> (http/post (str base-url path)
                 (assoc (auth-opts (:access-token config))
                        :content-type :json
                        :body (json/generate-string {:data body})))
      :body
      :data))


(defn put!
  [config path body]
  (-> (http/put (str base-url path)
                (assoc (auth-opts (:access-token config))
                       :content-type :json
                       :body (json/generate-string {:data body})))
      :body
      :data))


(defn delete!
  [config path]
  (-> (http/delete (str base-url path) (auth-opts (:access-token config)))
      :body
      :data))


(defn post-form!
  [config path params]
  (-> (http/post (str base-url path)
                 (assoc (auth-opts (:access-token config))
                        :multipart (mapv (fn [[k v]] {:name (name k) :content (str v)}) params)))
      :body
      :data))
