# FDK Reference data

This application provides a common API (REST & GraphQL) for codelists, concepts, help texts and other kinds of reference
data that are used by many other applications in the overall architecture. The application imports the reference data
from various sources as scheduled tasks.

For a broader understanding of the system’s context, refer to
the [architecture documentation](https://github.com/Informasjonsforvaltning/architecture-documentation) wiki.

## Getting Started

These instructions will give you a copy of the project up and running on your local machine for development and testing
purposes.

### Prerequisites

Ensure you have the following installed:

- Java 21
- Maven
- Docker

### Running locally

Clone the repository

```sh
git clone https://github.com/Informasjonsforvaltning/fdk-reference-data.git
cd fdk-reference-data
```

Start MongoDB and the application (either through your IDE using the dev profile, or via CLI):

```sh
docker compose up -d
mvn spring-boot:run -Dspring-boot.run.profiles=develop
```

### API Documentation

The API documentation is available at ```src/main/resources/openapi``` and ```src/main/resources/graphql```.

### Running tests

```sh
mvn verify
```
