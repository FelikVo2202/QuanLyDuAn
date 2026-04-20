CREATE OR REPLACE TRIGGER trg_prevent_edit_paid_bill
AFTER UPDATE ON Bill 
FOR EACH ROW
WHEN (OLD.PaymentStatus = 'Paid')
BEGIN
    RAISE_APPLICATION_ERROR(-20009, 'Cannot edit a bill that has already been paid. Bill ID: ' || :OLD.BillID);
END;
/
