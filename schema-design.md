# Smart Clinic schema

MySQL 8.4 owns transactional data. All examples are synthetic coursework records.

* `doctor`: `id BIGINT PRIMARY KEY AUTO_INCREMENT`, `name VARCHAR(120) NOT NULL`, `email VARCHAR(255) UNIQUE NOT NULL`, `specialty VARCHAR(80) NOT NULL`, `password VARCHAR(255)` containing a BCrypt hash.
* `doctor_available_times`: `doctor_id BIGINT NOT NULL REFERENCES doctor(id)`, `available_time VARCHAR(255)` in HH:mm format. JPA `@ElementCollection List<String>`.
* `patient`: `id BIGINT PRIMARY KEY AUTO_INCREMENT`, `name VARCHAR(255)`, `email VARCHAR(255) UNIQUE NOT NULL`, `phone VARCHAR(255) UNIQUE NOT NULL`, `password VARCHAR(255)` containing a BCrypt hash.
* `appointment`: `id BIGINT PRIMARY KEY AUTO_INCREMENT`, `doctor_id BIGINT NOT NULL REFERENCES doctor(id)`, `patient_id BIGINT NOT NULL REFERENCES patient(id)`, `appointment_time DATETIME(6) NOT NULL`, `reason VARCHAR(255)`, `status VARCHAR(255)`. Unique `(doctor_id, appointment_time)` prevents double booking. An appointment belongs to exactly one doctor and one patient; either person can have many appointments.
* `prescription`: `id BIGINT PRIMARY KEY AUTO_INCREMENT`, `appointment_id BIGINT NOT NULL REFERENCES appointment(id)`, `medication VARCHAR(1000)`, `instructions VARCHAR(1000)`. A doctor may only prescribe for their own appointment.

Reporting procedures are defined in `sql/reporting.sql`. Monthly/yearly reports count distinct patients; ties are resolved by lowest doctor ID. Daily reports include doctors with zero appointments. Production migrations should replace development `ddl-auto=update` before real deployment.

## NoSQL design boundary

An optional MongoDB `visit_notes` collection can store variable-format documents:
`{ "appointmentId": 1, "authorDoctorId": 1, "sections": [{"heading":"Demo note","text":"Synthetic content"}], "createdAt": "ISODate" }`.
Index `appointmentId`; validate referenced IDs through the service. The current runnable implementation stores prescriptions in MySQL and does not claim a MongoDB integration.
