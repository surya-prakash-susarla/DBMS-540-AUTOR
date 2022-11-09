create trigger chkreceptionist BEFORE insert on EMPLOYEES
FOR EACH ROW
when (new.role = 'receptionist')
DECLARE
    res number;    
BEGIN
    res:=0;
    select count(*) into res from EMPLOYEES where exists(select * from EMPLOYEES e1 where e1.role='receptionist' group by e1.service_center_id having count(*) > 0); 
    IF  res > 0
    THEN
    raise_application_error(-20000, 'Store cannot have more than one receptionist');
    END IF;
END;
/