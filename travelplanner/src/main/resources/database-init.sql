-- database-init.sql
-- Travel Planner - P0 schema (PostgreSQL)
-- Requires: PostgreSQL 13+ (recommended 14/15/16)

-- =========================
-- Extensions
-- =========================
-- For gen_random_uuid()
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- =========================
-- Schemas (optional: use public)
-- =========================
-- CREATE SCHEMA IF NOT EXISTS travelplanner;
-- SET search_path TO travelplanner, public;

-- =========================
-- Table: itineraries （行程）
-- 单城市、1~15天；仅保存基础信息
-- =========================
CREATE TABLE IF NOT EXISTS itineraries (
                                           id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city         VARCHAR(100)   NOT NULL,
    days         SMALLINT       NOT NULL CHECK (days BETWEEN 1 AND 15),
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT NOW()
    );

-- =========================
-- Table: pois （兴趣点/站点）
-- 记录顺序、坐标；可选原始来源数据 raw
-- =========================
CREATE TABLE IF NOT EXISTS pois (
                                    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    itinerary_id   UUID          NOT NULL,
    name           VARCHAR(200)  NOT NULL,
    lat            NUMERIC(9,6)  NOT NULL, -- ~ ±90.000000
    lng            NUMERIC(9,6)  NOT NULL, -- ~ ±180.000000
    sequence       SMALLINT      NOT NULL CHECK (sequence >= 1),
    rating         NUMERIC(3,2),           -- 可选：0.00~5.00
    address        VARCHAR(300),
    place_id       VARCHAR(128),           -- 可选：Google Place ID
    raw            JSONB,                  -- 可选：Places/Details 原始数据快照
    created_at     TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_pois_itinerary
    FOREIGN KEY (itinerary_id) REFERENCES itineraries(id)
    ON DELETE CASCADE,
    CONSTRAINT uq_itinerary_sequence UNIQUE (itinerary_id, sequence)
    );

-- =========================
-- Table: cities （静态白名单：可选）
-- P0：用于前端城市下拉或校验；后续可替换为外部源
-- =========================
CREATE TABLE IF NOT EXISTS cities (
                                      id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    country     VARCHAR(100) NOT NULL,
    alpha_rank  SMALLINT     NOT NULL CHECK (alpha_rank >= 1),
    lat         NUMERIC(9,6),
    lng         NUMERIC(9,6),
    CONSTRAINT uq_city UNIQUE (name, country)
    );

-- =========================
-- Indexes
-- =========================
-- 常用查询：按行程取 POI，顺序排序
CREATE INDEX IF NOT EXISTS idx_pois_itinerary ON pois (itinerary_id);
CREATE INDEX IF NOT EXISTS idx_pois_itinerary_sequence ON pois (itinerary_id, sequence);

-- 如果会按 place_id 查重，可启用：
-- CREATE UNIQUE INDEX IF NOT EXISTS uq_pois_place_per_itinerary ON pois (itinerary_id, place_id);

-- 如果会对 raw(JSONB) 做筛选（如按类型/标签），可启用 GIN：
-- CREATE INDEX IF NOT EXISTS idx_pois_raw_gin ON pois USING GIN (raw);