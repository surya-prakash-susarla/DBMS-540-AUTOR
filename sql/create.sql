CREATE TABLE SERVICE_CENTER(
  service_center_id VARCHAR(20) NOT NULL, 
  address VARCHAR2(50),
  telephone VARCHAR2(10),
  min_wage REAL,
  max_wage REAL,
  saturday NUMBER(1),
  CONSTRAINT pk_service_center PRIMARY KEY (service_center_id),
  check(saturday = 0 or saturday = 1)
);

CREATE TABLE CREDENTIALS(
  userid VARCHAR(9) PRIMARY KEY,
  username VARCHAR2(20) NOT NULL,
  pwd VARCHAR2(20) NOT NULL,
  role VARCHAR2(20), -- Which table is this user present
  check(role = 'customer' or role = 'employees')
);

CREATE TABLE EMPLOYEES(
  employee_id VARCHAR(9), 
  firstName VARCHAR2(20),
  lastName VARCHAR2(20),
  address VARCHAR2(50),
  role VARCHAR2(30),
  salary REAL,
  service_center_id VARCHAR(20),
  email VARCHAR2(100) DEFAULT NULL,
  CONSTRAINT pk_employees PRIMARY KEY(employee_id),
  CONSTRAINT fk_service_center_id FOREIGN KEY (service_center_id) REFERENCES SERVICE_CENTER(service_center_id),
  CONSTRAINT fk_emp_uid FOREIGN KEY (employee_id) REFERENCES CREDENTIALS(userid)
);

CREATE TABLE SERVICES(
  service_id VARCHAR(20),
  CONSTRAINT pk_services PRIMARY KEY (service_id)
);

CREATE TABLE CUSTOMER(
    cid VARCHAR(9),
    firstName VARCHAR2(20),
    lastName VARCHAR2(20),
    address VARCHAR2(100) DEFAULT '3212 Downtown, NY',
    emailAddress VARCHAR2(100) DEFAULT 'kogan@ncsu.edu',
    status NUMBER(1), /* No boolean type in Oracle. Use number instead. 0 - inactive, 1 - active and good standing, 2 - active and not good standing. */
    service_center_id VARCHAR(20),
    CONSTRAINT pk_customer PRIMARY KEY(cid),
    CONSTRAINT fk_cid FOREIGN KEY (cid) REFERENCES CREDENTIALS(userid),
    -- CONSTRAINT fk_cid FOREIGN KEY (cid) REFERENCES customer(cid),
    CONSTRAINT fk_customer_service_id FOREIGN KEY (service_center_id) REFERENCES SERVICE_CENTER(service_center_id),
    check(status = 0 or status = 1 or status = 2)
);

CREATE TABLE MANUFACTURER(
    manufacturerName VARCHAR2(20) PRIMARY KEY
);
    
CREATE TABLE CAR(
  vin VARCHAR2(17), 
  cid VARCHAR(9),
  manufacturerName VARCHAR2(20) , 
  mileage NUMBER(20),
  year NUMBER(4),
  lastmaintenance NUMBER(20),
  CONSTRAINT fk_cid_car FOREIGN KEY (cid) REFERENCES CUSTOMER(cid),
  CONSTRAINT pk_car PRIMARY KEY(vin),
  constraint fk_man_car FOREIGN KEY (manufacturerName) REFERENCES MANUFACTURER(manufacturerName)
);

CREATE TABLE SERVICE_TIME(
  manufacturerName VARCHAR2(20),
  service_id VARCHAR(20),
  time NUMBER(30),
  CONSTRAINT pk_service_time PRIMARY KEY (manufacturerName, service_id),
  CONSTRAINT fk_service_time_service_id FOREIGN KEY (service_id) REFERENCES SERVICES(service_id),
  CONSTRAINT fk_service_time_manufacturer FOREIGN KEY (manufacturerName) REFERENCES MANUFACTURER(manufacturerName)
);

CREATE TABLE SERVICE_COST(
    service_id VARCHAR(20), 
    amount NUMBER(20),
    manufacturerName VARCHAR2(20),
    service_center_id VARCHAR(20), 
    CONSTRAINT fk_serviceid FOREIGN KEY (service_id) REFERENCES SERVICES(service_id), 
    CONSTRAINT fk_servicecenterid FOREIGN KEY (service_center_id) REFERENCES SERVICE_CENTER(service_center_id),
    CONSTRAINT fk_manufacturer FOREIGN KEY (manufacturerName) REFERENCES MANUFACTURER(manufacturerName),
    CONSTRAINT pk_service_cost PRIMARY KEY(service_id,manufacturerName,service_center_id)
);

CREATE TABLE INVOICE(
  cid VARCHAR(9), -- Customer id
  service_string VARCHAR2(70),
  invoice_date VARCHAR(300),
  invoice_id VARCHAR2(100),
  mechanics VARCHAR2(30), -- A string of concatenated employee ids
  status NUMBER(1) DEFAULT 0, -- 0 for unpaid, 1 for paid
  costs REAL,
  vin VARCHAR2(17),
  CONSTRAINT pk_invoice PRIMARY KEY (invoice_id),
  CONSTRAINT fk_invoice_cid FOREIGN KEY (cid) REFERENCES CUSTOMER(cid),
  check(status = 0 or status = 1)
);

CREATE TABLE SCHEDULE(
  service_center_id VARCHAR(20),
  -- Each month will have week ids 1 through 4, and each day of the week will correspond to an integer id as well e.g. Sunday = 1, Monday = 2, …. Saturday = 7. Therefore, we can easily identify Saturdays by day id.
  week NUMBER(2),
  day NUMBER(2),
  --Each day has 9 time slots (8am - 9am is slot 1, 9am - 10am is slot 2, and so on). Therefore a service that lasts 2 hours takes two timeslots. The lunch period is the 12 - 1pm time slot and no work is done during that period.
  slot_start NUMBER(6),
  slot_end NUMBER(6),
  emp_id VARCHAR(9),
  invoice_id VARCHAR(100),
  CONSTRAINT pk_schedule PRIMARY KEY (emp_id, week, day, slot_start, slot_end),
  CONSTRAINT fk_sched_sc_id FOREIGN KEY (service_center_id) REFERENCES SERVICE_CENTER(service_center_id),
  CONSTRAINT fk_emp_id FOREIGN KEY (emp_id) REFERENCES EMPLOYEES(employee_id),
  CONSTRAINT fk_service_id FOREIGN KEY (invoice_id) REFERENCES INVOICE(invoice_id)
);

CREATE TABLE MAINTENANCE_SERVICE(
  service_id VARCHAR(20),
  service_names VARCHAR(500),
  CONSTRAINT pk_maintenance_service PRIMARY KEY (service_id),
  CONSTRAINT fk_maintenance_service_service FOREIGN KEY (service_id) REFERENCES SERVICES(service_id)
);

CREATE TABLE INDIVIDUAL_SERVICES(
  service_name VARCHAR(20),
  service_id VARCHAR(20), 
  CONSTRAINT pk_individual_services PRIMARY KEY (service_id),
  CONSTRAINT fk_individual_services_services FOREIGN KEY (service_id) REFERENCES SERVICES(service_id)
);

CREATE TABLE SWAPS(
  -- TODO: This is expected to be an auto-incrementing primary key, need to verify if the below works on our SQL.
  swap_id NUMBER GENERATED ALWAYS as IDENTITY(START with 1 INCREMENT by 1),
  source_emp_id VARCHAR(9),
  source_slot_week NUMBER(2),
  source_slot_day NUMBER(2),
  source_slot_start NUMBER(6),
  source_slot_end NUMBER(6),
  target_emp_id VARCHAR(9),
  target_slot_week NUMBER(2),
  target_slot_day NUMBER(2),
  target_slot_start NUMBER(6),
  target_slot_end NUMBER(6),
  -- 'status' can have 3 values: 0 -> requested, 1 -> accepted, 2-> rejected
  status NUMBER(3),
  CONSTRAINT pk_swaps PRIMARY KEY (swap_id),
  CONSTRAINT fk_source_emp_id FOREIGN KEY (source_emp_id) REFERENCES EMPLOYEES(employee_id),
  CONSTRAINT fk_target_emp_id FOREIGN KEY (target_emp_id) REFERENCES EMPLOYEES(employee_id),
  check(status = 0 or status = 1 or status = 2)
);

INSERT INTO MANUFACTURER VALUES(
  'Honda'
); 
INSERT INTO MANUFACTURER VALUES(
  'Nissan'
); 
INSERT INTO MANUFACTURER VALUES(
  'Toyota'
); 
INSERT INTO MANUFACTURER VALUES(
  'Lexus'
); 
INSERT INTO MANUFACTURER VALUES(
  'Infiniti'
); 
INSERT INTO INVOICE VALUES(
  NULL, NULL, NULL, '-1', NULL, 1, NULL, NULL
);

commit;
