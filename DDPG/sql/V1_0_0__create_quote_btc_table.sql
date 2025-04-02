USE quote;

CREATE TABLE IF NOT EXISTS quote_btc_1m (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_3m (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_5m (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_15m (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_30m (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_1h (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_2h (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_4h (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_6h (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_8h (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_12h (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_1d (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_3d (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_1w (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);

CREATE TABLE IF NOT EXISTS quote_btc_1M (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,         -- 自增主键
    open_time BIGINT NOT NULL UNIQUE,              -- 开盘时间 (Unix 时间戳)，并且是唯一的
    open DECIMAL(18,8) NOT NULL,                  -- 开盘价
    high DECIMAL(18,8) NOT NULL,                  -- 最高价
    low DECIMAL(18,8) NOT NULL,                   -- 最低价
    close DECIMAL(18,8) NOT NULL,                 -- 收盘价
    volume DECIMAL(18,8) NOT NULL,                -- 成交量
    close_time BIGINT NOT NULL,                   -- 收盘时间 (Unix 时间戳)
    quote_asset_volume DECIMAL(18,8) NOT NULL,    -- 成交额 (计价货币)
    trades BIGINT NOT NULL,                       -- 成交笔数
    taker_buy_base_asset_volume DECIMAL(18,8) NOT NULL,  -- 主动买入成交量 (基础货币)
    taker_buy_quote_asset_volume DECIMAL(18,8) NOT NULL  -- 主动买入成交额 (计价货币)
);