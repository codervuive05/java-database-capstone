package com.smartclinic;
import com.smartclinic.models.*;
import com.smartclinic.repo.*;
import java.time.*;
import java.util.*;
import org.springframework.boot.*;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
@Component @ConditionalOnProperty(name="clinic.demo.enabled",havingValue="true")
public class DemoData implements CommandLineRunner {
    private final DoctorRepository doctors;private final PatientRepository patients;private final AppointmentRepository appointments;
    public DemoData(DoctorRepository d,PatientRepository p,AppointmentRepository a){doctors=d;patients=p;appointments=a;}
    public void run(String... args){
        if(doctors.count()>0) return;
        var encoder=new BCryptPasswordEncoder();
        for(int i=1;i<=2;i++){Doctor d=new Doctor();d.name=i==1?"Dr. Linh Nguyen":"Dr. Alex Tran";d.email="doctor"+i+"@example.test";d.specialty=i==1?"Cardiology":"Dermatology";d.password=encoder.encode("DemoDoctor!2026");d.availableTimes=List.of("09:00","10:00","11:00","14:00");doctors.save(d);}
        for(int i=1;i<=5;i++){Patient p=new Patient();p.name="Demo Patient "+i;p.email="patient"+i+"@example.test";p.phone="090000000"+i;p.password=encoder.encode("DemoPatient!2026");patients.save(p);}
        for(int i=1;i<=3;i++){Appointment a=new Appointment();a.doctor=doctors.findAll().get(0);a.patient=patients.findAll().get(i-1);a.appointmentTime=LocalDate.now().plusDays(1).atTime(8+i,0);a.reason="Synthetic coursework appointment";appointments.save(a);}
    }
}
