SELECT model, speed, hd
FROM pc
WHERE price::numeric < 500;

SELECT DISTINCT maker
FROM product
WHERE type = 'Printer'
ORDER BY maker ASC;

SELECT model, ram, screen
FROM laptop
WHERE price::numeric > 1000;

SELECT *
FROM printer
WHERE color = 'y';

SELECT model, speed, hd
FROM pc
WHERE cd IN ('12x', '24x') AND price::numeric < 600;

SELECT p.maker, l.speed
FROM product p
JOIN laptop l ON p.model = l.model
WHERE l.hd >= 100;

SELECT p.model, pc.price
FROM product p
JOIN pc ON p.model = pc.model
WHERE p.maker = 'B'
UNION
SELECT p.model, l.price
FROM product p
JOIN laptop l ON l.model = p.model
WHERE p.maker = 'B'
UNION
SELECT p.model, pr.price
FROM product p
JOIN printer pr ON p.model = pr.model
WHERE p.maker = 'B';

SELECT DISTINCT p.maker
FROM product p
WHERE p.type = 'PC' AND p.maker NOT IN (
    SELECT p1.maker
    FROM product p1
    WHERE p1.type = 'Laptop'
    );

SELECT DISTINCT p.maker
FROM product p
JOIN pc ON p.model = pc.model
WHERE pc.speed >= 450
ORDER BY p.maker;

SELECT model, price
FROM printer
WHERE price = (SELECT MAX(price) FROM printer);

SELECT AVG(speed)
FROM pc;

SELECT AVG(speed)
FROM laptop
WHERE price::numeric > 1000;

SELECT AVG(speed)
FROM pc
JOIN product p ON p.model = pc.model
WHERE p.maker = 'A';

SELECT speed, AVG(price::numeric)
FROM pc
GROUP BY speed
ORDER BY speed;

SELECT hd
FROM pc
GROUP BY hd
HAVING COUNT(*) >= 2;

SELECT pc1.model as model1, pc2.model as model2, pc1.speed, pc1.ram
FROM pc pc1
JOIN pc pc2 ON pc1.speed = pc2.speed AND pc1.ram = pc2.ram AND pc1.model > pc2.model
ORDER BY model1, model2;

SELECT 'Laptop' as type, model, speed
FROM laptop
WHERE speed < (SELECT MIN(speed) FROM pc);

SELECT DISTINCT p.maker, pr.price
from product p
JOIN printer pr ON p.model = pr.model
WHERE pr.color = 'y' and pr.price = (SELECT MIN(price) from printer WHERE color = 'y');

SELECT p.maker, AVG(l.screen)
FROM product p
JOIN laptop l ON p.model = l.model
GROUP BY p.maker
ORDER BY p.maker;

SELECT maker, COUNT(DISTINCT model) as model_count
FROM product
WHERE type = 'PC'
GROUP BY maker
HAVING COUNT(DISTINCT model) >= 3
ORDER BY maker;

SELECT p.maker, MAX(pc.price) as max_price
FROM product p
JOIN pc ON pc.model = p.model
GROUP BY p.maker
ORDER BY p.maker;

SELECT speed, AVG(price::numeric) as avg_price
FROM pc
WHERE speed > 600
GROUP BY speed
ORDER BY speed;

SELECT DISTINCT maker
FROM product p
JOIN pc on p.model = pc.model
WHERE pc.speed >= 750
INTERSECT
SELECT DISTINCT maker
FROM product p
JOIN laptop l on p.model = l.model
WHERE l.speed >= 750;

WITH MaxPrices AS (
    SELECT MAX(price::numeric) as max_price FROM (
        SELECT price FROM pc
        UNION
        SELECT price FROM laptop
        UNION
        SELECT  price FROM printer
    ) AS all_prices
)
SELECT model
FROM pc
WHERE price::numeric = (SELECT max_price from MaxPrices)
UNION
SELECT model
FROM laptop
WHERE price::numeric = (SELECT max_price from MaxPrices)
UNION
SELECT model
FROM printer
WHERE price::numeric = (SELECT max_price from MaxPrices);

WITH MinRAM AS (
    SELECT MIN(ram) AS min_ram FROM pc
),
FastestInMinRAM AS (
    SELECT MAX(speed) as max_speed
    FROM pc
    WHERE ram = (SELECT min_ram from MinRAM)
)
SELECT DISTINCT p.maker
FROM product p
JOIN pc on pc.model = p.model
WHERE pc.speed = (SELECT max_speed from FastestInMinRAM)
    AND pc.ram = (SELECT min_ram from MinRAM)
    AND p.maker IN (
        SELECT DISTINCT maker
        FROM product
        WHERE type = 'Printer'
    );
