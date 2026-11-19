# Smart Clinic Management System

Java Development Capstone coursework. Source and validation prepared with AI assistance for learner review. All people, appointments and prescriptions are synthetic.

## Architecture

Spring Boot 3.5.7 / Java 17 serves the static role portals and a REST API. JPA persists doctors, patients, appointments and prescriptions in MySQL 8.4. Controllers validate request DTOs and JWT role claims; services enforce ownership and booking rules; repositories isolate data access. The application is a modular monolith; independently deployable services are a future boundary, not a claimed implemented microservice architecture.

See `schema-design.md`, `user-stories.md`, `sql/reporting.sql`, and `evidence/`.

## Run

1. Install Java 17, Maven and MySQL 8.4. Create database `smart_clinic` and a dedicated user.
2. Set `DB_URL` (default `jdbc:mysql://127.0.0.1:3316/smart_clinic?createDatabaseIfNotExist=true`), `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (at least 32 random bytes), and `ADMIN_PASSWORD`. Keep real secrets outside source control.
3. `mvn verify` runs integration tests using an isolated H2 database in MySQL compatibility mode.
4. `mvn package` then `java -jar target/smart-clinic-1.0.0.jar`. Default HTTP port: 8086; override `PORT`.
5. For synthetic demo seed records only, set `DEMO_ENABLED=true`. This runs only when no doctors exist. Admin email defaults to `admin@example.test`. Demo doctors: `doctor1@example.test`, `doctor2@example.test`, password `DemoDoctor!2026`. Patients: `patient1@example.test` through `patient5@example.test`, password `DemoPatient!2026`. These are public fixture credentials, not personal accounts. Disable demo mode for any real deployment.
6. After JPA creates the schema, run `mysql smart_clinic < sql/reporting.sql` once to install report procedures.

For a container, `docker build -t smart-clinic .` and pass the environment variables with `docker run --env-file .env -p 8086:8086 smart-clinic`. Use a DB hostname reachable from the container. Runtime listens on all container interfaces while local runs bind loopback. The Dockerfile is supplied; a Docker image build is not yet verified on this workstation.

## API

`POST /api/auth/login` accepts `{role,email,password}` and returns a 1-hour Bearer token. Roles are ADMIN, DOCTOR and PATIENT.

* `GET /api/doctors?name=&specialty=&date=YYYY-MM-DD&time=HH:mm` filters doctors; any authenticated role.
* `GET /api/doctors/{id}/availability/{date}` returns future unbooked slots.
* `POST /api/doctors` creates a validated profile; ADMIN only.
* `POST /api/appointments` accepts `{doctorId,appointmentTime,reason}`; PATIENT only. Identity comes from the token, not an arbitrary patient ID.
* `GET /api/appointments` returns only the authenticated patient's or doctor's appointments. Doctors can add `?date=YYYY-MM-DD`.
* `POST /api/prescriptions` accepts `{appointmentId,medication,instructions}`; the appointment's DOCTOR only.

All passwords are BCrypt hashes in MySQL and excluded from serialized entities. A pessimistic doctor lock and database uniqueness constraint prevent double-booking. Validation errors return 400, invalid tokens 401, wrong roles/ownership 403 and conflicts 409.

## Verification and limits

Four integration tests cover login failures, forged/missing JWTs, cross-role permissions, password non-disclosure, duplicate/past booking, filtered patient visibility, prescription ownership and required fields. Local MySQL SQL outputs and curl responses are collected by `scripts/collect_evidence.py`; screenshots come from the running web app. GitHub CI runs `mvn verify` on push/PR.

This educational application has no patient self-registration, appointment cancellation, audit log or production rate limiting. The NoSQL document design is documented but no MongoDB runtime is claimed. Use no real patient data. Evidence timestamps and sample report dates reflect the actual run.
