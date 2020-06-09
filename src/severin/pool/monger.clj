(ns severin.pool.monger
  "MongoDB connection pooling."
  (:require [severin.core :as severin]
            (monger
             [core :as mg]
             [credentials :as mcred])))

;; A created resource holds a database connection (com.mongodb.MongoClient), a database instance (com.mongodb.DB) and the URI.
(defrecord MongerConn [conn db uri])

(defn- parse-uri
  "Parses a URI string into a map of keywords to URI parts."
  [uri]
  (let [uri' (java.net.URI. uri)]
    {:host (.getHost uri')
     :port (let [port (.getPort uri')]
             (if (= port -1)
               27017
               port))
     :db-name (let [path (.getPath uri')]
                (if-not (empty? path)
                  (subs path 1)
                  "test"))
     :credentials (when-let [info (.getUserInfo uri')]
                    (zipmap [:username :password] (clojure.string/split info #":" 2)))}))

(defn- create-credentials
  "Create a MongoCredential instance from a map."
  [m]
  (when-let [cred (:credentials m)]
    (let [[username password] (map cred [:username :password])]
      (mcred/create username (:db-name m) password))))

;; Create and dispose MongoDB connections.
(defrecord MongerFactory
           []

  severin/FactoryProtocol

  (-create!
    [this uri]
    (let [uri' (parse-uri uri)
          conn (if-let [cred (:credentials uri')]
                 (mg/connect-with-credentials (:host uri') (:port uri') (create-credentials uri'))
                 (mg/connect uri'))
          db (mg/get-db conn (:db-name uri'))]
      (MongerConn. conn db uri)))

  (-dispose!
    [this resource]
    (mg/disconnect (:conn resource)))

  (-recycle!
    [this resource uri]
    (when-let [uri' (parse-uri uri)]
      (assoc resource :db (mg/get-db (:conn resource) (:db-name uri')))))

  (-valid?
    [this resource]
    true)

  severin/URI->KeyProtocol

  (-uri->key
    [this uri]
    (let [uri' (parse-uri uri)]
      (-> (if (:credentials uri')
            (format "%s@%s:%s:%d/%s"
                    (get-in uri' [:credentials :username])
                    (get-in uri' [:credentials :password])
                    (:host uri')
                    (:port uri')
                    (:db-name uri'))
            (format "%s:%d/%s"
                    (:host uri')
                    (:port uri')
                    (:db-name uri')))
          keyword))))

(defmethod severin/->factory "monger"
  [uri]
  (MongerFactory.))
