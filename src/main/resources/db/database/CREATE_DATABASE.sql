CREATE USER 'routineslim'@'<IP>' IDENTIFIED BY '<비밀번호>';
GRANT ALL PRIVILEGES ON routineslim.* TO 'routineslim'@'<IP>';
SHOW GRANTS FOR 'routineslim'@'<IP>';

CREATE DATABASE routineslim DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;