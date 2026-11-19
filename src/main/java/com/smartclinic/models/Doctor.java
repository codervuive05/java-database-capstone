package com.smartclinic.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="doctor")
public class Doctor {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    public Long id;
    @NotBlank @Size(max=120) public String name;
    @NotBlank @Email @Column(unique=true,nullable=false) public String email;
    @NotBlank @Size(max=80) public String specialty;
    @NotBlank @JsonProperty(access=JsonProperty.Access.WRITE_ONLY)
    public String password;
    @ElementCollection(fetch=FetchType.EAGER)
    @CollectionTable(name="doctor_available_times",joinColumns=@JoinColumn(name="doctor_id"))
    @Column(name="available_time")
    @NotEmpty public List<@Pattern(regexp="([01][0-9]|2[0-3]):[0-5][0-9]") String> availableTimes = new ArrayList<>();
}
