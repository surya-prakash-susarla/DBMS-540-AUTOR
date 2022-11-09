INSERT INTO CREDENTIALS VALUES('132457689','James','Williams','employees');
INSERT INTO EMPLOYEES VALUES('132457689', 'James', 'Williams', '1400 Gorman St, Raleigh, NC 27606-2972', 'Mechanics', 35.0, '30001', 'james@random.com');

INSERT INTO CREDENTIALS VALUES('314275869','David','Johnson','employees');
INSERT INTO EMPLOYEES VALUES('314275869', 'David', 'Johnson', '686 Stratford Court, Raleigh, NC 27606', 'Mechanics', 35.0, '30001', 'davic@random.com');

INSERT INTO CREDENTIALS VALUES('241368579','Maria','Garcia','employees');
INSERT INTO EMPLOYEES VALUES('241368579', 'Maria', 'Garcia', '1521 Graduate Lane, Raleigh, NC 27606', 'Mechanics', 35.0, '30001', 'maria@random.com');

INSERT INTO CREDENTIALS VALUES('241368580','Random','Garcio','employees');
INSERT INTO EMPLOYEES VALUES('241368580', 'Random', 'Garcio', '1521 Graduate Lane, Raleigh, NC 27606', 'Mechanics', 35.0, '30001', 'random@random.com');

INSERT INTO CREDENTIALS VALUES('423186759','Ellie','Clark','employees');
INSERT INTO EMPLOYEES VALUES('423186759', 'Ellie', 'Clark', '3125 Avent Ferry Road, Raleigh, NC 27605', 'Mechanics', 25.0, '30002', 'ellie@random.com');

INSERT INTO CREDENTIALS VALUES('123789456','Robert','Martinez','employees');
INSERT INTO EMPLOYEES VALUES('123789456', 'Robert', 'Martinez', '1232 Tartan Circle, Raleigh, NC 27607', 'Mechanics', 25.0, '30002', 'robert@random.com');

INSERT INTO CREDENTIALS VALUES('789123456','Charles','Rodriguez','employees');
INSERT INTO EMPLOYEES VALUES('789123456', 'Charles', 'Rodriguez', '218 Patton Lane, Raleigh, NC 27603', 'Mechanics', 25.0, '30002', 'charles@random.com');

INSERT INTO CREDENTIALS VALUES('125689347','Jose','Hernandez','employees');
INSERT INTO EMPLOYEES VALUES('125689347', 'Jose', 'Hernandez', '4747 Dola Mine Road, Raleigh, NC 27609', 'Mechanics', 25.0, '30002', 'jose@random.com');

INSERT INTO CREDENTIALS VALUES('347812569','Charlie','Brown','employees');
INSERT INTO EMPLOYEES VALUES('347812569', 'Charlie', 'Brown', '1 Rockford Mountain Lane, Morrisville, NC 27560', 'Mechanics', 22.0, '30003', 'charlie@random.com');

INSERT INTO CREDENTIALS VALUES('123456780','Jeff','Gibson','employees');
INSERT INTO EMPLOYEES VALUES('123456780', 'Jeff', 'Gibson', '900 Development Drive, Morrisville, NC 27560', 'Mechanics', 22.0, '30003', 'jeff@random.com');

INSERT INTO CREDENTIALS VALUES('123456708','Isabelle','Wilder','employees');
INSERT INTO EMPLOYEES VALUES('123456708', 'Isabelle', 'Wilder', '6601 Koppers Road, Morrisville, NC 27560', 'Mechanics', 22.0, '30003', 'isabelle@random.com');

INSERT INTO CREDENTIALS VALUES('123456078','Peter','Titus','employees');
INSERT INTO EMPLOYEES VALUES('123456078', 'Peter', 'Titus', '2860 Slater Road, Morrisville, NC 27560', 'Mechanics', 22.0, '30003', 'peter@random.com');

INSERT INTO CREDENTIALS VALUES('123450678','Mark','Mendez','employees');
INSERT INTO EMPLOYEES VALUES('123450678', 'Mark', 'Mendez', '140 Southport Drive, Morrisville, NC 27560', 'manager', 22.0, '30003', 'mark@random.com');

INSERT INTO CREDENTIALS VALUES('123405678','Lisa','Alberti','employees');
INSERT INTO EMPLOYEES VALUES('123405678', 'Lisa', 'Alberti', '100 Valley Glen Drive, Morrisville, NC 27560', 'receptionist', 22.0, '30003', 'lisa@random.com');

INSERT INTO CREDENTIALS VALUES('123','Lisa','Alberti','customer');
INSERT INTO CUSTOMER VALUES('123','Lisa','Alberti',DEFAULT,DEFAULT,1,'30001');
INSERT INTO CAR values('v10','123','Honda',53000,100,100);




INSERT INTO CREDENTIALS VALUES('10001','Peter','Parker','customer');
INSERT INTO CUSTOMER VALUES('10001','Peter','Parker',DEFAULT,DEFAULT,1,'30001');
INSERT INTO CAR values('4Y1BL658','10001','Toyota',53000,2007,1);

INSERT INTO CREDENTIALS VALUES('10002','Diana','Prince','customer');
INSERT INTO CUSTOMER VALUES('10002','Diana','Prince',DEFAULT,DEFAULT,1,'30001');
INSERT INTO CAR values('7A1ST264','10002','Honda',117000,1999,0);

INSERT INTO CREDENTIALS VALUES('10053','Billy','Batson','customer');
INSERT INTO CUSTOMER VALUES('10053','Billy','Batson',DEFAULT,DEFAULT,1,'30002');
INSERT INTO CAR values('5TR3K914','10053','Nissan',111000,2015,2);

INSERT INTO CREDENTIALS VALUES('10012','Bruce','Wayne','customer');
INSERT INTO CUSTOMER VALUES('10012','Bruce','Wayne',DEFAULT,DEFAULT,1,'30002');
INSERT INTO CAR values('15DC9A87','10012','Toyota',21000,2020,0);

INSERT INTO CREDENTIALS VALUES('10111','Steve','Rogers','customer');
INSERT INTO CUSTOMER VALUES('10111','Steve','Rogers',DEFAULT,DEFAULT,1,'30002');
INSERT INTO CAR values('18S5R2D8','10111','Nissan',195500,2019,2);

INSERT INTO CREDENTIALS VALUES('10035','Happy','Hogan','customer');
INSERT INTO CUSTOMER VALUES('10035','Happy','Hogan',DEFAULT,DEFAULT,1,'30002');
INSERT INTO CAR values('9R2UHP54','10035','Honda',67900,2013,1);

INSERT INTO CREDENTIALS VALUES('10022','Tony','Stark','customer');
INSERT INTO CUSTOMER VALUES('10022','Tony','Stark',DEFAULT,DEFAULT,1,'30003');
INSERT INTO CAR values('88TSM888','10022','Honda',10000,2000,0);

INSERT INTO CREDENTIALS VALUES('10003','Natasha','Romanoff','customer');
INSERT INTO CUSTOMER VALUES('10003','Natasha','Romanoff',DEFAULT,DEFAULT,1,'30003');
INSERT INTO CAR values('71HK2D89','10003','Toyota',195550,2004,1);

INSERT INTO CREDENTIALS VALUES('10011','Harvey','Bullock','customer');
INSERT INTO CUSTOMER VALUES('10011','Harvey','Bullock',DEFAULT,DEFAULT,1,'30003');
INSERT INTO CAR values('34KLE19D','10011','Toyota',123800,2010,2);

INSERT INTO CREDENTIALS VALUES('10062','Sam','Wilson','customer');
INSERT INTO CUSTOMER VALUES('10062','Sam','Wilson',DEFAULT,DEFAULT,1,'30003');
INSERT INTO CAR values('29T56WC3','10062','Nissan',51300,2011,0);

INSERT INTO CREDENTIALS VALUES('10501','Wanda','Maximoff','customer');
INSERT INTO CUSTOMER VALUES('10501','Wanda','Maximoff',DEFAULT,DEFAULT,1,'30003');
INSERT INTO CAR values('P39VN198','10501','Nissan',39500,2013,1);

INSERT INTO CREDENTIALS VALUES('10010','Virginia','Potts','customer');
INSERT INTO CUSTOMER VALUES('10010','Virginia','Potts',DEFAULT,DEFAULT,1,'30003');
INSERT INTO CAR values('39YVS415','10010','Honda',49900,2021,0);
commit;