-- ==========================================
-- 인기/랭킹/통계 통합 집계 테이블 (지역/숙소/유형/복합)
-- ==========================================
CREATE TABLE IF NOT EXISTS popularity_stats (
    id BIGINT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY COMMENT '집계 PK',

    stats_type VARCHAR(30) NOT NULL COMMENT '집계 유형: REGION, ACCOMMODATION, TYPE, TYPE_REGION',
    region_type VARCHAR(10) NULL COMMENT '집계 단위: sido(시도), gugun(구군), NULL',
    region_code INT NULL COMMENT '지역 코드: sido_code/gugun_code, NULL',
    accommodation_id BIGINT NULL COMMENT '숙소 PK (ACCOMMODATION 타입일 때만)',
    accommodation_type VARCHAR(50) NULL COMMENT '숙소 유형 (호텔/펜션 등, TYPE, TYPE_REGION일 때)',
    period_type VARCHAR(10) NOT NULL COMMENT '집계 단위: DAILY/WEEKLY/MONTHLY',
    period_value DATE NOT NULL COMMENT '집계 기준일자',

    reservation_count INT DEFAULT 0 COMMENT '예약수',
    view_count INT DEFAULT 0 COMMENT '조회수',
    review_count INT DEFAULT 0 COMMENT '리뷰수',
    popularity_score DOUBLE DEFAULT 0 COMMENT '종합 인기점수(예약*2 + 조회*1 등 가중치)',
    top_n_rank INT DEFAULT NULL COMMENT '랭킹(동일 단위 내 순위)',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '집계 생성일',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '집계 수정일',

    -- [중복 방지, 모든 집계 유형의 고유 row 보장]
    UNIQUE KEY uq_pop_stats(
        stats_type, region_type, region_code, accommodation_id, accommodation_type, period_type, period_value
    ),

    -- [주요 조회] 지역별/유형별/기간별 인기순 랭킹 SELECT에 사용
    INDEX idx_rank_region_period (
        stats_type, region_type, region_code, accommodation_type, period_type, period_value, popularity_score DESC
    ),
    -- [주요 조회] 숙소별 일간/기간 인기 SELECT에 사용
    INDEX idx_acc_period (
        stats_type, accommodation_id, period_type, period_value, popularity_score DESC
    ),
    -- [주요 조회] 랭킹/인기순 top N 빠른 SELECT에 사용
    INDEX idx_rank_topn (
        stats_type, region_type, region_code, accommodation_type, period_type, period_value, top_n_rank ASC
    ),
    -- [복합필터] 유형+지역별 집계, 인기순 SELECT에 사용
    INDEX idx_type_region_period (
        stats_type, accommodation_type, region_type, region_code, period_type, period_value, popularity_score DESC
    ),
    -- [분석/트렌드] 유형별 트렌드 통계 SELECT에 사용
    INDEX idx_type_period (
        stats_type, accommodation_type, period_type, period_value, popularity_score DESC
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='인기/랭킹/통계 통합 집계 테이블(지역, 숙소, 유형별 랭킹/트렌드)';
