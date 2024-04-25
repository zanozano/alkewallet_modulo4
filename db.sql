CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    email VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);

--------------

INSERT INTO users (email, password)
VALUES
('cristobal@alkewallet.com', '123456'),
('francisco@alkewallet.com', '123456');

--------------

CREATE TABLE currencies (
    currency_code VARCHAR(3) PRIMARY KEY
);

INSERT INTO currencies (currency_code) VALUES
('CLP'),
('USD'),
('EUR'),
('THB'),
('CNY');

--------------

CREATE TABLE accounts (
    id UUID PRIMARY KEY,
    user_id UUID,
    currency_code VARCHAR(3),
    FOREIGN KEY (currency_code) REFERENCES currencies(currency_code),
    FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO accounts (id, user_id, currency_code)
SELECT uuid_generate_v4(), u.id, c.currency_code
FROM users u
CROSS JOIN currencies c
WHERE u.email = 'cristobal@alkewallet.com'
LIMIT 5;

--------------

CREATE TABLE balances (
    balance_id UUID PRIMARY KEY,
    account_id UUID REFERENCES accounts(id),
    currency_code VARCHAR(3) REFERENCES currencies(currency_code),
    amount NUMERIC(18, 2),
    UNIQUE (account_id, currency_code)
);

INSERT INTO balances (balance_id, account_id, currency_code, amount)
SELECT DISTINCT ON (a.id, c.currency_code)
    uuid_generate_v4(), a.id, c.currency_code, 100000
FROM accounts a
CROSS JOIN currencies c
WHERE a.user_id = (SELECT id FROM users WHERE email = 'cristobal@alkewallet.com')
AND NOT EXISTS (
    SELECT 1
    FROM balances b
    WHERE b.account_id = a.id
    AND b.currency_code = c.currency_code
)
LIMIT 5;

--------------

CREATE TABLE transactions (
    transaction_id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    sender_id UUID REFERENCES users(id),
    receiver_id UUID REFERENCES users(id),
    amount NUMERIC(18, 2),
    currency_code VARCHAR(3) REFERENCES currencies(currency_code),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

--------------

SELECT
    u.email,
    u.password,
    b.account_id,
    b.currency_code,
    b.amount
FROM
    users u
JOIN
    accounts a ON u.id = a.user_id
JOIN
    balances b ON a.id = b.account_id
WHERE
    u.email = 'cristobal@alkewallet.com';

--------------


