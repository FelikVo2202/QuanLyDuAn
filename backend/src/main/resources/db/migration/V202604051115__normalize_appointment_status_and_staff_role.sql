UPDATE Appointment SET Status = UPPER(Status);
UPDATE Staff SET Role = 'MANAGER' WHERE Role = 'Quản lý';
UPDATE Staff SET Role = 'STYLIST' WHERE Role = 'Thợ cắt tóc';
UPDATE Staff SET Role = 'RECEPTIONIST' WHERE Role = 'Lễ tân';