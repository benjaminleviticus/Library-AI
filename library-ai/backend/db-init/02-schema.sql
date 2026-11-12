
-- Books table
CREATE TABLE IF NOT EXISTS books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    isbn VARCHAR(255),
    qrcode VARCHAR(255) UNIQUE,
    qrcodePath VARCHAR(500),
    description TEXT,
    genre VARCHAR(255),
    embedding vector(1536),
    img VARCHAR(500)
);

-- Admin table
CREATE TABLE IF NOT EXISTS admin (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL
);
