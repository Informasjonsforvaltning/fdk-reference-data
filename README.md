# FDK reference-data

Base image: [openjdk:15-slim](https://hub.docker.com/layers/openjdk/library/openjdk/15-slim/images/sha256-82fc670b1757068d299fb3f860201c5c97625b5ca351f903a6de33857398eb82?context=explore)
Source: [Dockerfile](https://github.com/Informasjonsforvaltning/fdk-reference-data/blob/master/Dockerfile)

##  Overview
FDK reference-data is a service that provides metadata for the various applications in this ecosystem. It provides code-lists, concepts and helptexts. 

# Technologies/frameworks
* Java 15
* Spring Boot v2.4.5
* Redis

## API

API documentation provided by Swagger 2.0 and is available on "/api-docs/reference-data".

The service provides REST API for:

* `GET /media-types`
    * Returns all IANA media types.
* `GET /file-types`
    * Returns all EU file types.    
  

## Environment variables
```
REDIS_HOST=localhost
REDIS_PORT=6379
```

## Run locally
```
docker-compose up -d

Start application in IDE of choice.
```    
        
 
