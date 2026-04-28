CREATE OR REPLACE TRIGGER trg_update_inventory
AFTER INSERT ON Inventory_Ledger
FOR EACH ROW
WHEN (NEW.TransactionType IN ('RESTOCK', 'ADJUSTMENT', 'RETURN'))
BEGIN
    UPDATE Product
    SET QuantityOnHand = QuantityOnHand + :NEW.ChangeAmount
    WHERE ProductID = :NEW.ProductID;
END;