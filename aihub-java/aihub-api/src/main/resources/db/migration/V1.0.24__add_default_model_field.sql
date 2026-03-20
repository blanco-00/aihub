-- 添加默认模型字段
ALTER TABLE model_config ADD COLUMN is_default TINYINT(1) DEFAULT 0 COMMENT '是否为默认模型';

-- 确保只有一个默认模型（触发器）
DELIMITER //
CREATE TRIGGER trg_ensure_single_default_model
BEFORE INSERT ON model_config
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 THEN
        UPDATE model_config SET is_default = 0 WHERE is_default = 1;
    END IF;
END//
;

CREATE TRIGGER trg_ensure_single_default_model_update
BEFORE UPDATE ON model_config
FOR EACH ROW
BEGIN
    IF NEW.is_default = 1 AND OLD.is_default = 0 THEN
        UPDATE model_config SET is_default = 0 WHERE is_default = 1;
    END IF;
END//
;
DELIMITER ;
