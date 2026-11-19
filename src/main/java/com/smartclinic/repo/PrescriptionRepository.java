package com.smartclinic.repo;
import com.smartclinic.models.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PrescriptionRepository extends JpaRepository<Prescription,Long> {}
