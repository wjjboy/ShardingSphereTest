-- db1
CREATE DATABASE `db1`;
CREATE TABLE `db1.t_a` (
   `id` int(11) NOT NULL,
   `name` varchar(100) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO db1.t_a (id, name) VALUES(1, 'A');
INSERT INTO db1.t_a (id, name) VALUES(2, 'B');
INSERT INTO db1.t_a (id, name) VALUES(3, 'C');

-- db2
CREATE DATABASE `db2`;
CREATE TABLE `db2.t_b` (
   `id` int(11) NOT NULL,
   `value` varchar(100) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO db2.t_b (id, value) VALUES(1, 'X');
INSERT INTO db2.t_b (id, value) VALUES(3, 'Z');

-- db3
CREATE DATABASE `db3`;
CREATE TABLE `db3.t_b` (
   `id` int(11) NOT NULL,
   `value` varchar(100) DEFAULT NULL,
   PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO db3.t_b (id, value) VALUES(2, 'Y');
