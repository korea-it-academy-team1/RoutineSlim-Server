CREATE TABLE `member` (
                          `member_id`	BINARY(16)	NOT NULL	COMMENT 'AUTO_INCREMENT',
                          `member_name`	VARCHAR(200)	NOT NULL	DEFAULT UNIQUE,
                          `member_password`	VARCHAR(255)	NOT NULL,
                          `name`	VARCHAR(50)	NOT NULL,
                          `email`	VARCHAR(255)	NOT NULL	DEFAULT UNIQUE,
                          `phone`	VARCHAR(30)	NULL,
                          `created_at`	DATETIME	NOT NULL,
                          `updated_at`	DATETIME	NULL
);

ALTER TABLE `member` ADD CONSTRAINT `PK_MEMBER` PRIMARY KEY (
                                                             `member_id`
    );