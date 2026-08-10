INSERT INTO hotels (id, name, city, address, star_rating) VALUES
    (1, 'Central Park Hotel', 'Paris', '25 Central Avenue', 4),
    (2, 'Riverside Grand Hotel', 'Lyon', '10 Riverside Road', 5),
    (3, 'Airport Business Hotel', 'Paris', '50 Airport Boulevard', 3);

INSERT INTO guests (id, first_name, last_name, email, phone_number) VALUES
    (1, 'Sofia', 'Martin', 'sofia.martin@example.com', '+33123456789'),
    (2, 'Daniel', 'Brown', 'daniel.brown@example.com', '+441234567890'),
    (3, 'Emma', 'Schmidt', 'emma.schmidt@example.com', '+491234567890');

INSERT INTO guest_profiles (
    id, guest_id, address, date_of_birth, nationality, preferred_language
) VALUES
    (1, 1, '14 Rue de Lyon, Paris', '1995-06-20', 'French', 'French'),
    (2, 2, '22 King Street, London', '1988-11-04', 'British', 'English'),
    (3, 3, '18 Hauptstrasse, Berlin', '1992-02-15', 'German', 'German');

INSERT INTO rooms (
    id, hotel_id, room_number, room_type, capacity, price_per_night, status
) VALUES
    (1, 1, '101', 'STANDARD', 2, 120.00, 'AVAILABLE'),
    (2, 1, '102', 'DELUXE', 3, 180.00, 'AVAILABLE'),
    (3, 1, '201', 'SUITE', 4, 300.00, 'MAINTENANCE'),
    (4, 2, '101', 'STANDARD', 2, 150.00, 'AVAILABLE'),
    (5, 2, '202', 'SUITE', 5, 400.00, 'AVAILABLE'),
    (6, 3, 'A101', 'STANDARD', 1, 90.00, 'AVAILABLE');

INSERT INTO reservations (
    id, guest_id, room_id, check_in_date, check_out_date, number_of_guests,
    total_price, status, created_at
) VALUES
    (1, 1, 1, '2027-07-10', '2027-07-13', 2, 360.00, 'CONFIRMED', CURRENT_TIMESTAMP),
    (2, 2, 2, '2027-07-15', '2027-07-18', 3, 540.00, 'PENDING', CURRENT_TIMESTAMP),
    (3, 3, 4, '2027-08-01', '2027-08-05', 2, 600.00, 'CONFIRMED', CURRENT_TIMESTAMP),
    (4, 1, 5, '2027-06-01', '2027-06-03', 4, 800.00, 'CANCELLED', CURRENT_TIMESTAMP),
    (5, 2, 1, '2027-08-10', '2027-08-12', 1, 240.00, 'CONFIRMED', CURRENT_TIMESTAMP);
