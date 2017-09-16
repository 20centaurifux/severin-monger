# severin-monger

## Introduction

severin-monger provides connection pooling for MongoDB by implementing the
[severin](https://github.com/20centaurifux/severin) API.

A created resource holds a database connection (com.mongodb.MongoClient), a
database instance (com.mongodb.DB) and the URI.

## Installation

The library can be installed from Clojars using Leiningen:

[![Clojars Project](http://clojars.org/zcfux/severin-monger/latest-version.svg)](https://clojars.org/zcfux/severin-monger)

## Usage

```
; create pool:
(use 'severin.pool.monger)

(def pool (severin.core/make-pool))

; connect to MongoDB and use "test" database:
(def r (severin.core/create! pool "monger://localhost/test"))

; do some stuff:
(mc/find (:db r) "foo" {})

; dispose connection:
(severin.core/dispose! pool r)
```
