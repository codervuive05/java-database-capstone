USE smart_clinic;
DELIMITER $$
CREATE PROCEDURE GetDailyAppointmentReportByDoctor(IN report_date DATE)
BEGIN
 SELECT d.id AS doctor_id,d.name AS doctor_name,COUNT(a.id) AS appointment_count
 FROM doctor d LEFT JOIN appointment a ON a.doctor_id=d.id AND DATE(a.appointment_time)=report_date
 GROUP BY d.id,d.name ORDER BY d.id;
END$$
CREATE PROCEDURE GetDoctorWithMostPatientsByMonth(IN report_year INT,IN report_month INT)
BEGIN
 SELECT d.id AS doctor_id,d.name AS doctor_name,COUNT(DISTINCT a.patient_id) AS patient_count
 FROM doctor d JOIN appointment a ON a.doctor_id=d.id
 WHERE YEAR(a.appointment_time)=report_year AND MONTH(a.appointment_time)=report_month
 GROUP BY d.id,d.name ORDER BY patient_count DESC,d.id LIMIT 1;
END$$
CREATE PROCEDURE GetDoctorWithMostPatientsByYear(IN report_year INT)
BEGIN
 SELECT d.id AS doctor_id,d.name AS doctor_name,COUNT(DISTINCT a.patient_id) AS patient_count
 FROM doctor d JOIN appointment a ON a.doctor_id=d.id WHERE YEAR(a.appointment_time)=report_year
 GROUP BY d.id,d.name ORDER BY patient_count DESC,d.id LIMIT 1;
END$$
DELIMITER ;
