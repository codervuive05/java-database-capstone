package com.smartclinic.controllers;
import com.smartclinic.models.Doctor;
import com.smartclinic.repo.DoctorRepository;
import com.smartclinic.services.*;
import jakarta.validation.Valid;
import java.time.*;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@RestController @RequestMapping("/api/doctors")
public class DoctorController {
    private final DoctorRepository doctors;private final DoctorService service;private final TokenService tokens;
    public DoctorController(DoctorRepository d,DoctorService s,TokenService t){doctors=d;service=s;tokens=t;}
    @GetMapping public ResponseEntity<?> list(@RequestHeader(value="Authorization",required=false) String token,
        @RequestParam(defaultValue="") String name,@RequestParam(defaultValue="") String specialty,
        @RequestParam(required=false) LocalDate date,@RequestParam(required=false) String time) {
        tokens.validate(token,"ADMIN","PATIENT","DOCTOR");
        List<Doctor> found=doctors.findAll().stream().filter(d->d.name.toLowerCase().contains(name.toLowerCase()))
            .filter(d->specialty.isEmpty()||d.specialty.equalsIgnoreCase(specialty))
            .filter(d->time==null||(date==null?d.availableTimes.contains(time):service.getAvailableTimeSlots(d.id,date).contains(time))).toList();
        return ResponseEntity.ok(Map.of("doctors",found));
    }
    @GetMapping("/{id}/availability/{date}") public ResponseEntity<?> availability(@PathVariable Long id,@PathVariable LocalDate date,
        @RequestHeader(value="Authorization",required=false) String token) {
        tokens.validate(token,"ADMIN","PATIENT","DOCTOR");
        return ResponseEntity.ok(Map.of("doctorId",id,"date",date,"availableTimes",service.getAvailableTimeSlots(id,date)));
    }
    @PostMapping public ResponseEntity<?> add(@RequestHeader(value="Authorization",required=false) String token,@Valid @RequestBody Doctor doctor){
        tokens.validate(token,"ADMIN");doctor.id=null;doctor.password=new BCryptPasswordEncoder().encode(doctor.password);
        return ResponseEntity.status(201).body(Map.of("message","Doctor added successfully","doctor",doctors.save(doctor)));
    }
}
