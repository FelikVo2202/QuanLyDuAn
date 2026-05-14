
ALTER TABLE Appointment ADD (
    EndDateTime TIMESTAMP
);

UPDATE Appointment
SET EndDateTime = AppointmentDateTime
WHERE EndDateTime IS NULL;

ALTER TABLE Appointment MODIFY (
    EndDateTime TIMESTAMP NOT NULL
);

ALTER TABLE Appointment ADD (
    CONSTRAINT chk_appointment_time CHECK (EndDateTime > AppointmentDateTime)
);

CREATE OR REPLACE TRIGGER trg_prevent_past_appointment
BEFORE INSERT OR UPDATE ON Appointment
FOR EACH ROW
BEGIN
    IF :NEW.AppointmentDateTime < SYSTIMESTAMP THEN
        RAISE_APPLICATION_ERROR(-20004, 'Appointment time cannot be in the past.');
    END IF;
END;
/

CREATE OR REPLACE TRIGGER trg_prevent_double_booking
BEFORE INSERT OR UPDATE ON Appointment
FOR EACH ROW
DECLARE
    v_overlap_count NUMBER;
    v_lock_dummy NUMBER;
BEGIN
    IF :NEW.Status != 'CANCELED' THEN
        SELECT StaffID INTO v_lock_dummy 
        FROM Staff 
        WHERE StaffID = :NEW.StaffID 
        FOR UPDATE; 

        SELECT COUNT(*)
        INTO v_overlap_count
        FROM Appointment
        WHERE StaffID = :NEW.StaffID
                    AND Status != 'CANCELED'
          AND :NEW.AppointmentDateTime < EndDateTime 
          AND :NEW.EndDateTime > AppointmentDateTime
          AND AppointmentID != NVL(:NEW.AppointmentID, -1);

        IF v_overlap_count > 0 THEN
            RAISE_APPLICATION_ERROR(-20001, 'Double booking detected! Staff ID ' || :NEW.StaffID || ' is already booked during this time.');
        END IF;
    END IF;
END;
/





ALTER TABLE Product ADD (
    CONSTRAINT chk_non_negative_onhand CHECK (QuantityOnHand >= 0)
);

BEGIN
    EXECUTE IMMEDIATE 'DROP TRIGGER trg_prevent_edit_paid_bill';
EXCEPTION
    WHEN OTHERS THEN
        IF SQLCODE != -4080 THEN
            RAISE;
        END IF;
END;
/

-- ==============================================================================
-- 1. KHÓA BẢNG CON (Bill_Detail): Ngăn chặn Thêm/Sửa/Xóa chi tiết hóa đơn
-- ==============================================================================
CREATE OR REPLACE TRIGGER trg_prevent_edit_paid_bd
BEFORE INSERT OR UPDATE OR DELETE ON Bill_Detail
FOR EACH ROW
DECLARE
    v_status VARCHAR2(20);
    v_bill_id NUMBER;
BEGIN
    IF DELETING THEN
        v_bill_id := :OLD.BillID;
    ELSE
        v_bill_id := :NEW.BillID;
    END IF;

    SELECT PaymentStatus INTO v_status
    FROM Bill
    WHERE BillID = v_bill_id;

    IF v_status = 'PAID' THEN
        RAISE_APPLICATION_ERROR(-20002, 'Bảo mật tài chính: Không thể thêm, sửa, hoặc xóa chi tiết của hóa đơn đã thanh toán (PAID).');
    END IF;
END;
/

-- ==============================================================================
-- 2. KHÓA BẢNG CHA (Bill): Ngăn chặn Sửa tổng tiền hoặc Xóa hẳn hóa đơn
-- ==============================================================================
CREATE OR REPLACE TRIGGER trg_prevent_edit_paid_bill
BEFORE UPDATE OR DELETE ON Bill
FOR EACH ROW
BEGIN
    IF :OLD.PaymentStatus = 'PAID' THEN
        IF UPDATING AND :NEW.PaymentStatus = 'REFUNDED' THEN
            NULL; 
        ELSE
            RAISE_APPLICATION_ERROR(-20003, 'Bảo mật tài chính: Không thể sửa thông tin hoặc xóa hóa đơn đã thanh toán (Ngoại trừ thao tác hoàn tiền).');
        END IF;
        
    END IF;
END;
/


