CREATE OR REPLACE PROCEDURE sp_calculate_bill_total (
    p_BillID IN NUMBER,
    p_Total OUT NUMBER
)
IS
    v_rows_updated NUMBER;
BEGIN
    -- 1. Extra validation to prevent null input
    IF p_BillID IS NULL THEN
        RAISE_APPLICATION_ERROR(-20010, 'Bill ID cannot be null');
    END IF;

    -- 2. Calculate and update in one atomic transaction
    UPDATE BILL
    SET TotalAmount = (
        SELECT NVL(SUM(LineTotal), 0)
        FROM BILL_DETAIL
        WHERE BillID = p_BillID
    )
    WHERE BillID = p_BillID
    RETURNING TotalAmount INTO p_Total;
    
    -- 3. Check if update actually hit a row
    v_rows_updated := SQL%ROWCOUNT;
    
    IF v_rows_updated = 0 THEN
        RAISE_APPLICATION_ERROR(-20011, 'Bill ' || p_BillID || ' does not exist');
    END IF;

EXCEPTION
    WHEN OTHERS THEN
        DBMS_OUTPUT.PUT_LINE('ERROR in sp_calculate_bill_total');
        DBMS_OUTPUT.PUT_LINE('BillID: ' || p_BillID);
        DBMS_OUTPUT.PUT_LINE('Error Code: ' || SQLCODE);
        DBMS_OUTPUT.PUT_LINE('Error Message: ' || SQLERRM);
        DBMS_OUTPUT.PUT_LINE('Backtrace: ' || DBMS_UTILITY.FORMAT_ERROR_BACKTRACE);
        RAISE;
END;
/
