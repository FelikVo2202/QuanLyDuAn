ALTER TABLE PRODUCT
  ADD (
    QuantityOnHand NUMBER(10, 2) DEFAULT 0 NOT NULL,
    DeletedAt TIMESTAMP NULL
  );

CREATE OR REPLACE TRIGGER trg_update_inventory
AFTER INSERT ON Inventory_Ledger
FOR EACH ROW
BEGIN
  IF :NEW.TransactionType IN ('Restock', 'Adjustment', 'Return') THEN
    UPDATE Product
    SET QuantityOnHand = QuantityOnHand + :NEW.ChangeAmount
    WHERE ProductID = :NEW.ProductID;
  END IF;
END;
/