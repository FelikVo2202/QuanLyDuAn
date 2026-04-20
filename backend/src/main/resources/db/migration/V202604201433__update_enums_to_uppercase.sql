ALTER TABLE Product MODIFY ProductType DEFAULT 'BOTH';
ALTER TABLE Product DROP CONSTRAINT chk_product_type;
ALTER TABLE Product ADD CONSTRAINT chk_product_type
    CHECK (ProductType IN ('RETAIL', 'PROFESSIONAL', 'BOTH'));

ALTER TABLE Inventory_Ledger DROP CONSTRAINT chk_trans_type;
ALTER TABLE Inventory_Ledger ADD CONSTRAINT chk_trans_type
    CHECK (TransactionType IN ('RESTOCK', 'SERVICE_USE', 'RETAIL_SALE', 'ADJUSTMENT', 'RETURN'));

ALTER TABLE Bill MODIFY PaymentStatus DEFAULT 'PENDING';
ALTER TABLE Bill DROP CONSTRAINT chk_payment_status;
ALTER TABLE Bill ADD CONSTRAINT chk_payment_status
    CHECK (PaymentStatus IN ('PENDING', 'PAID', 'REFUNDED', 'FAILED'));
