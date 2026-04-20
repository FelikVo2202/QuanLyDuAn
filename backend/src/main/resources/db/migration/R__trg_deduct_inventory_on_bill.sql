CREATE OR REPLACE TRIGGER trg_deduct_inventory_on_bill
AFTER UPDATE OF PaymentStatus ON Bill -- Optimized Trigger Hook
FOR EACH ROW
WHEN (NEW.PaymentStatus = 'PAID' AND OLD.PaymentStatus != 'PAID')
BEGIN
    INSERT INTO INVENTORY_LEDGER (ProductID, ChangeAmount, TransactionType, ReferenceID)
    SELECT PRODUCTID, -QUANTITY, 'RETAIL_SALE', BillID
    FROM BILL_DETAIL
    WHERE BillID = :NEW.BillID AND
    ProductID IS NOT NULL AND
    QUANTITY > 0; -- ADDED: Only deduct if quantity is greater than 0

    INSERT INTO INVENTORY_LEDGER (ProductID, ChangeAmount, TransactionType, ReferenceID)
    SELECT sr.PRODUCTID, -(sr.QUANTITYCONSUMED * bd.QUANTITY), 'SERVICE_USE', bd.BILLID
    FROM BILL_DETAIL bd
    JOIN SERVICE_RECIPE sr ON sr.SERVICEID = bd.SERVICEID 
    WHERE BillID = :NEW.BillID AND bd.SERVICEID IS NOT NULL AND bd.QUANTITY > 0; -- ADDED: Only deduct if quantity is greater than 0


EXCEPTION
    WHEN OTHERS THEN 
    RAISE_APPLICATION_ERROR(-20008, 'Inventory deduction failed for Bill ' || :NEW.BillID || ': ' || SQLERRM);
END;
/
