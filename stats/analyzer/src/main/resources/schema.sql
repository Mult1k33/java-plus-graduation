-- Создание схемы
CREATE SCHEMA IF NOT EXISTS stats_analyzer;

DROP TABLE IF EXISTS stats_analyzer.event_similarities CASCADE;
DROP TABLE IF EXISTS stats_analyzer.user_actions CASCADE;

-- Создание таблицы действий пользователей
CREATE TABLE IF NOT EXISTS stats_analyzer.user_actions (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    user_id BIGINT NOT NULL,
    event_id BIGINT NOT NULL,
    user_score DOUBLE PRECISION NOT NULL,
    timestamp_action TIMESTAMP NOT NULL
);

-- Создание таблицы сходства событий
CREATE TABLE stats_analyzer.event_similarities (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_a BIGINT NOT NULL,
    event_b BIGINT NOT NULL,
    score DOUBLE PRECISION NOT NULL
);

-- Индекс для быстрого поиска по паре событий
CREATE UNIQUE INDEX idx_event_similarities_pair ON stats_analyzer.event_similarities(event_a, event_b);
-- Индекс для быстрого поиска похожих мероприятий
CREATE INDEX IF NOT EXISTS idx_event_similarities_score ON stats_analyzer.event_similarities(score DESC);