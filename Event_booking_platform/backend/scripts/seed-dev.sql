-- Development seed data for manual testing. DEV ONLY - never run against prod.
-- Run on an EMPTY database (tables are created by the API on first start):
--   docker compose exec -T mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" event_booking_db' < backend/scripts/seed-dev.sql
-- Every account below uses the password: Password123!
-- Dates are relative to NOW(), so re-seed to refresh them.

SET @pw = '$2a$10$0fSfgyrnLzNYC/3Z/l9aKOATVbTHCat8J4MNghibz27OaNZck0h.i';

INSERT INTO users (username, email, password, role, active) VALUES
  ('admin',          'admin@example.com',     @pw, 'ADMIN',     1),
  ('organizer_anna', 'anna@example.com',      @pw, 'ORGANIZER', 1),
  ('organizer_marco','marco@example.com',     @pw, 'ORGANIZER', 1),
  ('alice',          'alice@example.com',     @pw, 'ATTENDEE',  1),
  ('bob',            'bob@example.com',       @pw, 'ATTENDEE',  1),
  ('carol',          'carol@example.com',     @pw, 'ATTENDEE',  1),
  ('dave_inactive',  'dave@example.com',      @pw, 'ATTENDEE',  0);

INSERT INTO venues (name, address, city, capacity) VALUES
  ('Tirana Cultural Centre',  'Rruga Myslym Shyri 1',   'Tirana', 300),
  ('Berat Open Air Arena',    'Bulevardi Republika 12', 'Berat',  1000),
  ('Durres Seaside Hall',     'Rruga Taulantia 8',      'Durres', 60),
  ('Shkoder Lakeside Park',   'Rruga e Liqenit 3',      'Shkoder', 150),
  ('Vlora Conference Hall',   'Rruga Ismail Qemali 20', 'Vlora',  120);

INSERT INTO categories (name) VALUES
  ('Music'), ('Technology'), ('Business'), ('Food & Drink'),
  ('Arts'), ('Sports'), ('Comedy'), ('Family');

SET @anna  = (SELECT id FROM users WHERE username = 'organizer_anna');
SET @marco = (SELECT id FROM users WHERE username = 'organizer_marco');
SET @alice = (SELECT id FROM users WHERE username = 'alice');
SET @bob   = (SELECT id FROM users WHERE username = 'bob');
SET @carol = (SELECT id FROM users WHERE username = 'carol');

SET @tirana  = (SELECT id FROM venues WHERE name = 'Tirana Cultural Centre');
SET @berat   = (SELECT id FROM venues WHERE name = 'Berat Open Air Arena');
SET @durres  = (SELECT id FROM venues WHERE name = 'Durres Seaside Hall');
SET @shkoder = (SELECT id FROM venues WHERE name = 'Shkoder Lakeside Park');

-- title, description, start, end, price, total, available, status, venue, organizer, version
INSERT INTO events (title, description, start_date_time, end_date_time, price, total_seats, available_seats, status, venue_id, organizer_id, version) VALUES
  -- ended events (reviews can be left)
  ('Tirana Jazz Night', 'An evening of live jazz with local and visiting artists.',
     NOW() - INTERVAL 10 DAY, NOW() - INTERVAL 10 DAY + INTERVAL 3 HOUR, 25.00, 100, 95, 'PUBLISHED', @tirana, @anna, 0),
  ('Spring Tech Meetup', 'Talks and demos from the local developer community.',
     NOW() - INTERVAL 30 DAY, NOW() - INTERVAL 30 DAY + INTERVAL 4 HOUR, 10.00, 50, 49, 'PUBLISHED', @tirana, @marco, 0),
  -- upcoming published events
  ('Summer Music Festival', 'Two stages, food trucks, and a sunset headliner under the open sky.',
     NOW() + INTERVAL 20 DAY, NOW() + INTERVAL 20 DAY + INTERVAL 8 HOUR, 60.00, 200, 196, 'PUBLISHED', @berat, @anna, 0),
  ('Startup Pitch Night', 'Ten early-stage teams pitch to a panel of investors and mentors.',
     NOW() + INTERVAL 7 DAY, NOW() + INTERVAL 7 DAY + INTERVAL 3 HOUR, 15.00, 40, 38, 'PUBLISHED', @tirana, @marco, 0),
  ('Cooking Workshop: Coastal Cuisine', 'Hands-on workshop cooking three seafood dishes. Fully booked - join the waitlist.',
     NOW() + INTERVAL 3 DAY, NOW() + INTERVAL 3 DAY + INTERVAL 3 HOUR, 35.00, 10, 0, 'PUBLISHED', @durres, @anna, 0),
  ('Lakeside Photography Walk', 'A guided sunrise walk around the lake. Free entry, all levels welcome.',
     NOW() + INTERVAL 12 DAY, NOW() + INTERVAL 12 DAY + INTERVAL 2 HOUR, 0.00, 25, 25, 'PUBLISHED', @shkoder, @marco, 0),
  ('Late Night Comedy', 'Stand-up starting in a few hours - inside the 24h cancellation window.',
     NOW() + INTERVAL 10 HOUR, NOW() + INTERVAL 13 HOUR, 20.00, 30, 28, 'PUBLISHED', @tirana, @anna, 0),
  -- drafts
  ('Autumn Food Market', 'Draft: street food and local producers.',
     NOW() + INTERVAL 40 DAY, NOW() + INTERVAL 40 DAY + INTERVAL 6 HOUR, 5.00, 150, 150, 'DRAFT', @shkoder, @anna, 0),
  ('Charity Fun Run', 'Draft: 5k charity run through the city.',
     NOW() + INTERVAL 50 DAY, NOW() + INTERVAL 50 DAY + INTERVAL 4 HOUR, 12.00, 120, 120, 'DRAFT', @tirana, @marco, 0),
  -- cancelled
  ('Winter Gala', 'Cancelled by the organizer.',
     NOW() + INTERVAL 25 DAY, NOW() + INTERVAL 25 DAY + INTERVAL 5 HOUR, 80.00, 100, 100, 'CANCELLED', @tirana, @marco, 0);

INSERT INTO event_categories (event_id, category_id)
SELECT e.id, c.id FROM events e JOIN categories c ON
     (e.title = 'Tirana Jazz Night'                   AND c.name IN ('Music', 'Arts'))
  OR (e.title = 'Spring Tech Meetup'                  AND c.name IN ('Technology'))
  OR (e.title = 'Summer Music Festival'               AND c.name IN ('Music', 'Food & Drink'))
  OR (e.title = 'Startup Pitch Night'                 AND c.name IN ('Business', 'Technology'))
  OR (e.title = 'Cooking Workshop: Coastal Cuisine'   AND c.name IN ('Food & Drink'))
  OR (e.title = 'Lakeside Photography Walk'           AND c.name IN ('Arts', 'Family'))
  OR (e.title = 'Late Night Comedy'                   AND c.name IN ('Comedy'))
  OR (e.title = 'Autumn Food Market'                  AND c.name IN ('Food & Drink', 'Family'))
  OR (e.title = 'Charity Fun Run'                     AND c.name IN ('Sports'))
  OR (e.title = 'Winter Gala'                         AND c.name IN ('Music', 'Business'));

SET @jazz    = (SELECT id FROM events WHERE title = 'Tirana Jazz Night');
SET @meetup  = (SELECT id FROM events WHERE title = 'Spring Tech Meetup');
SET @summer  = (SELECT id FROM events WHERE title = 'Summer Music Festival');
SET @pitch   = (SELECT id FROM events WHERE title = 'Startup Pitch Night');
SET @cooking = (SELECT id FROM events WHERE title = 'Cooking Workshop: Coastal Cuisine');
SET @comedy  = (SELECT id FROM events WHERE title = 'Late Night Comedy');

INSERT INTO bookings (seats_booked, status, booking_date, user_id, event_id) VALUES
  (2, 'CONFIRMED', NOW() - INTERVAL 20 DAY, @alice, @jazz),
  (3, 'CONFIRMED', NOW() - INTERVAL 18 DAY, @bob,   @jazz),
  (1, 'CONFIRMED', NOW() - INTERVAL 40 DAY, @carol, @meetup),
  (4, 'CONFIRMED', NOW() - INTERVAL 2 DAY,  @alice, @summer),
  (2, 'CONFIRMED', NOW() - INTERVAL 1 DAY,  @bob,   @pitch),
  (1, 'CANCELLED', NOW() - INTERVAL 3 DAY,  @carol, @pitch),
  (5, 'CONFIRMED', NOW() - INTERVAL 5 DAY,  @alice, @cooking),
  (5, 'CONFIRMED', NOW() - INTERVAL 4 DAY,  @bob,   @cooking),
  (2, 'CONFIRMED', NOW() - INTERVAL 1 DAY,  @alice, @comedy);

INSERT INTO waitlist_entries (joined_at, status, user_id, event_id) VALUES
  (NOW() - INTERVAL 1 DAY, 'WAITING', @carol, @cooking);

INSERT INTO reviews (rating, comment, created_at, user_id, event_id) VALUES
  (5, 'Wonderful atmosphere and great musicians.', NOW() - INTERVAL 9 DAY, @alice, @jazz),
  (4, 'Great night, the venue was a little warm.',  NOW() - INTERVAL 9 DAY, @bob,   @jazz),
  (4, 'Useful talks and friendly people.',          NOW() - INTERVAL 29 DAY, @carol, @meetup);
