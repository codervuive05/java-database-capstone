package com.smartclinic.services;
import com.smartclinic.models.Doctor;
import com.smartclinic.repo.*;
import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
@Service
public class DoctorService {
    private final DoctorRepository doctors; private final AppointmentRepository appointments; private final TokenService tokens;
    public DoctorService(DoctorRepository d,AppointmentRepository a,TokenService t){doctors=d;appointments=a;tokens=t;}
    public List<String> getAvailableTimeSlots(Long id,LocalDate date) {
        Doctor d=doctors.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Doctor not found"));
        Set<String> booked=new HashSet<>();
        appointments.findByDoctorIdAndAppointmentTimeGreaterThanEqualAndAppointmentTimeLessThan(id,date.atStartOfDay(),date.plusDays(1).atStartOfDay())
            .forEach(a->booked.add(a.appointmentTime.toLocalTime().toString()));
        return d.availableTimes.stream().filter(t->!booked.contains(t)).filter(t->LocalDateTime.of(date,LocalTime.parse(t)).isAfter(LocalDateTime.now())).sorted().toList();
    }
    public Map<String,Object> validateLogin(String email,String password) {
        Doctor d=doctors.findByEmail(email).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
        if(!new BCryptPasswordEncoder().matches(password,d.password)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
        return Map.of("token",tokens.generateToken(d.email,"DOCTOR"),"role","DOCTOR","name",d.name,"id",d.id);
    }
}
