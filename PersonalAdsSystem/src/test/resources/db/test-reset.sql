-- Reset test database before each integration test
TRUNCATE TABLE messages, comments, payments, ratings, sales_history, chats, ads, user_roles, users RESTART IDENTITY CASCADE;

-- Seed users
INSERT INTO users (id, username, email, password, about_me, average_rating, created_at) VALUES
 (1, 'owner', 'owner@test.com', '$2a$10$xxx', 'Owner user for integration tests', 4.5, now()),
 (2, 'other', 'other@test.com', '$2a$10$xxx', 'Other user for integration tests', 3.0, now()),
 (3, 'admin', 'admin@test.com', '$2a$10$xxx', 'Admin user for integration tests', 0.0, now());

-- User roles (resolve role ids dynamically)
INSERT INTO user_roles (user_id, role_id)
 SELECT 1, id FROM roles WHERE name = 'ROLE_USER' LIMIT 1;
INSERT INTO user_roles (user_id, role_id)
 SELECT 2, id FROM roles WHERE name = 'ROLE_USER' LIMIT 1;
INSERT INTO user_roles (user_id, role_id)
 SELECT 3, id FROM roles WHERE name = 'ROLE_ADMIN' LIMIT 1;

-- Seed ads
INSERT INTO ads (id, user_id, category, title, description, price, is_active, is_premium, created_at) VALUES
 (1, 1, 'ELECTRONICS', 'iPhone 13', 'Хороший телефон', 60000, true, false, now()),
 (2, 1, 'TRANSPORT', 'Toyota Camry', 'Хорошая машина', 1500000, true, true, now()),
 (3, 2, 'ELECTRONICS', 'Samsung TV', 'Большой телевизор', 80000, true, false, now());

-- Seed comments
INSERT INTO comments (id, user_id, ad_id, content, send_at) VALUES
 (1, 2, 1, 'Отличное объявление', now());

-- Seed chats
INSERT INTO chats (id, ad_id, buyer_id, seller_id, created_at) VALUES
 (1, 1, 2, 1, now());

-- Seed messages
INSERT INTO messages (id, chat_id, sender_id, content, send_at, is_read) VALUES
 (1, 1, 2, 'Здравствуйте, объявление актуально?', now(), false);

-- Seed payments
INSERT INTO payments (id, ad_id, user_id, plan, amount, confirmed_at, expire_at) VALUES
 (1, 2, 1, 'WEEK', 500, now(), NOW() + INTERVAL '7 days');

-- Seed ratings
INSERT INTO ratings (id, recipient_id, reviewer_id, rating, created_at) VALUES
 (1, 1, 2, 4, now());

-- Seed sales_history
INSERT INTO sales_history (id, ad_id, ad_title, seller_id, buyer_id, price, sold_at) VALUES
 (1, 3, 'Samsung TV', 2, 1, 80000, now());

-- Reset sequences
SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true);
SELECT setval(pg_get_serial_sequence('ads', 'id'), COALESCE((SELECT MAX(id) FROM ads), 1), true);
SELECT setval(pg_get_serial_sequence('comments', 'id'), COALESCE((SELECT MAX(id) FROM comments), 1), true);
SELECT setval(pg_get_serial_sequence('chats', 'id'), COALESCE((SELECT MAX(id) FROM chats), 1), true);
SELECT setval(pg_get_serial_sequence('messages', 'id'), COALESCE((SELECT MAX(id) FROM messages), 1), true);
SELECT setval(pg_get_serial_sequence('payments', 'id'), COALESCE((SELECT MAX(id) FROM payments), 1), true);
SELECT setval(pg_get_serial_sequence('ratings', 'id'), COALESCE((SELECT MAX(id) FROM ratings), 1), true);
SELECT setval(pg_get_serial_sequence('sales_history', 'id'), COALESCE((SELECT MAX(id) FROM sales_history), 1), true);
