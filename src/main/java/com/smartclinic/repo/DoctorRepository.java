package com.smartclinic.repo;
import com.smartclinic.models.Doctor;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface DoctorRepository extends JpaRepository<Doctor,Long> {
    Optional<Doctor> findByEmail(String email);
    @Lock(LockModeType.PESSIMISTIC_WRITE) @Query("select d from Doctor d where d.id=:id")
    Optional<Doctor> lockById(@Param("id") Long id);
}
