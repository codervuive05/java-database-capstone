package com.smartclinic.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="prescription")
public class Prescription {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @NotNull @ManyToOne(optional=false) @JoinColumn(name="appointment_id") public Appointment appointment;
    @NotBlank @Size(max=1000) public String medication;
    @NotBlank @Size(max=1000) public String instructions;
}
