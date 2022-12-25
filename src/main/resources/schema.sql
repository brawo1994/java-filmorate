CREATE TABLE IF NOT EXISTS users (
    id              INTEGER         PRIMARY KEY AUTO_INCREMENT,
    email           VARCHAR(255)    NOT NULL,
    login           VARCHAR(255)    NOT NULL,
    name            VARCHAR(255)    NOT NULL,
    birthday        DATE            NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa
(
    id              INTEGER         PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255)    NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
    id              INTEGER         PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255)    NOT NULL,
    description     VARCHAR(255)    NOT NULL,
    release_date    DATE            NOT NULL,
    duration        INTEGER         NOT NULL,
    rating_mpa      INTEGER         REFERENCES mpa (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS genre
(
    id              INTEGER         PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255)    NOT NULL
);

CREATE TABLE IF NOT EXISTS films_genre
(
    film_id         INTEGER         REFERENCES films (id) ON DELETE CASCADE,
    genre_id        INTEGER         REFERENCES genre (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS director
(
    id              INTEGER         PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(255)    NOT NULL
);

CREATE TABLE IF NOT EXISTS films_director
(
    film_id         INTEGER         REFERENCES films (id) ON DELETE CASCADE,
    director_id     INTEGER         REFERENCES director (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, director_id)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id         INTEGER         REFERENCES users (id) ON DELETE CASCADE,
    friend_id       INTEGER         REFERENCES users (id) ON DELETE CASCADE,
    status          VARCHAR(255)    NOT NULL,
    PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS films_like (
    film_id         INTEGER         REFERENCES films (id) ON DELETE CASCADE,
    user_id         INTEGER         REFERENCES users (id) ON DELETE CASCADE,
    PRIMARY KEY (film_id, user_id)
);

CREATE TABLE IF NOT EXISTS films_review (
    review_id       INTEGER         PRIMARY KEY AUTO_INCREMENT,
    film_id         INTEGER         NOT NULL    REFERENCES films (id) ON DELETE CASCADE,
    user_id         INTEGER         NOT NULL    REFERENCES users (id) ON DELETE CASCADE,
    content         VARCHAR(255)    NOT NULL,
    is_positive     BOOLEAN
);

CREATE TABLE IF NOT EXISTS reviews_like (
    review_id       INTEGER         NOT NULL    REFERENCES films_review (review_id) ON DELETE CASCADE,
    user_id         INTEGER         NOT NULL    REFERENCES users (id)               ON DELETE CASCADE,
    grade           INTEGER         NOT NULL,
    PRIMARY KEY (review_id, user_id)
);

CREATE TABLE IF NOT EXISTS event_history (
    event_id INTEGER AUTO_INCREMENT PRIMARY KEY,
    timestamp LONG NOT NULL,
    user_id INTEGER NOT NULL REFERENCES users (id) ON DELETE CASCADE,
    entity_id INTEGER NOT NULL,
    operation VARCHAR(10) NOT NULL,
    event_type VARCHAR(10) NOT NULL
);