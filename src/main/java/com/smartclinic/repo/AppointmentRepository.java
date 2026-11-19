package com.smartclinic.repo;
import com.smartclinic.models.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByDoctorIdAndAppointmentTimeGreaterThanEqualAndAppointmentTimeLessThan(Long id,LocalDateTime start,LocalDateTime end);
    List<Appointment> findByDoctorId(Long id);
    List<Appointment> findByPatientId(Long id);
    boolean existsByDoctorIdAndAppointmentTime(Long id,LocalDateTime time);
}
