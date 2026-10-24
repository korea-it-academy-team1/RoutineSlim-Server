CREATE TABLE `expense` (
	`expense_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`member_id`	BIGINT	NOT NULL,
	`expense_method_id`	BIGINT	NOT NULL,
	`amount`	INT	NOT NULL,
	`description`	VARCHAR(250)	NULL,
	`expense_date`	DATE	NOT NULL
);



CREATE TABLE `schedule` (
	`schedule_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`member_id`	BIGINT	NOT NULL,
	`title`	VARCHAR(100)	NOT NULL,
	`amount`	INT	NOT NULL,
	`category`	VARCHAR(30)	NOT NULL,
	`payment_date`	DATETIME	NOT NULL,
	`description`	TEXT	NULL,
	`created_at`	DATETIME	NOT NULL,
	`updated_at`	DATETIME	NULL,
	`repeat_type`	VARCHAR(20)	NOT NULL	COMMENT 'NONE, MONTHLY, YEARLY',
	`payment_status`	VARCHAR(20)	NOT NULL	COMMENT 'PENDING, COMPLETED, CANCELED',
	`end_date`	DATE	NULL
);

CREATE TABLE `subscription_category` (
	`sub_ctg_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`category_name`	VARCHAR(50)	NOT NULL	DEFAULT UNIQUE
);

CREATE TABLE `refresh_token` (
	`token_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`member_id`	BIGINT	NOT NULL,
	`token_hash`	VARCHAR(255)	NOT NULL	COMMENT 'UNIQUE',
	`expires_at`	DATETIME	NOT NULL,
	`created_at`	DATETIME	NOT NULL,
	`revoked_at`	DATETIME	NULL
);

CREATE TABLE `subscription` (
	`sub_id`	BINARY(16)	NOT NULL,
	`member_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`sub_ctg_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`name`	VARCHAR(100)	NOT NULL,
	`amount`	INT UNSIGNED	NOT NULL,
	`currency`	CHAR(3)	NOT NULL,
	`billing_cycle`	VARCHAR(10)	NOT NULL,
	`cycle_interval`	SMALLINT UNSIGNED	NOT NULL,
	`start_date`	DATE	NOT NULL,
	`billing_day`	TINYINT UNSIGNED	NOT NULL,
	`next_billing_date`	DATE	NOT NULL,
	`status`	VARCHAR(10)	NOT NULL,
	`trial_end_date`	DATE	NULL,
	`notify_enabled`	TINYINT(1)	NOT NULL,
	`notify_days_before`	TINYINT UNSIGNED	NULL,
	`memo`	TEXT	NULL,
	`canceled_at`	DATETIME(3)	NULL,
	`created_at`	DATETIME(3)	NOT NULL,
	`updated_at`	DATETIME(3)	NOT NULL,
	`deleted_at`	DATETIME(3)	NULL
);

CREATE TABLE `categories` (
	`categories_id`	BINARY(16)	NOT NULL,
	`member_id`	BINARY(16)	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`name`	VARCHAR(50)	NULL,
	`color`	CHAR(7)	NULL,
	`icon`	VARCHAR(50)	NULL,
	`sort_order`	SMALLINT	NULL
);

CREATE TABLE `expense_method` (
	`expense_method_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`method_name`	VARCHAR(30)	NOT NULL	COMMENT 'UNIQUE'
);

CREATE TABLE `recommendation` (
	`rec_id`	BINARY(16)	NOT NULL,
	`sub_id`	BINARY(16)	NOT NULL,
	`member_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`title`	VARCHAR(255)	NULL,
	`rule_code`	VARCHAR(255)	NULL,
	`saving_amount`	VARCHAR(255)	NULL,
	`status`	VARCHAR(255)	NULL,
	`created_at`	VARCHAR(255)	NULL
);

CREATE TABLE `device` (
	`device_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`member_id`	BIGINT	NOT NULL,
	`fcm_token`	VARCHAR(512)	NOT NULL	COMMENT 'UNIQUE',
	`notification_enabled`	BOOLEAN	NOT NULL
);

CREATE TABLE `comment` (
	`comment_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`post_id`	BIGINT	NOT NULL,
	`content`	TEXT	NOT NULL,
	`created_at`	DATETIME	NOT NULL,
	`updated_at`	DATETIME	NULL
);

CREATE TABLE `service_preset` (
	`preset_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`sub_ctg_id`	BIGINT	NOT NULL,
	`service_name`	VARCHAR(100)	NOT NULL,
	`logo_url`	VARCHAR(500)	NULL
);

CREATE TABLE `post` (
	`post_id`	BIGINT	NOT NULL	COMMENT 'AUTO_INCREMENT',
	`member_id`	BIGINT	NOT NULL,
	`title`	VARCHAR(200)	NOT NULL,
	`content`	TEXT	NOT NULL,
	`created_at`	DATETIME	NOT NULL,
	`updated_at`	DATETIME	NULL
);

ALTER TABLE `expense` ADD CONSTRAINT `PK_EXPENSE` PRIMARY KEY (
	`expense_id`
);

ALTER TABLE `schedule` ADD CONSTRAINT `PK_SCHEDULE` PRIMARY KEY (
	`schedule_id`
);

ALTER TABLE `subscription_category` ADD CONSTRAINT `PK_SUBSCRIPTION_CATEGORY` PRIMARY KEY (
	`sub_ctg_id`
);

ALTER TABLE `refresh_token` ADD CONSTRAINT `PK_REFRESH_TOKEN` PRIMARY KEY (
	`token_id`
);

ALTER TABLE `subscription` ADD CONSTRAINT `PK_SUBSCRIPTION` PRIMARY KEY (
	`sub_id`
);

ALTER TABLE `categories` ADD CONSTRAINT `PK_CATEGORIES` PRIMARY KEY (
	`categories_id`
);

ALTER TABLE `expense_method` ADD CONSTRAINT `PK_EXPENSE_METHOD` PRIMARY KEY (
	`expense_method_id`
);

ALTER TABLE `recommendation` ADD CONSTRAINT `PK_RECOMMENDATION` PRIMARY KEY (
	`rec_id`
);

ALTER TABLE `device` ADD CONSTRAINT `PK_DEVICE` PRIMARY KEY (
	`device_id`
);

ALTER TABLE `comment` ADD CONSTRAINT `PK_COMMENT` PRIMARY KEY (
	`comment_id`
);

ALTER TABLE `service_preset` ADD CONSTRAINT `PK_SERVICE_PRESET` PRIMARY KEY (
	`preset_id`
);

ALTER TABLE `post` ADD CONSTRAINT `PK_POST` PRIMARY KEY (
	`post_id`
);

