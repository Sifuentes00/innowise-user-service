ALTER TABLE users ADD COLUMN user_id UUID UNIQUE;

CREATE INDEX idx_users_user_id ON users(user_id);
