# JMS ActiveMQ Demo (Spring Boot Multi-Module)

This project demonstrates **JMS messaging patterns** with **ActiveMQ Classic** using three independent Spring Boot applications:

1. `producer-service`
2. `consumer-a-service`
3. `consumer-b-service`

## 1. What is JMS?

**JMS (Java Message Service)** is a Java API standard for sending and receiving messages between distributed systems. It defines common messaging concepts such as:

- `Message`
- `Queue`
- `Topic`
- Message producers and consumers

JMS provides a common programming model, regardless of the underlying message broker vendor.

## 2. What is ActiveMQ?

**ActiveMQ Classic** is a message broker implementation. It receives, stores, routes, and delivers messages between producers and consumers. It supports JMS, OpenWire, and multiple transport protocols.

In this project, ActiveMQ runs in Docker and acts as the central broker for all three applications.

## 3. JMS vs ActiveMQ

- **JMS** = specification/API (the contract your Java code uses).
- **ActiveMQ** = concrete broker product implementing JMS behavior.

You code to JMS interfaces (`JmsTemplate`, `@JmsListener`) and run against ActiveMQ as the runtime messaging infrastructure.

## 4. What is `JmsTemplate`?

`JmsTemplate` is a Spring helper for producing JMS messages. It handles:

- Connection/session boilerplate
- Message conversion
- Sending messages to queue or topic destinations

In this project:

- Default `JmsTemplate` sends to queue `orders.queue`.
- `topicJmsTemplate` (`setPubSubDomain(true)`) publishes to topic `orders.topic`.

## 5. What is `@JmsListener`?

`@JmsListener` marks a method as an asynchronous JMS message consumer.

- Spring creates listener containers in the background.
- The method runs when a new message arrives.

This project uses two container factories:

- `queueFactory` (`pubSubDomain=false`)
- `topicFactory` (`pubSubDomain=true`)

## 6. Queues vs Topics

- **Queue (Point-to-Point):** each message is processed by **one** consumer only.
- **Topic (Publish/Subscribe):** each message is delivered to **all active subscribers**.

With both consumers running:

- Queue message -> handled by Consumer A **or** Consumer B (competing consumers).
- Topic message -> handled by Consumer A **and** Consumer B.

## 7. Project Architecture

```text
jms-activemq-demo/
├── pom.xml
├── docker-compose.yml
├── README.md
├── producer-service/
├── consumer-a-service/
└── consumer-b-service/
```

Message flow:

1. Producer receives REST calls.
2. Producer builds an `OrderEvent` and serializes it to JSON.
3. Producer sends JSON to queue and/or topic.
4. Consumer A and Consumer B listen on both destinations.
5. Queue messages are load-balanced between consumers.
6. Topic messages are broadcast to all active consumers.

## 8. Start ActiveMQ

From the project root:

```bash
docker compose up -d
```

Ports:

- `61616` -> JMS/OpenWire broker endpoint
- `8161` -> ActiveMQ web console (`http://localhost:8161`, default often `admin/admin`)

## 9. Build all modules

```bash
mvn clean install
```

## 10. Run every service

Open three terminals from project root:

Terminal 1:

```bash
mvn -pl consumer-a-service spring-boot:run
```

Terminal 2:

```bash
mvn -pl consumer-b-service spring-boot:run
```

Terminal 3:

```bash
mvn -pl producer-service spring-boot:run
```

## 11. Curl commands for testing

Queue:

```bash
curl -X POST "http://localhost:8080/api/messages/queue?customer=Alice&amount=150"
```

Topic:

```bash
curl -X POST "http://localhost:8080/api/messages/topic?customer=Bob&amount=250"
```

Both queue + topic:

```bash
curl -X POST "http://localhost:8080/api/messages/both?customer=Charlie&amount=350"
```

Each call returns `202 Accepted` with the generated `OrderEvent`.

## 12. Expected output

Producer log example:

```text
Published to queue [orders.queue]: {"orderId":"...","customerName":"Alice","amount":150,"createdAt":"..."}
Published to topic [orders.topic]: {"orderId":"...","customerName":"Charlie","amount":350,"createdAt":"..."}
```

Consumer output example for queue (only one consumer gets each message):

```text
==================================================
 Consumer A RECEIVED MESSAGE
 Source : QUEUE
 Body   : {"orderId":"...","customerName":"Alice","amount":150,"createdAt":"..."}
==================================================
```

Consumer output example for topic (both consumers get the same message):

```text
==================================================
 Consumer A RECEIVED MESSAGE
 Source : TOPIC
 Body   : {"orderId":"...","customerName":"Bob","amount":250,"createdAt":"..."}
==================================================
```

```text
==================================================
 Consumer B RECEIVED MESSAGE
 Source : TOPIC
 Body   : {"orderId":"...","customerName":"Bob","amount":250,"createdAt":"..."}
==================================================
```

## 13. Classroom exercises

### Experiment 1: Queue persistence

1. Stop both consumers.
2. Send five queue messages.
3. Open the ActiveMQ console.
4. Verify that messages are waiting.
5. Start one consumer.
6. Observe the messages being processed.

### Experiment 2: Competing consumers

1. Start both consumers.
2. Send ten queue messages.
3. Observe that each message is processed by only one consumer.

### Experiment 3: Topic broadcasting

1. Start both consumers.
2. Publish one topic message.
3. Observe that both consumers receive it.

### Experiment 4: Offline topic subscriber

1. Stop Consumer B.
2. Publish a topic message.
3. Start Consumer B.
4. Consumer B does not receive the earlier message because non-durable topic subscribers only receive messages while they are actively connected.
