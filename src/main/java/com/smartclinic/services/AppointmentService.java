package com.smartclinic.services;
import com.smartclinic.models.*;
import com.smartclinic.repo.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
public class AppointmentService {
    private final AppointmentRepository appointments; private final DoctorRepository doctors; private final PatientRepository patients;
    public AppointmentService(AppointmentRepository a,DoctorRepository d,PatientRepository p){appointments=a;doctors=d;patients=p;}
    @Transactional
    public Appointment bookAppointment(String patientEmail,Long doctorId,LocalDateTime time,String reason) {
        Doctor d=doctors.lockById(doctorId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found"));
        Patient p=patients.findByEmail(patientEmail).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Patient not found"));
        if(!time.isAfter(LocalDateTime.now()) || !d.availableTimes.contains(time.toLocalTime().toString()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Choose a future available time");
        if(appointments.existsByDoctorIdAndAppointmentTime(doctorId,time)) throw new ResponseStatusException(HttpStatus.CONFLICT,"Time slot already booked");
        Appointment a=new Appointment();a.doctor=d;a.patient=p;a.appointmentTime=time;a.reason=reason;
        return appointments.save(a);
    }
    public List<Appointment> getAppointmentsForDoctorOnDate(Long id,LocalDate date){
        return appointments.findByDoctorIdAndAppointmentTimeGreaterThanEqualAndAppointmentTimeLessThan(id,date.atStartOfDay(),date.plusDays(1).atStartOfDay());
    }
}
