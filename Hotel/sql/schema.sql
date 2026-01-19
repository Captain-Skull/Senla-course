CREATE TABLE rooms (
    number int PRIMARY KEY,
    room_type varchar(12) CHECK ( room_type IN ('ECONOM', 'STANDARD', 'LUXURY', 'PRESIDENTIAL') ),
    price int NOT NULL,
    capacity int NOT NULL,
    room_status varchar(11) CHECK ( room_status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'CLEANING') ) DEFAULT 'AVAILABLE',
    end_date date NOT NULL DEFAULT current_date,
    days_under_status int NOT NULL DEFAULT 0
);

CREATE SEQUENCE IF NOT EXISTS services_seq START 1;

CREATE TABLE services (
    id varchar(3) PRIMARY KEY DEFAULT 'S' || nextval('services_seq'),
    name varchar(50) NOT NULL ,
    price int NOT NULL,
    description text
);

CREATE SEQUENCE IF NOT EXISTS guests_seq START 1;

CREATE TABLE guests (
    id varchar(4) PRIMARY KEY DEFAULT 'G' || nextval('guests_seq'),
    firstname varchar(20) NOT NULL ,
    lastname varchar(20) NOT NULL ,
    room_number int NOT NULL
);

CREATE TABLE guest_service_usage (
    id int PRIMARY KEY ,
    guest_id varchar(4) NOT NULL ,
    service_id varchar(3) NOT NULL ,
    usage_date date NOT NULL
);

CREATE TABLE room_guests_history (
    id varchar(4) PRIMARY KEY ,
    firstname varchar(20) NOT NULL ,
    lastname varchar(20) NOT NULL ,
    room_number int NOT NULL,
    guest_group_id int NOT NULL
);