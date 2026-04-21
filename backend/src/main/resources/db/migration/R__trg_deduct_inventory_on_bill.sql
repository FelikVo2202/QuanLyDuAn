CREATE OR REPLACE TRIGGER trg_deduct_inventory_on_bill
AFTER UPDATE OF PaymentStatus ON Bill -- Optimized Trigger Hook
FOR EACH ROW
WHEN (NEW.PaymentStatus = 'PAID' AND OLD.PaymentStatus != 'PAID')
BEGIN
    INSERT INTO INVENTORY_LEDGER (ProductID, ChangeAmount, TransactionType, ReferenceID)
    SELECT PRODUCTID, -(bd.QUANTITY * p.ConversionFactor), 'RETAIL_SALE', bd.BillID
    FROM BILL_DETAIL bd
    JOIN PRODUCT p ON p.ProductID = bd.ProductID
    WHERE BillID = :NEW.BillID AND
    ProductID IS NOT NULL AND
    QUANTITY > 0; -- ADDED: Only deduct if quantity is greater than 0

    INSERT INTO INVENTORY_LEDGER (ProductID, ChangeAmount, TransactionType, ReferenceID)
    SELECT sr.PRODUCTID, -(sr.QUANTITYCONSUMED * bd.QUANTITY), 'SERVICE_USE', bd.BILLID
    FROM BILL_DETAIL bd
    JOIN SERVICE_RECIPE sr ON sr.SERVICEID = bd.SERVICEID 
    WHERE BillID = :NEW.BillID AND bd.SERVICEID IS NOT NULL AND bd.QUANTITY > 0; -- ADDED: Only deduct if quantity is greater than 0

    MERGE INTO PRODUCT pr
        USING (
            SELECT ProductID, SUM(DeductAmount) AS TotalDeduct
            FROM (
                 SELECT bd.ProductID, (bd.QUANTITY * p.ConversionFactor) AS DeductAmount
                 FROM BILL_DETAIL bd
                 JOIN PRODUCT p ON p.ProductID = bd.ProductID
                 WHERE bd.BillID = :NEW.BillID AND bd.ProductID IS NOT NULL AND bd.QUANTITY > 0
                 UNION ALL
                 SELECT sr.ProductID, (sr.QUANTITYCONSUMED * bd.QUANTITY) AS DeductAmount
                 FROM BILL_DETAIL bd
                 JOIN SERVICE_RECIPE sr ON sr.SERVICEID = bd.SERVICEID
                 WHERE bd.BillID = :NEW.BillID AND bd.SERVICEID IS NOT NULL AND bd.QUANTITY > 0
             )
            GROUP BY ProductID
        ) src
        ON (p.ProductID = src.ProductID)
        WHEN MATCHED THEN
            UPDATE SET p.QuantityOnHand = p.QuantityOnHand - src.TotalDeduct;

EXCEPTION
    WHEN OTHERS THEN 
    RAISE_APPLICATION_ERROR(-20008, 'Inventory deduction failed for Bill ' || :NEW.BillID || ': ' || SQLERRM);
END;
/
