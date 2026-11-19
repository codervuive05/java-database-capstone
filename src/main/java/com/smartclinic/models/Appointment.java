package com.smartclinic.models;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
@Entity
@Table(name="appointment",uniqueConstraints=@UniqueConstraint(columnNames={"doctor_id","appointment_time"}))
public class Appointment {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @NotNull @ManyToOne(optional=false) @JoinColumn(name="doctor_id") public Doctor doctor;
    @NotNull @ManyToOne(optional=false) @JoinColumn(name="patient_id") public Patient patient;
    @NotNull @Future @Column(name="appointment_time",nullable=false) public LocalDateTime appointmentTime;
    @NotBlank public String reason;
    public String status="BOOKED";
}
