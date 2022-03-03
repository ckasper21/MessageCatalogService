# Message Catalog Service 
Task
-----
1. You are given two feeds running locally, sending messages to a local NATS broker. Catalog every message with an associated timestamp into a persistent storage. These messages are serialized via protobuf.

Run
-----
1. Spin up docker containers to start feeds: `docker-compose up`
2. `./gradlew clean build`
3. `./gradlew bootRun`

Requirements
------------
- Java JDK 11
- Gradle-7.4 (for external libraries)
- Docker
- Log files are stored in /logs directory

Notes
------------
- The NATS server/container must be started before running the application or the application will quit.
- In a real world example, we would want to store our messages in a database such as MongoDB.