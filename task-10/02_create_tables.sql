CREATE TABLE Product (
    maker varchar(10) NOT NULL,
    model varchar(50) PRIMARY KEY,
    type varchar(50) NOT NULL CHECK (type IN ('PC', 'Laptop', 'Printer'))
);

CREATE TABLE PC (
    code SERIAL PRIMARY KEY,
    model varchar(50) NOT NULL REFERENCES Product(model) ON DELETE CASCADE,
    speed smallint NOT NULL,
    ram real NOT NULL,
    hd real NOT NULL,
    cd varchar(10) NOT NULL,
    price money
);

CREATE TABLE Laptop (
    code SERIAL PRIMARY KEY,
    model varchar(50) NOT NULL REFERENCES Product(model) ON DELETE CASCADE,
    speed smallint NOT NULL ,
    ram real NOT NULL ,
    hd real NOT NULL ,
    screen smallint CHECK ( screen BETWEEN 0 AND 255) NOT NULL,
    price money
);

CREATE TABLE Printer (
    code SERIAL PRIMARY KEY,
    model varchar(50) NOT NULL REFERENCES Product(model) ON DELETE CASCADE,
    color char(1) NOT NULL CHECK ( color IN ('y', 'n') ),
    type varchar(10) NOT NULL CHECK ( type IN ('Laser', 'Jet', 'Matrix') ),
    price money
);