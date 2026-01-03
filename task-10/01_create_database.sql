SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'task10'
  AND pid <> pg_backend_pid();

SELECT pg_sleep(1);

DROP DATABASE IF EXISTS task10;

CREATE DATABASE task10;