CREATE USER 'routineslim'@'<IP>' IDENTIFIED BY '<비밀번호>';
GRANT ALL PRIVILEGES ON routineslim.* TO 'routineslim'@'<IP>';
SHOW GRANTS FOR 'routineslim'@'<IP>';