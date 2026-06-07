CREATE TABLE IF NOT EXISTS movie (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    title        VARCHAR(500) NOT NULL,
    release_year INTEGER      NOT NULL,
    winner       BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE INDEX IF NOT EXISTS idx_movie_release_year ON movie (release_year);
CREATE INDEX IF NOT EXISTS idx_movie_winner        ON movie (winner);

CREATE TABLE IF NOT EXISTS producer (
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(500) NOT NULL,
    CONSTRAINT uq_producer_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_producer_name ON producer (name);

CREATE TABLE IF NOT EXISTS movie_producer (
    movie_id    BIGINT NOT NULL,
    producer_id BIGINT NOT NULL,
    CONSTRAINT pk_movie_producer PRIMARY KEY (movie_id, producer_id),
    CONSTRAINT fk_mp_movie    FOREIGN KEY (movie_id)    REFERENCES movie (id),
    CONSTRAINT fk_mp_producer FOREIGN KEY (producer_id) REFERENCES producer (id)
);

CREATE OR REPLACE VIEW vw_producer_intervals AS
SELECT
    ROW_NUMBER() OVER (ORDER BY p.id, m.release_year)                                   AS id,
    p.name                                                                               AS producer_name,
    m.release_year                                                                       AS previous_win,
    LEAD(m.release_year) OVER (PARTITION BY p.id ORDER BY m.release_year)               AS following_win,
    LEAD(m.release_year) OVER (PARTITION BY p.id ORDER BY m.release_year) - m.release_year AS interval_diff
FROM producer p
         JOIN movie_producer mp ON p.id = mp.producer_id
         JOIN movie m ON mp.movie_id = m.id
WHERE m.winner = TRUE;
