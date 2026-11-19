package com.smartclinic.controllers;
import com.smartclinic.repo.PatientRepository;
import com.smartclinic.services.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.Map;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.http.*;
import org.springframework.web.server.ResponseStatusException;
@RestController @RequestMapping("/api/auth")
public class AuthController {
    public record Login(@NotBlank String role,@NotBlank @Email String email,@NotBlank String password){}
    private final PatientRepository patients; private final DoctorService doctors;private final TokenService tokens;
    @Value("${clinic.admin.email}") private String adminEmail;
    @Value("${clinic.admin.password}") private String adminPassword;
    public AuthController(PatientRepository p,DoctorService d,TokenService t){patients=p;doctors=d;tokens=t;}
    @PostMapping("/login") public ResponseEntity<?> login(@Valid @RequestBody Login request){
        if(request.role().equals("DOCTOR")) return ResponseEntity.ok(doctors.validateLogin(request.email(),request.password()));
        if(request.role().equals("PATIENT")) {
            var p=patients.findByEmail(request.email()).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials"));
            if(!new BCryptPasswordEncoder().matches(request.password(),p.password)) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
            return ResponseEntity.ok(Map.of("token",tokens.generateToken(p.email,"PATIENT"),"role","PATIENT","id",p.id,"name",p.name));
        }
        if(request.role().equals("ADMIN") && adminEmail.equals(request.email()) && adminPassword.equals(request.password()))
            return ResponseEntity.ok(Map.of("token",tokens.generateToken(adminEmail,"ADMIN"),"role","ADMIN","name","Clinic Administrator"));
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,"Invalid credentials");
    }
}
