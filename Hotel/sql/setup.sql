SELECT pg_terminate_backend(pid)
FROM pg_stat_activity
WHERE datname = 'hotel'
  AND pid <> pg_backend_pid();

SELECT pg_sleep(1);

DROP DATABASE IF EXISTS hotel;

CREATE DATABASE hotel