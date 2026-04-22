ALTER TABLE PRODUCT
  ADD (
    QuantityOnHand NUMBER(10, 2) DEFAULT 0 NOT NULL,
    DeletedAt TIMESTAMP NULL
  );

-- Trigger maintains inventory quantity
CREATE OR REPLACE TRIGGER trg_update_inventory
AFTER INSERT ON Inventory_Ledger
FOR EACH ROW
BEGIN
  UPDATE Product
  SET QuantityOnHand = QuantityOnHand + :NEW.ChangeAmount
  WHERE ProductID = :NEW.ProductID;
END;

