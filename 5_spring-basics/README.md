# Spring Basics From Scratch Lab

This is a minimal Spring Boot project for a 1-hour internship exercise.

The setup is already done. Your task is to build the Spring classes yourself.

## Goal

Create a small notification application using Spring beans and dependency injection.

By the end, running the application should print something like:

```text
[INTERNSHIP] Sending EMAIL notification: Welcome to Spring!
```

## Step 1: Open the project in IntelliJ

1. Open IntelliJ IDEA.
2. Choose **File > Open**.
3. Select this project folder.
4. IntelliJ should detect the `pom.xml` file.
5. Wait until Maven finishes loading.
6. Run `SpringBasicsApplication`.

At this point, the app should start, but it does not do anything yet.

## Step 2: Create a NotificationService interface

Create a new file:

```text
src/main/java/com/example/springbasics/NotificationService.java
```

Add:

```java
package com.example.springbasics;

public interface NotificationService {
    void send(String message);
}
```

## Step 3: Create EmailNotificationService

Create a class called `EmailNotificationService`.

It should:

- implement `NotificationService`
- be managed by Spring using `@Service`
- print the message to the console

Example output:

```text
Sending EMAIL notification: Welcome to Spring!
```

## Step 4: Create NotificationManager

Create a class called `NotificationManager`.

It should:

- be managed by Spring using `@Component`
- receive `NotificationService` through constructor injection
- have a method called `notifyUser(String message)`

## Step 5: Run code when the app starts

Create a class called `AppRunner`.

It should:

- be managed by Spring using `@Component`
- implement `CommandLineRunner`
- inject `NotificationManager`
- call `notificationManager.notifyUser("Welcome to Spring!")`

## Step 6: Add application property

The project already contains this property:

```properties
notification.prefix=[INTERNSHIP]
```

Inject it into `EmailNotificationService` using `@Value`.

Expected final output:

```text
[INTERNSHIP] Sending EMAIL notification: Welcome to Spring!
```

## Bonus task

Create `SmsNotificationService` as another implementation of `NotificationService`.

Then solve the multiple-bean problem using either:

- `@Primary`
- `@Qualifier`

