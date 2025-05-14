
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