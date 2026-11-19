package com.smartclinic.models;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
@Entity @Table(name="patient")
public class Patient {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) public Long id;
    @NotBlank public String name;
    @NotBlank @Email @Column(unique=true,nullable=false) public String email;
    @NotBlank @Column(unique=true,nullable=false) public String phone;
    @NotBlank @JsonProperty(access=JsonProperty.Access.WRITE_ONLY) public String password;
}
