-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
-- -----------------------------------------------------
-- Schema ssafytrip
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema ssafytrip
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `ssafytrip` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci ;
USE `ssafytrip` ;

-- -----------------------------------------------------
-- Table `ssafytrip`.`sidos`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ssafytrip`.`sidos` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '시도번호',
  `sido_code` INT NOT NULL COMMENT '시도코드',
  `sido_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '시도이름',
  PRIMARY KEY (`no`),
  UNIQUE INDEX `sido_code_UNIQUE` (`sido_code` ASC) VISIBLE)
ENGINE = InnoDB
AUTO_INCREMENT = 35
DEFAULT CHARACTER SET = utf8mb4
COMMENT = '시도정보테이블';


-- -----------------------------------------------------
-- Table `ssafytrip`.`guguns`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ssafytrip`.`guguns` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '구군번호',
  `sido_code` INT NOT NULL COMMENT '시도코드',
  `gugun_code` INT NOT NULL COMMENT '구군코드',
  `gugun_name` VARCHAR(20) NULL DEFAULT NULL COMMENT '구군이름',
  PRIMARY KEY (`no`),
  INDEX `guguns_sido_to_sidos_cdoe_fk_idx` (`sido_code` ASC) VISIBLE,
  INDEX `gugun_code_idx` (`gugun_code` ASC) VISIBLE,
  CONSTRAINT `guguns_sido_to_sidos_cdoe_fk`
    FOREIGN KEY (`sido_code`)
    REFERENCES `ssafytrip`.`sidos` (`sido_code`))
ENGINE = InnoDB
AUTO_INCREMENT = 469
DEFAULT CHARACTER SET = utf8mb4
COMMENT = '구군정보테이블';


-- -----------------------------------------------------
-- Table `ssafytrip`.`contenttypes`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ssafytrip`.`contenttypes` (
  `content_type_id` INT NOT NULL COMMENT '콘텐츠타입번호',
  `content_type_name` VARCHAR(45) NULL DEFAULT NULL COMMENT '콘텐츠타입이름',
  PRIMARY KEY (`content_type_id`))
ENGINE = InnoDB
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '콘텐츠타입정보테이블';


-- -----------------------------------------------------
-- Table `ssafytrip`.`attractions`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ssafytrip`.`attractions` (
  `no` INT NOT NULL AUTO_INCREMENT COMMENT '명소코드',
  `content_id` INT NULL DEFAULT NULL COMMENT '콘텐츠번호',
  `title` VARCHAR(500) NULL DEFAULT NULL COMMENT '명소이름',
  `content_type_id` INT NULL DEFAULT NULL COMMENT '콘텐츠타입',
  `area_code` INT NULL DEFAULT NULL COMMENT '시도코드',
  `si_gun_gu_code` INT NULL DEFAULT NULL COMMENT '구군코드',
  `first_image1` VARCHAR(100) NULL DEFAULT NULL COMMENT '이미지경로1',
  `first_image2` VARCHAR(100) NULL DEFAULT NULL COMMENT '이미지경로2',
  `map_level` INT NULL DEFAULT NULL COMMENT '줌레벨',
  `latitude` DECIMAL(20,17) NULL DEFAULT NULL COMMENT '위도',
  `longitude` DECIMAL(20,17) NULL DEFAULT NULL COMMENT '경도',
  `tel` VARCHAR(20) NULL DEFAULT NULL COMMENT '전화번호',
  `addr1` VARCHAR(100) NULL DEFAULT NULL COMMENT '주소1',
  `addr2` VARCHAR(100) NULL DEFAULT NULL COMMENT '주소2',
  `homepage` VARCHAR(1000) NULL DEFAULT NULL COMMENT '홈페이지',
  `overview` VARCHAR(10000) NULL DEFAULT NULL COMMENT '설명',
  PRIMARY KEY (`no`),
  INDEX `attractions_typeid_to_types_typeid_fk_idx` (`content_type_id` ASC) VISIBLE,
  INDEX `attractions_sido_to_sidos_code_fk_idx` (`area_code` ASC) VISIBLE,
  INDEX `attractions_sigungu_to_guguns_gugun_fk_idx` (`si_gun_gu_code` ASC) VISIBLE,
  CONSTRAINT `attractions_area_to_sidos_code_fk`
    FOREIGN KEY (`area_code`)
    REFERENCES `ssafytrip`.`sidos` (`sido_code`),
  CONSTRAINT `attractions_sigungu_to_guguns_gugun_fk`
    FOREIGN KEY (`si_gun_gu_code`)
    REFERENCES `ssafytrip`.`guguns` (`gugun_code`),
  CONSTRAINT `attractions_typeid_to_types_typeid_fk`
    FOREIGN KEY (`content_type_id`)
    REFERENCES `ssafytrip`.`contenttypes` (`content_type_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 107559
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci
COMMENT = '명소정보테이블';


-- -----------------------------------------------------
-- Table `ssafytrip`.`plan`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ssafytrip`.`plan` (
  `plan_id` INT NOT NULL AUTO_INCREMENT,
  `member_id` INT NOT NULL,
  PRIMARY KEY (`plan_id`),
  INDEX `fk_travel_member1_idx` (`member_id` ASC) VISIBLE,
  CONSTRAINT `fk_travel_member1`
    FOREIGN KEY (`member_id`)
    REFERENCES `ssafytrip`.`member` (`mno`))
ENGINE = InnoDB
AUTO_INCREMENT = 3
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


-- -----------------------------------------------------
-- Table `ssafytrip`.`plan_attraction`
-- -----------------------------------------------------
CREATE TABLE IF NOT EXISTS `ssafytrip`.`plan_attraction` (
  `plan_attraction_id` INT NOT NULL AUTO_INCREMENT,
  `attraction_id` INT NOT NULL,
  `plan_id` INT NOT NULL,
  PRIMARY KEY (`plan_attraction_id`),
  INDEX `fk_plan_attraction_attractions1_idx` (`attraction_id` ASC) VISIBLE,
  INDEX `fk_plan_attraction_plan1_idx` (`plan_id` ASC) VISIBLE,
  CONSTRAINT `fk_plan_attraction_attractions1`
    FOREIGN KEY (`attraction_id`)
    REFERENCES `ssafytrip`.`attractions` (`no`),
  CONSTRAINT `fk_plan_attraction_plan1`
    FOREIGN KEY (`plan_id`)
    REFERENCES `ssafytrip`.`plan` (`plan_id`))
ENGINE = InnoDB
AUTO_INCREMENT = 9
DEFAULT CHARACTER SET = utf8mb4
COLLATE = utf8mb4_0900_ai_ci;


SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;