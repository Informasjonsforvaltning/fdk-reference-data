# FDK reference-data

Base image: [openjdk:15-slim](https://hub.docker.com/layers/openjdk/library/openjdk/15-slim/images/sha256-82fc670b1757068d299fb3f860201c5c97625b5ca351f903a6de33857398eb82?context=explore)
Source: [Dockerfile](https://github.com/Informasjonsforvaltning/fdk-reference-data/blob/master/Dockerfile)

##  Overview
FDK reference-data is a service that provides metadata for the various applications in this ecosystem. It provides code-lists, concepts and helptexts. 

# Technologies/frameworks
* Java 21
* Spring Boot
* MongoDB

## API
The service provides REST API with following specification: [OpenAPI3](./src/main/resources/openapi/openapi.yaml).

## Environment variables
```
MONGODB_DATABASE=reference-data
MONGODB_PORT=27017
MONGODB_HOST=localhost
MONGODB_USERNAME=root
MONGODB_PASSWORD=password
MONGODB_AUTH_DATABASE=admin

API_KEY=my-api-key

LOG_LEVEL=DEBUG
```

## Run locally
```
docker-compose up -d

Start application in IDE of choice.
```    
        