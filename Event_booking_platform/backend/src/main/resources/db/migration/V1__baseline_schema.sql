-- Baseline schema (matches the entity mappings as of the Flyway introduction).

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `active` bit(1) NOT NULL,
  `email` varchar(254) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('ADMIN','ATTENDEE','ORGANIZER') NOT NULL,
  `username` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_users_username` (`username`),
  UNIQUE KEY `uk_users_email` (`email`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `venues` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `address` varchar(255) NOT NULL,
  `capacity` int NOT NULL,
  `city` varchar(100) NOT NULL,
  `name` varchar(160) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `categories` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_categories_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `events` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `available_seats` int NOT NULL,
  `description` text NOT NULL,
  `end_date_time` datetime(6) NOT NULL,
  `price` decimal(10,2) NOT NULL,
  `start_date_time` datetime(6) NOT NULL,
  `status` enum('CANCELLED','DRAFT','PUBLISHED') NOT NULL,
  `title` varchar(180) NOT NULL,
  `total_seats` int NOT NULL,
  `version` bigint NOT NULL,
  `organizer_id` bigint NOT NULL,
  `venue_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdocju8m76a3f8o6ljh2jrn2ra` (`organizer_id`),
  KEY `FKqdxygdernwwt74hdvix9u5nr3` (`venue_id`),
  CONSTRAINT `FKdocju8m76a3f8o6ljh2jrn2ra` FOREIGN KEY (`organizer_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqdxygdernwwt74hdvix9u5nr3` FOREIGN KEY (`venue_id`) REFERENCES `venues` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `event_categories` (
  `event_id` bigint NOT NULL,
  `category_id` bigint NOT NULL,
  PRIMARY KEY (`event_id`,`category_id`),
  KEY `FKr1pvd2finvdolgyyntti13d3x` (`category_id`),
  CONSTRAINT `FK25gan1thxtb38jlco3w9pjsdo` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
  CONSTRAINT `FKr1pvd2finvdolgyyntti13d3x` FOREIGN KEY (`category_id`) REFERENCES `categories` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `bookings` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `booking_date` datetime(6) NOT NULL,
  `seats_booked` int NOT NULL,
  `status` enum('CANCELLED','CONFIRMED') NOT NULL,
  `event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2ww82bk3npaiyu9oeehwtt2q3` (`event_id`),
  KEY `FKeyog2oic85xg7hsu2je2lx3s6` (`user_id`),
  CONSTRAINT `FK2ww82bk3npaiyu9oeehwtt2q3` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`),
  CONSTRAINT `FKeyog2oic85xg7hsu2je2lx3s6` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reviews` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `comment` varchar(2000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `rating` int NOT NULL,
  `event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_review_user_event` (`user_id`,`event_id`),
  KEY `FKem6jjo18jyueiqhferf3dwfbx` (`event_id`),
  CONSTRAINT `FKcgy7qjc1r99dp117y9en6lxye` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKem6jjo18jyueiqhferf3dwfbx` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `waitlist_entries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `joined_at` datetime(6) NOT NULL,
  `status` enum('CANCELLED','PROMOTED','WAITING') NOT NULL,
  `event_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_waitlist_user_event` (`user_id`,`event_id`),
  KEY `FKhrfxv476fswluafeb1sux3yq6` (`event_id`),
  CONSTRAINT `FKdpgl4h9wd7kabg6dk86wdvjqy` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKhrfxv476fswluafeb1sux3yq6` FOREIGN KEY (`event_id`) REFERENCES `events` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `password_reset_tokens` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `expires_at` datetime(6) NOT NULL,
  `token_hash` varchar(64) NOT NULL,
  `used_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_password_reset_token_hash` (`token_hash`),
  KEY `FKk3ndxg5xp6v7wd4gjyusp15gq` (`user_id`),
  CONSTRAINT `FKk3ndxg5xp6v7wd4gjyusp15gq` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
