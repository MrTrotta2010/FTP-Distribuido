CREATE TABLE USERS (
	userName varchar(256) NOT NULL,
	passwd varchar(1024) NOT NULL,
	PRIMARY KEY (userName)
);

CREATE TABLE ADMINS (
	userName varchar(256) NOT NULL,
	passwd varchar(1024) NOT NULL,
	PRIMARY KEY (userName)
);