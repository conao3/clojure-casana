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


(def ^:private github-app-identifier "1200852897934381")


(defn sign-attach!
  [config task-gid pr-url attachment-gid]
  (let [user-gid (:gid (get! config "/users/me"))
        cookie (str "user=" user-gid
                    "; ticket=" (:ticket config)
                    "; auth_token=" (:session-auth-token config))]
    (-> (http/post (str "https://app.asana.com/-/sign_attach_request"
                        "?task=" task-gid
                        "&appIdentifier=" github-app-identifier)
                   {:headers {"Content-Type" "text/plain;charset=UTF-8"
                               "Cookie" cookie
                               "Origin" "https://app.asana.com"}
                    :body (json/generate-string {:task task-gid
                                                 :query pr-url
                                                 :attachment (str attachment-gid)
                                                 :asset (str attachment-gid)})
                    :as :json})
        :body)))


(defn github-widget!
  [sign-result]
  (-> (http/post "https://github.integrations.asana.plus/widget"
                 {:headers {"Content-Type" "application/json"
                             "Accept-Language" "en"
                             "Origin" "https://app.asana.com"
                             "X-Asana-Request-Signature" (:token sign-result)}
                  :body (json/generate-string {:data (:data sign-result)})
                  :as :json})
      :body))
