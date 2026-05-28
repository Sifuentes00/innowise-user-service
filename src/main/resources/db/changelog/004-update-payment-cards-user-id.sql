ALTER TABLE payment_cards DROP CONSTRAINT fk_payment_cards_user;

ALTER TABLE payment_cards ADD CONSTRAINT fk_payment_cards_user FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE;
