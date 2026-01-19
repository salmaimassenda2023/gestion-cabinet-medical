CREATE TABLE faq (
    id SERIAL PRIMARY KEY,
    question VARCHAR(255) NOT NULL,
    answer TEXT NOT NULL
);

INSERT INTO faq (question, answer) VALUES
('Find available clinics', 'You can find clinics by searching on our homepage. We list providers in your area with real-time availability for appointments.'),
('How to book?', 'To book an appointment, simply find a clinic you like, select an available time slot, and confirm your booking. It takes just a few seconds!'),
('View pricing', 'Pricing is transparent. You can see the consultation fees and service costs on each clinic''s profile before you book your appointment.'),
('What is Clinic Flow?', 'Clinic Flow is a platform designed to simplify healthcare management for both doctors and patients, making it easy to discover care and manage appointments.'),
('Is my data secure?', 'Yes, we take security seriously. All your personal and medical data is encrypted and handled according to strict healthcare privacy standards.');
