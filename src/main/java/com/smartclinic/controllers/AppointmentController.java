package com.smartclinic.controllers;
import com.smartclinic.repo.*;
import com.smartclinic.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.*;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/appointments")
public class AppointmentController {
    public record Booking(@NotNull Long doctorId,@NotNull @Future LocalDateTime appointmentTime,@NotBlank String reason){}
    private final TokenService tokens;private final AppointmentService service;private final AppointmentRepository appointments;
    private final DoctorRepository doctors;private final PatientRepository patients;
    public AppointmentController(TokenService t,AppointmentService s,AppointmentRepository a,DoctorRepository d,PatientRepository p){tokens=t;service=s;appointments=a;doctors=d;patients=p;}
    @PostMapping public ResponseEntity<?> book(@RequestHeader(value="Authorization",required=false) String token,@Valid @RequestBody Booking b){
        var c=tokens.validate(token,"PATIENT");
        return ResponseEntity.status(201).body(Map.of("appointment",service.bookAppointment(c.getSubject(),b.doctorId(),b.appointmentTime(),b.reason())));
    }
    @GetMapping public ResponseEntity<?> mine(@RequestHeader(value="Authorization",required=false) String token,@RequestParam(required=false) LocalDate date){
        var c=tokens.validate(token,"PATIENT","DOCTOR");
        if(c.get("role").equals("PATIENT")) return ResponseEntity.ok(Map.of("appointments",appointments.findByPatientId(patients.findByEmail(c.getSubject()).orElseThrow().id)));
        Long id=doctors.findByEmail(c.getSubject()).orElseThrow().id;
        return ResponseEntity.ok(Map.of("appointments",date==null?appointments.findByDoctorId(id):service.getAppointmentsForDoctorOnDate(id,date)));
    }
}
