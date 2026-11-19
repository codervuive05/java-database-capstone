package com.smartclinic.controllers;
import com.smartclinic.models.Prescription;
import com.smartclinic.repo.*;
import com.smartclinic.services.TokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/prescriptions")
public class PrescriptionController {
    public record Request(@NotNull Long appointmentId,@NotBlank String medication,@NotBlank String instructions){}
    private final TokenService tokens;private final PrescriptionRepository prescriptions;private final AppointmentRepository appointments;
    public PrescriptionController(TokenService t,PrescriptionRepository p,AppointmentRepository a){tokens=t;prescriptions=p;appointments=a;}
    @PostMapping public ResponseEntity<?> save(@RequestHeader(value="Authorization",required=false) String token,@Valid @RequestBody Request r){
        var claims=tokens.validate(token,"DOCTOR");
        var appointment=appointments.findById(r.appointmentId()).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND,"Appointment not found"));
        if(!appointment.doctor.email.equals(claims.getSubject())) return ResponseEntity.status(403).body(Map.of("error","Appointment belongs to another doctor"));
        Prescription p=new Prescription();p.appointment=appointment;p.medication=r.medication();p.instructions=r.instructions();
        return ResponseEntity.status(201).body(Map.of("message","Prescription saved","prescription",prescriptions.save(p)));
    }
}
