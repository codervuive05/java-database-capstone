package com.smartclinic;
import com.fasterxml.jackson.databind.*;
import com.smartclinic.services.TokenService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import java.time.LocalDate;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"spring.datasource.url=jdbc:h2:mem:clinic;MODE=MySQL;DB_CLOSE_DELAY=-1","spring.datasource.username=sa","spring.datasource.password=","spring.datasource.driver-class-name=org.h2.Driver","spring.jpa.hibernate.ddl-auto=create-drop","clinic.jwt.secret=test-only-signing-key-which-is-at-least-32-bytes-long","clinic.admin.password=TestAdmin!2026","clinic.demo.enabled=true"})
@AutoConfigureMockMvc
class ClinicIntegrationTest {
    @Autowired MockMvc mvc; @Autowired ObjectMapper json; @Autowired TokenService tokens;
    String token(String role,String email){return "Bearer "+tokens.generateToken(email,role);}
    @Test void protectedEndpointsRejectMissingAndForgedTokens() throws Exception {
        mvc.perform(get("/api/doctors")).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/doctors").header("Authorization","Bearer forged")).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/doctors").header("Authorization",token("PATIENT","patient1@example.test")).contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Test\",\"email\":\"test@example.test\",\"password\":\"abc\",\"specialty\":\"Cardiology\",\"availableTimes\":[\"09:00\"]}"))
            .andExpect(status().isForbidden());
    }
    @Test void loginRejectsIncorrectCredentialsAndDoesNotExposePasswords() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"DOCTOR\",\"email\":\"doctor1@example.test\",\"password\":\"wrong\"}"))
            .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{\"role\":\"DOCTOR\",\"email\":\"doctor1@example.test\",\"password\":\"DemoDoctor!2026\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        mvc.perform(get("/api/doctors").header("Authorization",token("PATIENT","patient1@example.test")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.doctors[0].password").doesNotExist());
    }
    @Test void bookingRemovesAvailabilityAndRejectsDuplicateAndPastTime() throws Exception {
        String day=LocalDate.now().plusDays(2).toString();
        String auth=token("PATIENT","patient1@example.test");
        String body="{\"doctorId\":1,\"appointmentTime\":\""+day+"T14:00:00\",\"reason\":\"Course test\"}";
        mvc.perform(post("/api/appointments").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/appointments").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isConflict());
        mvc.perform(get("/api/doctors/1/availability/"+day).header("Authorization",auth))
            .andExpect(status().isOk()).andExpect(jsonPath("$.availableTimes.length()").value(3));
        mvc.perform(post("/api/appointments").header("Authorization",auth).contentType(MediaType.APPLICATION_JSON).content(body.replace(day,"2000-01-01")))
            .andExpect(status().isBadRequest());
    }
    @Test void patientCannotReadAnotherPatientsAppointmentsAndPrescriptionOwnershipIsEnforced() throws Exception {
        mvc.perform(get("/api/appointments").header("Authorization",token("PATIENT","patient5@example.test")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.appointments.length()").value(0));
        String body="{\"appointmentId\":1,\"medication\":\"Demo only\",\"instructions\":\"Synthetic example\"}";
        mvc.perform(post("/api/prescriptions").header("Authorization",token("DOCTOR","doctor2@example.test")).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isForbidden());
        mvc.perform(post("/api/prescriptions").header("Authorization",token("DOCTOR","doctor1@example.test")).contentType(MediaType.APPLICATION_JSON).content(body))
            .andExpect(status().isCreated());
        mvc.perform(post("/api/prescriptions").header("Authorization",token("DOCTOR","doctor1@example.test")).contentType(MediaType.APPLICATION_JSON).content("{\"appointmentId\":1,\"medication\":\"\",\"instructions\":\"\"}"))
            .andExpect(status().isBadRequest());
    }
}
