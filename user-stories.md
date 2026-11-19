# User stories and acceptance criteria

## Admin
As an administrator, I want to sign in to the admin portal so that doctor management is restricted to my role.
Acceptance: correct credentials return a signed role token; invalid credentials return 401; patient/doctor tokens cannot add doctors.

As an administrator, I want to add doctors with a specialty and available times so that patients can find and book them.
Acceptance: valid data creates a doctor; blank fields/invalid email/time fail validation; duplicate email returns 409; passwords are hashed and never returned.

## Patient
As a patient, I want to sign in and search doctors by name, specialty and available time so that I can find a suitable appointment.
Acceptance: matching doctors are listed; no matches has a clear empty state; authentication is required.

As a patient, I want to book a future available slot so that I can attend a consultation.
Acceptance: booking persists; taken/past slots are rejected; the booked slot disappears from availability; concurrent requests cannot double-book a doctor.

As a patient, I want to view my appointments so that I can track bookings without accessing other patients' records.
Acceptance: only appointments linked to the authenticated email are returned.

## Doctor
As a doctor, I want to sign in and view my appointments, optionally by date, so that I can plan my day.
Acceptance: all my appointments are listed with patient name and date; other doctors' appointments are excluded.

As a doctor, I want to save a prescription for my appointment so that the consultation is documented.
Acceptance: medication/instructions are required; a different doctor's appointment returns 403; a valid prescription is saved and acknowledged.

As a clinic administrator, I want daily, monthly and yearly reports so that I can evaluate synthetic appointment activity.
Acceptance: procedures execute in MySQL; month/year reports use distinct patient counts and deterministic tie-breaking.
